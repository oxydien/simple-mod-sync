import {createEffect, createMemo, createSignal, For, on, Show} from "solid-js";
import Input from "../common/Input";
import {PlatformContent} from "../../core/types/abstraction/PlatformContent";
import PlatformContentWidget from "../content/PlatformContentWidget";
import InfoBox from "../info/InfoBox";
import {ContentPlatformKey} from "../../core/platforms/ContentPlatformManager";
import {sms} from "../../core/SchemaGenerator";
import PaginatedData from "../../core/types/PaginatedData";
import Pagination from "../common/Pagination";
import LoadingMessage from "../info/LoadingMessage";
import SyncEnvironment from "../../core/types/SyncEnvironment";
import ContentType from "../../core/types/sms/ContentType";

const CONTENT_SEARCH_DELAY_MS = 600;

interface PlatformContentSearchSectionProps {
  platformKey: ContentPlatformKey;
  env: SyncEnvironment | null;
  contentType: ContentType;
  onAdd: (content: PlatformContent) => void;
}

export default function PlatformContentSearchSection(props: PlatformContentSearchSectionProps) {
  const [query, setQuery] = createSignal<string>("");
  const [entries, setEntries] = createSignal<PaginatedData<PlatformContent> | null>(null);
  const [page, setPage] = createSignal(0);

  const [isSearching, setIsSearching] = createSignal<boolean>(false);
  const [error, setError] = createSignal<string | null>(null);
  const [reSearchTimeout, setReSearchTimeout] = createSignal<number | null>(null);

  const platform = createMemo(() =>
    sms().getContentPlatforms().getPlatform(props.platformKey)
  );
  createEffect(
    on(() => platform(), () => {
      setPage(0);
      setEntries(null);
      setError(null);
      setIsSearching(true)
      search();
    })
  );
  createEffect(
    on(() => props.contentType, () => {
      setPage(0);
      setEntries(null);
      setError(null);
      setIsSearching(true)
      search();
    })
  )

  if (!platform()) {
    return (
      <InfoBox variant="warning">
        <p>
          Failed to load platform '{props.platformKey}'. <br/>
          This platform is not registered in global context.
        </p>
      </InfoBox>
    )
  }

  const handleInputChange = (value: string) => {
    setPage(0);
    setQuery(value);
    search();
  }

  const handlePageChange = (page: number) => {
    setPage(page - 1);
    setIsSearching(true);
    search();
  }

  const search = () => {
    let timeoutId = reSearchTimeout();
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    timeoutId = setTimeout(() => {
      fetchContent()
    }, CONTENT_SEARCH_DELAY_MS)
    setReSearchTimeout(timeoutId)
  }

  const fetchContent = async () => {
    setIsSearching(true);
    setError(null);
    const res = await platform()!.search(query(), props.contentType, props.env, page());
    setIsSearching(false);
    if (res.data) {
      setEntries(res.data);
      return;
    }
    setError(res.error || null);
  }

  const handleAdd = (entry: PlatformContent) => {
    props.onAdd(entry);
  }

  return <div class="flex flex-col gap-2 w-full">
    <Input
      placeholder={"Search content here..."}
      value={query()}
      onChange={handleInputChange}
    />
    <div class={"sect flex flex-col gap-1 w-full"}>
      <Show when={isSearching()}>
        <LoadingMessage message="Searching..." />
      </Show>
      <Show when={!isSearching()}>
        <Show when={error() != null}>
          <InfoBox variant="warning">
            <p>
              <strong><u class="decoration-dashed">Failed to search!</u></strong> <br />
              {error()}
            </p>
          </InfoBox>
        </Show>
        <Show when={entries() == null}>
          <p class="text-center">
            There is nothing here...<br /> Try searching something...
          </p>
        </Show>
        <Show when={entries() != null}>
          <For each={entries()!.data}>
            {(entry: PlatformContent) => <PlatformContentWidget data={entry} onAdd={() => handleAdd(entry)} />}
          </For>
          <Show when={entries()!.data.length === 0}>
            <p>
              Nothing found...
            </p>
          </Show>
          <Pagination
            currentPage={entries()!.currentPage + 1}
            totalPages={entries()!.totalPages}
            onPageChange={handlePageChange}
          />
        </Show>
      </Show>
    </div>
  </div>
}