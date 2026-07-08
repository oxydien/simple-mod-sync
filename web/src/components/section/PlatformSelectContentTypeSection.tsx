import ContentType from "../../core/types/sms/ContentType";
import {For} from "solid-js";
import Button from "../common/Button";
import Icon from "../common/Icon";

interface PlatformSelectContentTypeSectionProps {
  value: ContentType;
  onChange?: (value: ContentType) => void;
}

const CONTENT_TYPES: Record<string, ContentType> = {
  "Mods": "mod",
  "Resource packs": "resourcepack",
  "Shaders": "shader",
  "Datapacks": "datapack",
}

export default function PlatformSelectContentTypeSection(props: PlatformSelectContentTypeSectionProps) {

  return <div class="flex flex-wrap gap-1 w-full">
    <For each={Object.entries(CONTENT_TYPES)}>
      {(entry) =>
        <Button
          variant={entry[1] === props.value ? "primary" : "default"}
          onClick={() => props.onChange && props.onChange(entry[1])}
          class="grow w-[initial]!"
        >
          <Icon name={entry[1]} />
          {entry[0]}
        </Button>
      }
    </For>
  </div>
}