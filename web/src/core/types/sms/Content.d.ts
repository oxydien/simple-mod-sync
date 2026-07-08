import ContentType from "./ContentType";

export default interface Content {
  url: string;
  type?: ContentType;
  name: string;
  version?: string;
  directory?: string | null;
}
