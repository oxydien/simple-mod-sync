import {ContentPlatformKey} from "../../core/platforms/ContentPlatformManager";
import {PlatformContent} from "../../core/types/abstraction/PlatformContent";
import PlatformFile from "../../core/types/abstraction/PlatformFile";
import SyncEnvironment from "../../core/types/SyncEnvironment";
import {createSignal, For, onMount, Show} from "solid-js";
import {sms} from "../../core/SchemaGenerator";
import PlatformContentWidget from "../content/PlatformContentWidget";
import Separator from "../common/Separator";
import LoadingMessage from "../info/LoadingMessage";
import InfoBox from "../info/InfoBox";
import PlatformFileWidget from "../content/PlatformFileWidget";
import ContentType from "../../core/types/sms/ContentType";

interface PlatformContentVersionsSectionProps {
  platformKey: ContentPlatformKey;
  content: PlatformContent;
  contentType: ContentType;
  environment: SyncEnvironment | null;
  onSelected?: (file: PlatformFile) => void;
}

export default function PlatformContentVersionsSection(props: PlatformContentVersionsSectionProps) {
  const [isLoading, setIsLoading] = createSignal<boolean>(false);
  const [files, setFiles] = createSignal<PlatformFile[]>([]);
  const [error, setError] = createSignal<string | null>(null);

  onMount(async () => {
    setError(null);
    setIsLoading(true);
    const platformManager = sms().getContentPlatforms();
    const platform = platformManager.getPlatform(props.platformKey)!;
    const res = await platform.getVersionsFor(props.content, props.contentType, props.environment);
    setIsLoading(false);
    if (res.data) {
      setFiles(res.data);
      return;
    }
    setError(res.error || null);
  });

  return <div class="flex flex-col gap-4 w-full">
    <PlatformContentWidget data={props.content} />

    <Separator>Files</Separator>
    <Show when={error() != null}>
      <InfoBox variant="warning">
        <p>
          <strong><u class="decoration-dashed">Failed to find versions!</u></strong> <br />
          {error()}
        </p>
      </InfoBox>
    </Show>

    <Show when={!isLoading()} fallback={<LoadingMessage message="Fetching mod versions..." />}>
      <div class="flex flex-col gap-2">
        <For each={files()} fallback={"Nothing found :?"}>
          {(file: PlatformFile) =>
            <PlatformFileWidget file={file} onSelected={() => props.onSelected && props.onSelected(file)} />
          }
        </For>
      </div>
    </Show>
  </div>
}