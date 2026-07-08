
export interface ResultData<T> {
  data?: T | null;
  error?: string;
  error_internal?: boolean;
}