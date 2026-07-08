import Content from "./Content";
import Modification from "./Modification";

export default interface SyncSchema {
  sync_version: number;
  sync: Content[];
  modify?: Modification[];
}
