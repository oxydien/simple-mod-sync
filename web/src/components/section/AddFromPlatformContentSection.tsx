import {createSignal, Show} from "solid-js";
import {ContentPlatformKey} from "../../core/platforms/ContentPlatformManager";
import SelectPlatformSection from "./SelectPlatformSection";
import PlatformContentSearchSection from "./PlatformContentSearchSection";
import SyncEnvironment from "../../core/types/SyncEnvironment";
import {PlatformContent} from "../../core/types/abstraction/PlatformContent";
import PlatformContentVersionsSection from "./PlatformContentVersionsSection";
import PlatformFile from "../../core/types/abstraction/PlatformFile";
import ExtendedContent from "../../core/types/sms/extensions/ExtendedContent";
import ContentType from "../../core/types/sms/ContentType";
import PlatformSelectContentTypeSection from "./PlatformSelectContentTypeSection";


interface AddContentFromPlatformSectionProps {
  env: SyncEnvironment | null;
  onInsert?: (content: ExtendedContent) => void;
}

export default function AddContentFromPlatformSection(props: AddContentFromPlatformSectionProps) {
  const [platform, setPlatform] = createSignal<ContentPlatformKey>();
  const [contentType, setContentType] = createSignal<ContentType>("mod");
  const [content, setContent] = createSignal<PlatformContent | null>(null);

  const handleContentSelected = (content: PlatformContent) => {
    setContent(content);
  }

  const handleFileSelected = (file: PlatformFile) => {
    if (!props.onInsert) return;
    const extContent: ExtendedContent = {
      $platform_data: "", // TODO: prompt content platform to save its data to string
      name: content()!.slug,
      type: contentType(),
      url: file.file_url,
      version: file.version
    }
    props.onInsert(extContent);
  }

  return <div class="flex flex-wrap gap-2 justify-stretch">
    <Show when={!content()}>
      <SelectPlatformSection value={platform()} onChange={setPlatform} />
      <PlatformSelectContentTypeSection value={contentType()} onChange={setContentType} />
      <Show when={platform()}>
        <PlatformContentSearchSection
          platformKey={platform()!}
          env={props.env}
          contentType={contentType()}
          onAdd={handleContentSelected}
        />
      </Show>
    </Show>

    <Show when={content() && platform()}>
      <PlatformContentVersionsSection
        platformKey={platform()!}
        content={content()!}
        contentType={contentType()}
        environment={props.env}
        onSelected={handleFileSelected}
      />
    </Show>
  </div>
}