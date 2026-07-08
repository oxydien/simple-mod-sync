import CurseForgePagination from "./CurseForgePagination";
import CurseForgeFile from "./CurseForgeFile";

export default interface CurseForgeSearchResponse {
  data: CurseForgeFile[];
  pagination: CurseForgePagination;
}