import PistonDataVersion from "./PistonDataVersion";

export default interface PistonDataManifest {
  latest: PistonDataVersion;
  versions: PistonDataVersion[];
}