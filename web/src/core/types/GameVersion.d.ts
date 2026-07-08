
// @ts-ignore Circular reference
type DotNums = `${number}` | `${number}.${DotNums}`;
type AnyGameVersion = DotNums | `${DotNums}-${string}`;

export default AnyGameVersion;
export { DotNums };
