import LoaderType from "./LoaderType";
import AnyGameVersion from "./GameVersion";

export default interface SyncEnvironment {
  mc_version: string | AnyGameVersion;
  mc_loader: string | LoaderType;
}
