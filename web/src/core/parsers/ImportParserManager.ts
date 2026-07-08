import {ImportFileParser} from "./ImportFileParser";
import SyncSchemaParser from "./SyncSchemaParser";

type ImportParserKey = "sms";

export default class ImportParserManager {
  public parsers: Record<ImportParserKey, ImportFileParser>;

  constructor() {
    this.parsers = {
      "sms": new SyncSchemaParser(),
    };
  }

  getParser(key: ImportParserKey): ImportFileParser | null {
    return this.parsers[key];
  }
}

export type { ImportParserKey }
