import ExtendedSyncSchema from "../types/sms/extensions/ExtendedSyncSchema";
import { ResultData } from "../types/ResultData";
import {ImportFileParser} from "./ImportFileParser";


export default class SyncSchemaParser implements ImportFileParser {
  getFileExtensions(): string[] {
    return ['json'];
  }

  getFileNames(): string[] {
    return [];
  }

  /**
   * Parses a JSON file into an ExtendedSyncSchema object.
   * Validates required fields: sync_version (safe integer) and sync (non-empty array).
   */
  async parseFile(file: File): Promise<ResultData<ExtendedSyncSchema>> {
    let text: string;
    try {
      const buffer = await file.arrayBuffer();
      text = new TextDecoder('utf-8').decode(buffer);
    } catch (error) {
      return {
        error: `Read error: ${String(error)}`,
      };
    }

    let json: unknown;
    try {
      json = JSON.parse(text);
    } catch (error) {
      return {
        error: `Parse error: Invalid JSON - ${String(error)}`,
      };
    }

    if (!this.isObject(json)) {
      return {
        error: 'Parse error: Root is not an object',
      };
    }

    // Validate sync_version
    if (!('sync_version' in json) || !Number.isSafeInteger(json.sync_version)) {
      return {
        error: 'Validation error: Missing or invalid "sync_version" (must be a safe integer)',
      };
    }

    // Validate sync array
    if (!('sync' in json) || !Array.isArray(json.sync)) {
      return {
        error: 'Validation error: Missing or invalid "sync" (must be an array)',
      };
    }

    const syncData = json.sync as unknown[];

    // Validate each sync item
    for (let i = syncData.length - 1; i >= 0; i--) {
      const content = syncData[i];

      if (!this.isObject(content)) {
        return {
          error: `Validation error: Sync item at index ${i} is not an object`,
        };
      }

      if (!('url' in content) || typeof content.url !== 'string') {
        return {
          error: `Validation error: Sync item at index ${i} missing or invalid "url" (must be a string)`,
        };
      }

      if (!('name' in content) || typeof content.name !== 'string') {
        return {
          error: `Validation error: Sync item at index ${i} missing or invalid "name" (must be a string)`,
        };
      }
    }

    return {
      data: json as unknown as ExtendedSyncSchema,
    };
  }

  private isObject(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null && !Array.isArray(value);
  }
}