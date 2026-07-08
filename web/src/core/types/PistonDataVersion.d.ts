import {VersionType} from "./VersionWrapper";
import AnyGameVersion from "./GameVersion";

export default interface PistonDataVersion {
  id:	AnyGameVersion;
  type:	VersionType;
  url:string;
  time:	string;
  releaseTime: string;
  sha1:	string;
  complianceLevel: number;
}
