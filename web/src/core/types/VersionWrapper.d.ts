import AnyGameVersion from "./GameVersion";

type VersionType = "release" | "snapshot";

export interface VersionWrapper {
  version: AnyGameVersion;
  type: VersionType;
}

export type { VersionType };
