import ExtendedSyncSchema from "../../core/types/sms/extensions/ExtendedSyncSchema";
import {createSignal, For} from "solid-js";
import ContentWidget from "../content/ContentWidget";
import Separator from "../common/Separator";
import Button from "../common/Button";
import ExtendedContent from "../../core/types/sms/extensions/ExtendedContent";


interface ContentListSectionProps {
  file: ExtendedSyncSchema,
  onAction: (action: string) => void,
  onEntryFieldChange: (index: number, field: keyof ExtendedContent, value: string | undefined) => void;
}

export default function ContentListSection(props: ContentListSectionProps) {
  const [expanded, setExpanded] = createSignal<number[]>([]);

  const handleToggleExpanded = (idx: number) => {
    const arr = [...expanded()];
    const index = arr.indexOf(idx);
    if (index > -1) {
      arr.splice(index, 1);
      setExpanded(arr);
      return;
    }
    arr.push(idx);
    setExpanded(arr);
  }

  return <div class="flex flex-col gap-2">
    <Separator>
      Content
    </Separator>

    <nav class="flex flex-wrap items-center justify-end gap-4">
      <div class="flex flex-wrap items-center gap-2">
        <Button
          class="w-[initial]!"
          onClick={() => props.onAction("add")}
        >
          Add manually
        </Button>
        <Button
          class="w-[initial]!"
          variant="primary"
          onClick={() => props.onAction("add_platform")}
        >
          Add from platform
        </Button>
      </div>
    </nav>

    <div class="sect flex flex-col gap-2">
      <For each={props.file.sync} fallback={<p>There is nothing here...</p>}>
        {(entry, idx) => <ContentWidget
          entry={entry}
          expanded={expanded().includes(idx())}
          onExpand={() => handleToggleExpanded(idx())}
          onFieldChange={(field, value) => props.onEntryFieldChange(idx(), field, value)}
        />}
      </For>
    </div>
  </div>
}