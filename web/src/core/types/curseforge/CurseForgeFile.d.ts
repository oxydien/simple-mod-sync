import AnyGameVersion from "../GameVersion";

export default interface CurseForgeFile {
  id: number;
  gameId: number;
  modId: number;
  displayName: string;
  fileSizeOnDisk: number;
  gameVersions: AnyGameVersion[];
  dependencies?: any[]; // Might want to add this in the future?
  downloadUrl: string;

  // Note that a lot of fields are missing
  // Check out: https://docs.curseforge.com/rest-api/?javascript#get-mod-files
}