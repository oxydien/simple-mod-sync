import {ContentPlatform} from "./ContentPlatform";
import ModrinthPlatform from "./modrinth/ModrinthPlatform";
import CursePlatform from "./curseforge/CursePlatform";

type ContentPlatformKey = "modrinth" | "curseforge";

export class ContentPlatformManager {
  public platforms: Record<ContentPlatformKey, ContentPlatform>;

  constructor() {
    this.platforms = {
      "modrinth": new ModrinthPlatform(),
      "curseforge": new CursePlatform(),
    };
  }

  async getAvailable(): Promise<ContentPlatformKey[]> {
    const platformEntries = Object.keys(this.platforms) as ContentPlatformKey[];

    const results = await Promise.all(
      platformEntries.map(async (key) => {
        const platform = this.platforms[key];
        console.debug("getAvailable", "Looking up", key, platform);
        if (platform == null) return null;
        const isAvailable = await platform.isAvailable();
        if (isAvailable.data) return key;
        console.warn(`Content platform ${key} is not available`, isAvailable.error, `, Internal issue?: ${isAvailable.error_internal}`);
        return null;
      })
    );

    return results.filter((x): x is ContentPlatformKey => x !== null);
  }

  getPlatform(key: ContentPlatformKey): ContentPlatform | null {
    return this.platforms[key];
  }
}

export type { ContentPlatformKey }
