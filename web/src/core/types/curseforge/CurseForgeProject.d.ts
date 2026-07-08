import CurseForgeModLinks from "./CurseForgeModLinks";
import CurseForgeModLogo from "./CurseForgeProjectIcon";

export type CurseForgeCategory = Record<string, unknown>;
export type CurseForgeAuthor = Record<string, unknown>;
export type CurseForgeFile = Record<string, unknown>;
export type CurseForgeFileIndex = Record<string, unknown>;
export type CurseForgeAsset = Record<string, unknown>;

export default interface CurseForgeProject {
  screenshots: CurseForgeAsset[];
  id: number;
  gameId: number;
  name: string;
  slug: string;
  links: CurseForgeModLinks;
  summary: string;
  status: number;
  downloadCount: number;
  isFeatured: boolean;
  primaryCategoryId: number;
  categories: CurseForgeCategory[];
  classId: number;
  authors: CurseForgeAuthor[];
  logo: CurseForgeModLogo | null;
  mainFileId: number;
  latestFiles: CurseForgeFile[];
  latestFilesIndexes: CurseForgeFileIndex[];
  latestEarlyAccessFilesIndexes: CurseForgeFileIndex[];
  dateCreated: string;
  dateModified: string;
  dateReleased: string;
  allowModDistribution: boolean | null;
  gamePopularityRank: number;
  isAvailable: boolean;
  hasCommentsEnabled: boolean;
  thumbsUpCount: number;
  featuredProjectTag?: string;
}