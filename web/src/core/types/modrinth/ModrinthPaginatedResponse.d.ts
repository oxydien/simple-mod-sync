
export default interface ModrinthPaginatedResponse<T> {
  hits: T[];
  limit: number;
  offset: number;
  total_hits: number;
}
