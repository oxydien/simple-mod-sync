import PaginatedData from "../../types/PaginatedData";
import {PlatformContent} from "../../types/abstraction/PlatformContent";
import {ResultData} from "../../types/ResultData";
import ContentType from "../../types/sms/ContentType";
import SyncEnvironment from "../../types/SyncEnvironment";
import LoaderType from "../../types/LoaderType";
import {ContentPlatform} from "../ContentPlatform";
import {apiGet} from "../../comm";
import {
  CurseForgeProject,
  CurseForgeSearchResponse,
  CurseForgeModFilesResponse,
  CurseForgeFile,
} from "../../types/curseforge";
import PlatformFile from "../../types/abstraction/PlatformFile";
import {MC_LOADER_UNKNOWN, MC_VERSION_UNKNOWN} from "../../constants";

const MINECRAFT_GAME_ID = 432;

const CLASS_MODPACK = 4471;
const CLASS_RESOURCEPACK = 12;
const CLASS_DATAPACK = 6945;
const CLASS_SHADERS = 6552;
const CLASS_MOD = 6;

const CLASS_MODPACK_URL = "modpack";
const CLASS_RESOURCEPACK_URL = "texture-packs";
const CLASS_DATAPACK_URL = "data-packs";
const CLASS_SHADERS_URL = "shaders";
const CLASS_MOD_URL = "mc-mods";

const CURSE_PAGE_SIZE = 50;

const CURSE_BASE_URL = "https://www.curseforge.com/minecraft";
/**
 * Using unofficial Curseforge API proxy `curse.tools` because I ain't contacting overwolf.
 * If they wish to give an official API key to this project, business email is open.
 */
const CURSE_API_BASE_URL = "https://api.curse.tools";

const CURSE_ROUTES = {
  "status": "/", // returns text "CurseForge Core ..."
  "search": "/v1/mods/search", // parametters: gameId, classId, searchFilter, sortField=6, gameVersion, modLoaderType (using LOADER_ID), index (offset), pageSize=CURSE_PAGE_SIZE
  "files": "/v1/mods/{modId}/files",
} as const;

type CurseRoute = keyof typeof CURSE_ROUTES;

const LOADER_ID: Record<LoaderType, number> = {
  "any": 0,
  "fabric": 4,
  "quilt": 5,
  "neoforge": 6,
} as const;

const TYPE_TO_CLASS: Partial<Record<ContentType, number>> = {
  mod: CLASS_MOD,
  resourcepack: CLASS_RESOURCEPACK,
  shader: CLASS_SHADERS,
  datapack: CLASS_DATAPACK,
};

const CLASS_TO_TYPE: Partial<Record<number, ContentType>> = {
  [CLASS_MOD]: "mod",
  [CLASS_RESOURCEPACK]: "resourcepack",
  [CLASS_SHADERS]: "shader",
  [CLASS_DATAPACK]: "datapack",
};

const CLASS_URL: Partial<Record<number, string>> = {
  [CLASS_MODPACK]: CLASS_MODPACK_URL,
  [CLASS_RESOURCEPACK]: CLASS_RESOURCEPACK_URL,
  [CLASS_DATAPACK]: CLASS_DATAPACK_URL,
  [CLASS_SHADERS]: CLASS_SHADERS_URL,
  [CLASS_MOD]: CLASS_MOD_URL,
};

export default class CursePlatform extends ContentPlatform {
  getName(): string {
    return "CurseForge";
  }

  getIcon(): string {
    return "platforms/curseforge";
  }

  /** Checks whether the CurseForge API is currently reachable. */
  async isAvailable(): Promise<ResultData<boolean>> {
    // The status route returns plain text, not JSON, so apiGet fails to
    // parse it and the raw body ends up in `error`. Checking for the
    // known text is ugly, but it works.
    const result = await apiGet<unknown>(this.resolve("status"));
    if (result.error?.includes("CurseForge Core")) {
      return {
        data: true,
      };
    }
    return {
      ...result,
      data: false,
    };
  }

  /** Searches CurseForge for content matching the query, type, and environment. */
  search(query: string, type: ContentType, environment: SyncEnvironment | null, page: number): Promise<ResultData<PaginatedData<PlatformContent>>> {
    const classId = this.mapLocalContentType(type);
    const index = page * CURSE_PAGE_SIZE;

    const url = this.resolve("search");
    url.searchParams.set("gameId", MINECRAFT_GAME_ID.toString());
    url.searchParams.set("classId", classId.toString());
    url.searchParams.set("searchFilter", query);
    url.searchParams.set("sortField", "6");
    url.searchParams.set("sortOrder", "desc");
    url.searchParams.set("index", index.toString());
    url.searchParams.set("pageSize", CURSE_PAGE_SIZE.toString());

    if (environment) {
      url.searchParams.set("gameVersion", environment.mc_version);
      if (type === "mod")
        url.searchParams.set("modLoaderType", LOADER_ID[environment.mc_loader as LoaderType].toString());
    }

    return new Promise(async (resolve, _) => {
      const res = await apiGet<CurseForgeSearchResponse>(url);
      if (this.dataOrError(resolve, res)) return;

      const paginated = res.data!;
      const hits = paginated.data.map(mod => this.mapCurseForgeProject(mod));
      resolve({
        data: {
          data: hits,
          currentPage: page,
          pageSize: paginated.pagination.pageSize,
          totalCount: paginated.pagination.totalCount,
          totalPages: Math.ceil(paginated.pagination.totalCount / CURSE_PAGE_SIZE)
        }
      });
    });
  }

  getVersionsFor(content: PlatformContent, contentType: ContentType, environment: SyncEnvironment | null): Promise<ResultData<PlatformFile[]>> {
    const curseForgeProject = content.platform_info as CurseForgeProject;

    const url = this.resolve("files", { "modId": curseForgeProject.id.toString() });
    url.searchParams.set("pageSize", CURSE_PAGE_SIZE.toString());

    if (environment) {
      url.searchParams.set("gameVersion", environment.mc_version);
      if (contentType === "mod")
        url.searchParams.set("modLoaderType", LOADER_ID[environment.mc_loader as LoaderType].toString());
    }

    return new Promise(async (resolve, _) => {
      const res = await apiGet<CurseForgeModFilesResponse>(url);
      if (this.dataOrError(resolve, res)) return;

      const paginated = res.data!;

      const platformFiles = paginated.data.map((f) => this.mapCurseFile(f, environment));
      resolve({
        data: platformFiles,
      })
    })
  }


  /** Builds the full URL for a given CurseForge API route. */
  resolve(route: CurseRoute, props: Record<string, string> | null = null): URL {
    let str = `${CURSE_API_BASE_URL}${CURSE_ROUTES[route]}`;

    if (props) {
      for (const entry of Object.entries(props)) {
        str = str.replace(`{${entry[0]}}`, entry[1]);
      }
    }

    return new URL(str);
  }

  /** Maps a CurseForge classId to the local content type. */
  mapCurseForgeContentType(classId: number): ContentType {
    const type = CLASS_TO_TYPE[classId];
    if (!type) {
      throw new Error(`Unknown CurseForge classId "${classId}"`);
    }
    return type;
  }

  /** Maps a local content type to the CurseForge classId. */
  mapLocalContentType(type: ContentType): number {
    const classId = TYPE_TO_CLASS[type];
    if (classId === undefined) {
      throw new Error(`ContentType "${type}" is not supported by CurseForge`);
    }
    return classId;
  }

  /** Converts a CurseForge mod into the local platform content shape. */
  mapCurseForgeProject(mod: CurseForgeProject): PlatformContent {
    const contentType = this.mapCurseForgeContentType(mod.classId);
    const pageUrl = mod.links?.websiteUrl || "";

    return {
      content_type: contentType,
      slug: mod.slug,
      title: mod.name,
      icon_url: mod.logo?.url ?? "",
      page_url: pageUrl,
      summary: mod.summary,
      platform_info: mod,
    };
  }

  mapCurseFile(file: CurseForgeFile, environment: SyncEnvironment | null): PlatformFile {
    return {
      name: file.displayName,
      file_url: file.downloadUrl,
      version: file.id.toString(),
      mc_loaders: [environment?.mc_loader || MC_LOADER_UNKNOWN],
      mc_versions: file.gameVersions || [environment?.mc_version || MC_VERSION_UNKNOWN],
    };
  }
}