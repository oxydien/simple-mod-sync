import Button from "../common/Button";
import PlatformFile from "../../core/types/abstraction/PlatformFile";


interface PlatformFileWidgetProps {
  file: PlatformFile;
  onSelected: () => void;
}

export default function PlatformFileWidget(props: PlatformFileWidgetProps) {

  return <article class="flex flex-col border-sep p-2">
      <div class="flex flex-col lg:flex-row items-center justify-between">
        <div class="flex flex-col gap-1">
          <strong class="wrap-anywhere">{props.file.name}</strong>
          <small class="max-w-[70vw] wrap-anywhere">
            {props.file.file_url || <em>EMPTY URL</em>}
          </small>
        </div>

        <nav class="flex flex-col lg:flex-row gap-1 w-full lg:w-[initial] mt-4 lg:mt-0">
          <Button variant="primary" onClick={() => props.onSelected()}>
            Add this one
          </Button>
        </nav>
      </div>
    </article>;
}