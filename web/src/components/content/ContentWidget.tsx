import {createEffect, createMemo, createSignal, For, Show} from "solid-js";
import Content from "../../core/types/sms/Content";
import ExtendedContent from "../../core/types/sms/extensions/ExtendedContent";
import Button from "../common/Button";
import Icon from "../common/Icon";
import Input from "../common/Input";
import ContentType from "../../core/types/sms/ContentType";

const CONTENT_TYPES: ContentType[] = [
  "mod",
  "resourcepack",
  "shader",
  "config",
  "packed",
  "datapack",
];

interface ContentWidgetProps {
  entry: Content | ExtendedContent;
  expanded: boolean;
  onExpand?: () => void;
  onRemove?: () => void;
  onFieldChange(field: "url" | "name", value: string): void;
  onFieldChange(field: "version" | "directory", value: string | undefined): void;
  onFieldChange(field: "type", value: ContentType): void;
}

export default function ContentWidget(props: ContentWidgetProps) {
  const [name, setName] = createSignal(props.entry.name);
  const [url, setUrl] = createSignal(props.entry.url);
  const [version, setVersion] = createSignal(props.entry.version ?? "");
  const [directory, setDirectory] = createSignal(props.entry.directory ?? "");

  createEffect(() => {
    setName(props.entry.name);
  });
  createEffect(() => {
    setUrl(props.entry.url);
  });
  createEffect(() => {
    setVersion(props.entry.version ?? "");
  });
  createEffect(() => {
    setDirectory(props.entry.directory ?? "");
  });

  const resolvedType = createMemo<ContentType>(() => props.entry.type ?? "mod");

  const showsDirectory = createMemo(
    () => resolvedType() === "config" || resolvedType() === "packed"
  );

  const isUrlValid = createMemo(() => {
    try {
      new URL(url());
      return true;
    } catch {
      return false;
    }
  });

  const handleTypeChange = (e: Event) => {
    const value = (e.currentTarget as HTMLSelectElement).value as ContentType;
    props.onFieldChange("type", value);
  };

  return (
    <article class="flex flex-col border-sep p-2">
      <div class="flex flex-col lg:flex-row items-center justify-between">
        <div class="flex flex-col gap-1">
          <div class="flex flex-row gap-1 items-center">
            <Icon name={props.entry.type || "mod"} />
            <strong class="wrap-anywhere">{props.entry.name}</strong>•
            <em class="wrap-anywhere">
              {props.entry.version || "unknown version"}
            </em>
          </div>
          <small class="max-w-[70vw] wrap-anywhere">
            {props.entry.url || <em>EMPTY URL</em>}
          </small>
        </div>

        <nav class="flex flex-col lg:flex-row gap-1 w-full lg:w-[initial] mt-4 lg:mt-0">
          <Show when={props.onExpand}>
            <Button variant="primary" onClick={() => props.onExpand && props.onExpand()}>
              {props.expanded ? "Collapse" : "Expand"}
            </Button>
          </Show>
        </nav>
      </div>

      <Show when={props.expanded}>
        <div class="flex flex-col gap-2 mt-4 border-t-sep">

          <label class="flex flex-col gap-1">
            <span>Type</span>
            <select
              class="border-sep p-2"
              value={resolvedType()}
              onChange={handleTypeChange}
            >
              <For each={CONTENT_TYPES}>
                {(type) => <option value={type}>{type}</option>}
              </For>
            </select>
          </label>

          <label class="flex flex-col gap-1">
            <span>Name</span>
            <Input
              value={name()}
              onChange={setName}
              onBlur={() => props.onFieldChange("name", name())}
            />
            <Show when={name() === ""}>
              <small class="text-warning">This should not be empty</small>
            </Show>
          </label>

          <label class="flex flex-col gap-1">
            <span>URL</span>
            <Input
              value={url()}
              onChange={setUrl}
              onBlur={() => props.onFieldChange("url", url())}
              noLimit
            />
            <Show when={!isUrlValid()}>
              <small class="text-warning">This does not look like a valid URL</small>
            </Show>
          </label>

          <label class="flex flex-col gap-1">
            <span>Version</span>
            <Input
              value={version()}
              onChange={setVersion}
              onBlur={() =>
                props.onFieldChange(
                  "version",
                  version() === "" ? undefined : version()
                )
              }
            />
          </label>

          <Show when={showsDirectory()}>
            <label class="flex flex-col gap-1">
              <span>Directory</span>
              <Input
                value={directory()}
                onChange={setDirectory}
                onBlur={() =>
                  props.onFieldChange(
                    "directory",
                    directory() === "" ? undefined : directory()
                  )
                }
              />
            </label>
          </Show>

          <Show when={props.onRemove}>
            <div>
              <p class="leading-5 text-sm my-2">
                This button removes the entry from the sync file, the content might still be present on clients computers.<br />
                If you wish to remove the files from the clients computer, leave the url empty or create a modify entry.
              </p>
              <Button
                  variant="destructive"
                  onClick={props.onRemove}
              >
                Remove
              </Button>
            </div>
          </Show>
        </div>
      </Show>
    </article>
  );
}