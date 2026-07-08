import CurseForgeProject from "./CurseForgeProject";
import CurseForgePagination from "./CurseForgePagination";

export default interface CurseForgeSearchResponse {
  data: CurseForgeProject[];
  pagination: CurseForgePagination;
}