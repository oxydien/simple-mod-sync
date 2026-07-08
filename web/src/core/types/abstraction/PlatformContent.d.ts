import ContentType from "../sms/ContentType";


export interface PlatformContent {
  icon_url: string;
  page_url: string;
  title: string;
  slug: string;
  summary: string;
  content_type: ContentType;

  /* This is for the ContentPlatform impl to store information between search results and retrieving files */
  platform_info: unknown;
}