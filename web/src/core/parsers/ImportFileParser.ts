import {ResultData} from "../types/ResultData";
import ExtendedSyncSchema from "../types/sms/extensions/ExtendedSyncSchema";


export abstract class ImportFileParser {
  abstract getFileExtensions(): string[];
  abstract getFileNames(): string[];

  abstract parseFile(file: File): Promise<ResultData<ExtendedSyncSchema>>;
}