import ModrinthProjectType from "./ModrinthProjectType";
import AnyGameVersion from "../GameVersion";

type ModrinthProjectSideState = "required" | "optional" | "unsupported" | "unknown";

export default interface ModrinthProject {
  project_id: string;
  slug: string;
  title: string;
  description: string;
  categories: string[];
  client_side: ModrinthProjectSideState;
  server_side: ModrinthProjectSideState;
  project_type: ModrinthProjectType;
  downloads: number;
  color: number;
  thread_id?: string;
  author: string;
  versions: AnyGameVersion[];
  follows: string;
  icon_url: string;
  date_created: string;
  date_modified: string;
  latest_version: AnyGameVersion;
  license: string;
  gallery: string[];
  featured_gallery: string;
}
