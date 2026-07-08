import AnyGameVersion from "../GameVersion";
import LoaderType from "../LoaderType";

type ModrinthFileType = "required-resource-pack" | "optional-resource-pack" | "sources-jar" | "dev-jar" | "javadoc-jar" | "unknown" | "signature" | null;
type ModrinthVersionType = "release" | "beta" | "alpha";

interface ModrinthFileHashes {
  sha512?: string,
  sha1?: string,
}

interface ModrinthFile {
  hashes: ModrinthFileHashes;
  url: string;
  filename: ModrinthFile;
  primary: boolean;
  size: number;
  file_type?: ModrinthFileType;
}


interface ModrinthVersion {
  name?: string;
  version_number?: string;
  changelog?: string | null;
  dependencies?: any[]; // Might want to add this in the future?
  game_versions?: AnyGameVersion[];
  version_type?: ModrinthVersionType;
  loaders?: LoaderType[];
  featured?: boolean;
  status?: string;
  requested_status?: string;

  id: string;
  project_id: string;
  author_id: string;
  date_published: string;
  downloads: number;
  files: ModrinthFile[]
}