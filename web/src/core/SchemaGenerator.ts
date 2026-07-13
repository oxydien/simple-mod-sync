import {ContentPlatformManager} from "./platforms/ContentPlatformManager";
import ImportParserManager from "./parsers/ImportParserManager";
import ExtendedSyncSchema from "./types/sms/extensions/ExtendedSyncSchema";
import {links} from "../func/links";
import GameVersionResolver from "./GameVersionResolver";
import SyncEnvironment from "./types/SyncEnvironment";

let INSTANCE: SchemaGenerator;

export class SchemaGenerator {
  private readonly contentPlatforms: ContentPlatformManager;
  private readonly parsers: ImportParserManager;
  private readonly versionResolver: GameVersionResolver;

  constructor() {
    this.contentPlatforms = new ContentPlatformManager();
    this.parsers = new ImportParserManager();
    this.versionResolver = new GameVersionResolver();
  }

  getContentPlatforms() {
    return this.contentPlatforms;
  }
  getParsers() {
    return this.parsers;
  }
  async getVersions() {
    return await this.versionResolver.getVersions();
  }

  createDefault(env: SyncEnvironment | null): ExtendedSyncSchema {
    return {
      "$comment_update_at": links.this,
      "$schema": links.sms_schema,
      environment: env,
      sync_version: 3,
      sync: [],
      modify: [],
    }
  }
}

export function sms(): SchemaGenerator {
  if (!INSTANCE) {
    INSTANCE = new SchemaGenerator();
  }
  return INSTANCE;
}