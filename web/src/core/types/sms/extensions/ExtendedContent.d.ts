import Content from "../Content";


export default interface ExtendedContent extends Content {
  $platform_data?: string,
}