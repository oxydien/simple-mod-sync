import SyncSchema from "../SyncSchema";
import SyncEnvironment from "../../SyncEnvironment";
import ExtendedContent from "./ExtendedContent";


export default interface ExtendedSyncSchema extends SyncSchema {
  $schema?: string;
  $comment_update_at?: string,
  environment?: SyncEnvironment;
  sync: ExtendedContent[];
}
