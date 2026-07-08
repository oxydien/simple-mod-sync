import ExtendedSyncSchema from "../../core/types/sms/extensions/ExtendedSyncSchema";
import Button from "../common/Button";
import Separator from "../common/Separator";
import {createMemo, createSignal} from "solid-js";

interface ExportSchemaSectionProps {
  schema: ExtendedSyncSchema
}

export default function ExportSchemaSection(props: ExportSchemaSectionProps) {

  const strVersion = createMemo(() => {
    return JSON.stringify(props.schema, null, 2);
  })

  const [copyLabel, setCopyLabel] = createSignal("Copy to clipboard");
  const [error, setError] = createSignal<string | null>(null);

  let copyResetTimeout: ReturnType<typeof setTimeout> | undefined;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(strVersion());
      setError(null);
      setCopyLabel("Copied!");

      if (copyResetTimeout) clearTimeout(copyResetTimeout);
      copyResetTimeout = setTimeout(() => {
        setCopyLabel("Copy to clipboard");
      }, 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to copy to clipboard");
    }
  };

  const handleDownload = () => {
    try {
      const blob = new Blob([strVersion()], { type: "application/json" });
      const url = URL.createObjectURL(blob);

      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "sync_schema.json";
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);

      URL.revokeObjectURL(url);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to download file");
    }
  };

  return (
    <section class="flex flex-col gap-4 my-8">
      <Separator>Export</Separator>

      <div class="sect flex flex-wrap gap-2 justify-center">
        <Button
          onClick={handleCopy}
          class="grow w-[initial]!"
          variant="primary"
        >
          {copyLabel()}
        </Button>
        <Button
          onClick={handleDownload}
          class="grow w-[initial]!"
        >
          Download as file
        </Button>
      </div>
    </section>
  )
}