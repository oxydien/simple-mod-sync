type ModrinthFacetKey = string | "categories" | "versions" | "project_type";
type ModrinthFacetValue = string;
type ModrinthFacetToken = `${ModrinthFacetKey}:${ModrinthFacetValue}`;
type ModrinthFacetPair = readonly [ModrinthFacetToken];
type ModrinthFacets = readonly ModrinthFacetPair[];

export default ModrinthFacets;
export { ModrinthFacetPair };
