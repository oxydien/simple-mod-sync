import {PlatformContent} from "../../core/types/abstraction/PlatformContent";
import Button from "../common/Button";
import {Show} from "solid-js";


interface PlatformContentWidgetProps {
  data: PlatformContent;
  onAdd?: () => void;
}

export default function PlatformContentWidget(props: PlatformContentWidgetProps) {
  return (
    <article class="flex flex-col lg:flex-row items-center justify-between border-sep py-1 pl-1 pr-3">
      <div class="flex flex-col lg:flex-row items-center justify-between gap-1">
        <div>
          <img src={props.data.icon_url} alt="ICON" class="w-20 h-20 select-none p-2" />
        </div>

        <div class="flex flex-col gap-2">
          <div class="flex flex-row flex-wrap gap-1">
            <strong>
              {props.data.title}
            </strong>
            •
            <span class="wrap-anywhere">
              {props.data.slug}
            </span>
          </div>

          <small class="max-w-120 wrap-anywhere">
            {props.data.summary}
          </small>
        </div>
      </div>

      <nav class="flex flex-col lg:flex-row gap-1 w-full lg:w-[initial] mt-4 lg:mt-0">
        <Button link={props.data.page_url} target={"_blank"}>
          Visit
        </Button>
        <Show when={props.onAdd}>
          <Button variant="primary" onClick={() => {props.onAdd && props.onAdd()}}>
            Add
          </Button>
        </Show>
      </nav>
    </article>
  )
}