import {createSignal, Match, Show, Switch} from "solid-js";
import Important from "../info/Important";
import Button from "../common/Button";
import {sms} from "../../core/SchemaGenerator";
import InfoBox from "../info/InfoBox";
import LoadingMessage from "../info/LoadingMessage";
import ExtendedSyncSchema from "../../core/types/sms/extensions/ExtendedSyncSchema";
import SelectEnvironmentSection from "./SelectEnvironmentSection";
import SyncEnvironment from "../../core/types/SyncEnvironment";

interface GeneratorStartSectionProps {
  onLoad: (file: ExtendedSyncSchema) => void;
}

export default function GeneratorStartSection(props: GeneratorStartSectionProps) {
  const [isParsing, setIsParsing] = createSignal(false);
  const [parseError, setParseError] = createSignal<string | null>(null);
  const [schema, setSchema] = createSignal<ExtendedSyncSchema | null>(null);

  const [showEnv, setShowEnv] = createSignal(false);

  const parsers = sms().getParsers();

  async function parseFile(file: File) {
    const smsParser = parsers.getParser("sms");
    if (!smsParser) {
      setParseError("No parser found.");
      return;
    }
    setParseError(null);
    setIsParsing(true);
    const res = await smsParser.parseFile(file);
    setIsParsing(false);
    if (!res.data) {
      setParseError(res.error || null);
      return;
    }

    console.log("Parsing succeeded", res.data);
    if (!("environment" in res.data)) {
      setShowEnv(true);
      setSchema(res.data);
      return;
    }

    props.onLoad(res.data);
  }

  const handleSelectFile = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (!file) return;
    parseFile(file);
  }

  const handleCreateFile = () => {
    setShowEnv(true);
  }

  const handleEnvSelected = (env: SyncEnvironment) => {
    let res: ExtendedSyncSchema;
    if (schema()) {
      res = { ...schema()!, environment: env }
    } else {
      res = sms().createDefault(env);
    }
    props.onLoad(res);
  }

  return (
    <div class="max-w-120 mx-auto my-40">

      <h3 class="text-primary text-xl text-center font-extrabold">
        Create or Import
      </h3>


      <Switch>
        <Match when={showEnv()}>
          <SelectEnvironmentSection onEnvironment={handleEnvSelected} />
        </Match>
        <Match when={true}>
          <section
            class="sect flex flex-col gap-4"
            id="fileImportDrop"
          >
            <Show when={parseError() != null} >
              <InfoBox variant="warning">
                <p>
                  <Important>Failed to import the file</Important> <br />
                  {parseError()!}
                </p>
              </InfoBox>
            </Show>

            <Show when={!isParsing()} fallback={<LoadingMessage />}>
              <label
                for="bigFileInput"
                class="block border-4 border-dashed text-center p-8"
                onDragOver={(e) => e.preventDefault()}
                onDragEnter={(e) => e.preventDefault()}
                onDrop={(e) => {
                  e.preventDefault();
                  const files = e.dataTransfer?.files ?? [];
                  if (files.length > 0) {
                    const event = new Event('change', { bubbles: true });
                    Object.defineProperty(event, 'target', {
                      value: { files: files },
                      enumerable: true,
                    });
                    handleSelectFile(event);
                  }
                }}
              >
                <Important>
                  Drag or Select a file for import
                </Important>
                <input
                  type="file"
                  id="bigFileInput"
                  class="hidden"
                  onChange={handleSelectFile}
                />
              </label>
            </Show>

            <Button
              variant="primary"
              onClick={handleCreateFile}
            >
              Create a new file
            </Button>
          </section>
        </Match>
      </Switch>
    </div>
  )
}