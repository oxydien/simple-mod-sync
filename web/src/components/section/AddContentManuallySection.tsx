import InfoBox from "../info/InfoBox";
import Important from "../info/Important";
import ContentWidget from "../content/ContentWidget";
import ExtendedContent from "../../core/types/sms/extensions/ExtendedContent";
import Button from "../common/Button";
import {createStore} from "solid-js/store";


interface AddContentManuallySectionProps {
  onAction: (action: string) => void;
  onInsert?: (entry: ExtendedContent) => void;
}

export default function AddContentManuallySection(props: AddContentManuallySectionProps) {
  const [entry, setEntry] = createStore<ExtendedContent>({
    name: "",
    url: "",
    type: "mod",
    version: "1.0.0",
  });


  const handleFieldChange = (
    field: "url" | "name" | "version" | "directory" | "type",
    value: string | undefined
  ) => {
    setEntry(field as any, value as any);
  };

  return (
    <div class="flex flex-col gap-4">
      <Important>
        Add new content
      </Important>
      <InfoBox>
        <p>
          You will be able to modify this later.
        </p>
      </InfoBox>
      <ContentWidget
        entry={entry}
        expanded={true}
        onFieldChange={handleFieldChange}
      />
      <nav class="flex flex-col lg:flex-row gap-2">
        <Button onClick={() => props.onAction("")}>
          Cancel
        </Button>
        <Button
          variant="primary"
          onClick={() => props.onInsert && props.onInsert(entry)}
        >
          Save
        </Button>
      </nav>
    </div>
  );
}