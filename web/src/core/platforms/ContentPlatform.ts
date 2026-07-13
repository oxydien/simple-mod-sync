import ContentType from "../types/sms/ContentType";
import SyncEnvironment from "../types/SyncEnvironment";
import {ResultData} from "../types/ResultData";
import PaginatedData from "../types/PaginatedData";
import {PlatformContent} from "../types/abstraction/PlatformContent";
import PlatformFile from "../types/abstraction/PlatformFile";


export abstract class ContentPlatform {
  /** Returns the display name of this platform. */
  abstract getName(): string;

  /** Returns the icon identifier for this platform. */
  abstract getIcon(): string;

  /** Checks whether the platform is currently reachable. */
  abstract isAvailable(): Promise<ResultData<boolean>>;

  /** Searches platform for content matching the query, type, and environment. */
  abstract search(query: string, type: ContentType, environment: SyncEnvironment | null, page: number)
    : Promise<ResultData<PaginatedData<PlatformContent>>>;

  /** Searches platform for all files for given content and environment.
   * Note that the content has to be fetched from the same platform. */
  abstract getVersionsFor(content: PlatformContent, contentType: ContentType, environment: SyncEnvironment | null)
    : Promise<ResultData<PlatformFile[]>>;



  /** Helper to forward errors up. Returns true when result does not contain data */
  dataOrError(resolve: (value: ResultData<any> | PromiseLike<ResultData<any>>) => void, res: ResultData<any>): boolean {
    if (!res.data) {
      resolve({
        ...res,
        data: null,
      });
      return true;
    }

    return false;
  }
}