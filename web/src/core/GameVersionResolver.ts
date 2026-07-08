import {VersionWrapper} from "./types/VersionWrapper";
import {ResultData} from "./types/ResultData";
import {apiGet} from "./comm";
import FabricGameVersion from "./types/FabricGameVersion";

/**
 * Using fabrics api for this, since pistondata does not allow cross-origin requests
 */
const MANIFEST_URL = "https://meta.fabricmc.net/v2/versions/game";

export default class GameVersionResolver {
  private cache: VersionWrapper[];
  private hasFetched: boolean;

  constructor() {
    this.cache = [];
    this.hasFetched = false;
  }

  async getVersions(): Promise<ResultData<VersionWrapper[]>> {
    if (this.hasFetched) {
      return new Promise((resolve) => {resolve({data: this.cache})});
    }

    this.hasFetched = true;
    const res = await apiGet<FabricGameVersion[]>(MANIFEST_URL);
    if (!res.data) {
      return {
        ...res,
        data: null,
      }
    }

    this.cache = res.data.map((mcVersion): VersionWrapper => {
      return {
        version: mcVersion.version,
        type: mcVersion.stable ? "release" : "snapshot",
      }
    });
    return {data: this.cache};
  }
}
