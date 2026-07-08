import {createSignal, Match, Switch} from "solid-js";
import GeneratorStartSection from "../components/section/GeneratorStartSection";
import ExtendedSyncSchema from "../core/types/sms/extensions/ExtendedSyncSchema";
import AddContentFromPlatformSection from "../components/section/AddFromPlatformContentSection";
import ContentListSection from "../components/section/ContentListSection";
import AddContentManuallySection from "../components/section/AddContentManuallySection";
import Button from "../components/common/Button";
import ExtendedContent from "../core/types/sms/extensions/ExtendedContent";
import ExportSchemaSection from "../components/section/ExportSchemaSection";


export default function GeneratorPage() {
  const [action, setAction] = createSignal("");

  const [file, setFile] = createSignal<ExtendedSyncSchema | null>(null);

  let exportSectRef: HTMLDivElement | null = null;

  const handleInsertContent = (content: ExtendedContent) => {
    setFile(prev => ({...prev!, sync: [content, ...prev!.sync]}))
  }

  const handleInsertAction = (content: ExtendedContent) => {
    handleInsertContent(content);
    setAction("");
  }


  const handleEntryFieldChange = (index: number, field: keyof ExtendedContent, value: string | undefined) => {
    setFile(prev => {
      if (!prev) return prev;

      return {
        ...prev,
        sync: prev.sync.map((entry, i) =>
          i === index ? { ...entry, [field]: value } : entry
        )
      };
    });
  };


  const handleSetAction = (act: string) => {
    setAction(act);
  }

  return (<div id="generatorPage" class="flex flex-col gap-4 my-8">
    <Switch>
      <Match when={file() == null}>
        <GeneratorStartSection onLoad={setFile} />
      </Match>

      <Match when={action() === "add"}>
        <AddContentManuallySection
          onAction={handleSetAction}
          onInsert={handleInsertAction}
        />
      </Match>

      <Match when={action() === "add_platform"}>
        <Button onClick={() => handleSetAction("")}>
          Back to content list
        </Button>
        <AddContentFromPlatformSection
          env={file()!.environment!}
          onInsert={handleInsertAction}
        />
      </Match>

      <Match when={true}>
        <ContentListSection
          file={file()!}
          onAction={handleSetAction}
          onEntryFieldChange={handleEntryFieldChange}
        />
        <ExportSchemaSection schema={file()!} ref={exportSectRef} />
      </Match>
    </Switch>
  </div>)
}