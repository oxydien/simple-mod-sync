import PaginatedData from "../../types/PaginatedData";
import {PlatformContent} from "../../types/abstraction/PlatformContent";
import {ResultData} from "../../types/ResultData";
import ContentType from "../../types/sms/ContentType";
import SyncEnvironment from "../../types/SyncEnvironment";
import {ContentPlatform} from "../ContentPlatform";
import {apiGet} from "../../comm";
import {
  ModrinthFacetPair,
  ModrinthFacets,
  ModrinthPaginatedResponse,
  ModrinthProject,
  ModrinthProjectType,
  ModrinthResponse,
  ModrinthStatistics,
  ModrinthVersion,
} from "../../types/modrinth";
import PlatformFile from "../../types/abstraction/PlatformFile";
import {MC_LOADER_UNKNOWN, MC_VERSION_UNKNOWN} from "../../constants";

const MODRINTH_PAGE_SIZE = 50;

const MODRINTH_BASE_URL = "https://modrinth.com";
const MODRINTH_API_BASE_URL = "https://api.modrinth.com";

const MODRINTH_ROUTES = {
  "status": "/v2/statistics",
  "search": "/v2/search",
  "version": "/v2/project/{slug}/version",
} as const;

type ModrinthRoute = keyof typeof MODRINTH_ROUTES;

export default class ModrinthPlatform extends ContentPlatform {
  getName(): string {
    return "Modrinth";
  }

  getIcon(): string {
    return "platforms/modrinth";
  }

  /** Checks whether the Modrinth API is currently reachable. */
  async isAvailable(): Promise<ResultData<boolean>> {
    const result = await apiGet<ModrinthStatistics>(this.resolve("status"));
    if (result.data?.projects && result.data.projects > 0) {
      return {
        data: true,
      };
    }
    return {
      ...result,
      data: false,
    };
  }

  /** Searches Modrinth for content matching the query, type, and environment. */
  search(query: string, type: ContentType, environment: SyncEnvironment | null, page: number): Promise<ResultData<PaginatedData<PlatformContent>>> {
    const facets = this.createFacets(type, environment);
    const offset = page * MODRINTH_PAGE_SIZE;

    const facetsStr = JSON.stringify(facets);

    const url = this.resolve("search");
    url.searchParams.set("query", query);
    url.searchParams.set("facets", facetsStr);
    url.searchParams.set("offset", offset.toString());
    url.searchParams.set("limit", MODRINTH_PAGE_SIZE.toString());

    return new Promise(async (resolve, _) => {
      const res = await apiGet<ModrinthResponse<ModrinthPaginatedResponse<ModrinthProject>>>(url);
      if (this.dataOrError(resolve, res)) return;

      const paginated = res.data as ModrinthPaginatedResponse<ModrinthProject>;
      const hits = paginated.hits.map(project => this.mapModrinthProject(project));
      resolve({
        data: {
          data: hits,
          currentPage: page,
          pageSize: paginated.limit,
          totalCount: paginated.total_hits,
          totalPages: Math.ceil(paginated.total_hits / MODRINTH_PAGE_SIZE)
        }
      })
    })
  }

  getVersionsFor(content: PlatformContent, contentType: ContentType, environment: SyncEnvironment | null): Promise<ResultData<PlatformFile[]>> {
    const slug = content.slug;

    const url = this.resolve("version", {"slug": slug});
    url.searchParams.set("include_changelog", "false");

    if (environment) {
      const loaders = `["${environment.mc_loader}"]`;
      const gameVersions = `["${environment.mc_version}"]`
      if (contentType === "mod")
        url.searchParams.set("loaders", loaders);
      url.searchParams.set("game_versions", gameVersions);
    }

    return new Promise(async (resolve, _) => {
      const res = await apiGet<ModrinthResponse<ModrinthVersion[]>>(url);
      if (this.dataOrError(resolve, res)) return;

      const data = res.data as ModrinthVersion[];
      if (data.length <= 0) {
        resolve({
          data: [],
        });
        return;
      }

      const platformFiles = data.map((v) => this.mapModrinthVersion(v, environment));
      resolve({
        data: platformFiles,
      })
    })
  }

  dataOrError(resolve: (value: (ResultData<any> | PromiseLike<ResultData<any>>)) => void, res: ResultData<any>): boolean {
    const base = super.dataOrError(resolve, res);
    if (base) return true;

    const data = res.data;
    if ("error" in data) {
      resolve({
        error: `${data.error}: ${data.description}`,
        error_internal: false,
      });
      return true;
    }

    return false;
  }

  /** Builds the full URL for a given Modrinth API route. */
  resolve(route: ModrinthRoute, props: Record<string, string> | null = null): URL {
    let str = `${MODRINTH_API_BASE_URL}${MODRINTH_ROUTES[route]}`;

    if (props) {
      for (const entry of Object.entries(props)) {
        str = str.replace(`{${entry[0]}}`, entry[1]);
      }
    }

    return new URL(str);
  }

  /** Builds the Modrinth search facets for the given content type and environment. */
  createFacets(type: ContentType, environment: SyncEnvironment | null): ModrinthFacets {
    const typeFacet: ModrinthFacetPair = [`project_type:${this.mapLocalContentType(type)}`];

    if (environment) {
      const versionFacet: ModrinthFacetPair = [`versions:${environment.mc_version}`];

      if (type === "mod") {
        const loaderFacet: ModrinthFacetPair = [`categories:${environment.mc_loader}`];
        return [typeFacet, versionFacet, loaderFacet];
      }

      return [typeFacet, versionFacet];
    }

    return [typeFacet];
  }

  /** Maps a Modrinth project type to the local content type. Kept separate in case the two diverge later. */
  mapModrinthContentType(type: ModrinthProjectType): ContentType {
    return type as ContentType;
  }

  /** Maps a local content type to the Modrinth project type. Kept separate in case the two diverge later. */
  mapLocalContentType(type: ContentType): ModrinthProjectType {
    return type as ModrinthProjectType;
  }

  /** Converts a Modrinth project into the local platform content shape. */
  mapModrinthProject(project: ModrinthProject): PlatformContent {
    const contentType = this.mapModrinthContentType(project.project_type);
    return {
      content_type: contentType,
      slug: project.slug,
      title: project.title,
      icon_url: project.icon_url,
      page_url: `${MODRINTH_BASE_URL}/${contentType}/${project.slug}`,
      summary: project.description,

      platform_info: project
    }
  }

  mapModrinthVersion(version: ModrinthVersion, environment: SyncEnvironment | null): PlatformFile {
    let file = version.files.find(e => e.primary);
    if (!file) {
      file = version.files[0];
    }

    return {
      name: `${version.name} ${version.version_number}`,
      file_url: file.url,
      version: version.id,
      mc_loaders: version.loaders || [environment?.mc_loader || MC_LOADER_UNKNOWN],
      mc_versions: version.game_versions || [environment?.mc_version || MC_VERSION_UNKNOWN],
    };
  }
}