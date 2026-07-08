import ModificationType from "./ModificationType";

export default interface Modification {
  type: ModificationType;
  pattern: string;
  path: string;
}
