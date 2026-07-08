import {ContentPlatformKey} from "../../core/platforms/ContentPlatformManager";
import {createSignal, For, onMount, Show} from "solid-js";
import {ContentPlatform} from "../../core/platforms/ContentPlatform";
import LoadingMessage from "../info/LoadingMessage";
import Button from "../common/Button";
import Icon from "../common/Icon";
import {sms} from "../../core/SchemaGenerator";
import Important from "../info/Important";

let AVAILABLE_PLATFORMS_CACHE: ContentPlatformKey[] = [];
let HAS_FETCHED: boolean = false;

interface SelectPlatformSectionProps {
  value?: ContentPlatformKey;
  onChange: (value: ContentPlatformKey) => void;
}

interface ContentPlatformEntry {
  key: ContentPlatformKey;
  platform: ContentPlatform;
}

export default function SelectPlatformSection(props: SelectPlatformSectionProps) {
  const [isLoading, setIsLoading] = createSignal<boolean>(false);
  const [platforms, setPlatforms] = createSignal<ContentPlatformEntry[]>([]);

  const platformProvider = sms().getContentPlatforms();

  const loadFromCache = () => {
    const foundPlatforms: ContentPlatformEntry[] = [];
    for (let key of AVAILABLE_PLATFORMS_CACHE) {
      const plf = platformProvider.getPlatform(key);
      if (!plf) continue;
      foundPlatforms.push({
        key,
        platform: plf,
      });
    }
    setPlatforms(foundPlatforms);
  };

  onMount(() => {
    if (!HAS_FETCHED) {
      setIsLoading(true);
      HAS_FETCHED = true;

      platformProvider.getAvailable()
        .then(res => {
          console.log("Loaded content platforms: ", res);
          AVAILABLE_PLATFORMS_CACHE = res;
          loadFromCache();
        })
        .catch((err) => {
          console.error("Error while loading content platforms", err);
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      loadFromCache();
    }
  });

  return (
    <Show
      when={!isLoading()}
      fallback={<LoadingMessage message="Loading content platforms..." />}
    >
      <div class="flex flex-col gap-1 w-full">
        <Important>Select platform for searching:</Important>
        <div class="flex flex-wrap gap-2 w-full">
          <For each={platforms()}>
            {(entry: ContentPlatformEntry) => (
              <Button
                variant={props.value === entry.key ? "primary" : "default"}
                disabled={props.value === entry.key}
                onClick={() => { props.onChange(entry.key); }}
                class="grow w-[initial]!"
              >
                <Icon name={entry.platform.getIcon()} />
                {entry.platform.getName()}
              </Button>
            )}
          </For>
        </div>
      </div>
    </Show>
  );
}