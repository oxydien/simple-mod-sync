import ModrinthError from "./ModrinthError";

type ModrinthResponse<T> = T | ModrinthError;

export default ModrinthResponse;
