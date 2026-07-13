import {VersionWrapper} from "../../core/types/VersionWrapper";
import {createMemo, createSignal, For, onMount, Show} from "solid-js";
import {sms} from "../../core/SchemaGenerator";
import LoadingMessage from "../info/LoadingMessage";
import LoaderType from "../../core/types/LoaderType";
import AnyGameVersion from "../../core/types/GameVersion";
import Important from "../info/Important";
import Button from "../common/Button";
import SyncEnvironment from "../../core/types/SyncEnvironment";
import {debug, info} from "../../core/log";
import Icon from "../common/Icon";


const LOADER_TYPES: LoaderType[] = [
  "fabric",
  "quilt",
  "neoforge"
];

interface SelectEnvironmentSectionProps {
  onEnvironment: (SyncEnvironment: SyncEnvironment | null) => void;
}

export default function SelectEnvironmentSection(props: SelectEnvironmentSectionProps) {
  const [isLoading, setIsLoading] = createSignal<boolean>(false);
  const [showSnapshots, setShowSnapshots] = createSignal<boolean>(false); // not mounted to anything
  const [versions, setVersions] = createSignal<VersionWrapper[]>([]);

  const [selectedVersion, setSelectedVersion] = createSignal<AnyGameVersion>();
  const [selectedLoader, setSelectedLoader] = createSignal<LoaderType>(LOADER_TYPES[0]);

  const showVersionsWrappers = createMemo((): VersionWrapper[] => {
    return versions().map((version) => {
      if (showSnapshots()) return version;
      if (version.type === "release") return version;
      return null;
    }).filter((f) => f != null);
  });
  const showVersions = createMemo((): AnyGameVersion[] => showVersionsWrappers().map((version) => version.version));

  onMount(async () => {
    setIsLoading(true);
    info("Load versions", "Making request");
    const res = await sms().getVersions();
    setIsLoading(false);
    if (res.data && res.data.length > 0) {
      info("Load versions", "Loaded versions:", res.data.length);
      setVersions(res.data);
      if (showVersionsWrappers().length > 0) {
        debug("Load versions", "Selecting default version:", showVersionsWrappers()[0]);
        setSelectedVersion(showVersionsWrappers()[0].version)
      } else {
        setSelectedVersion(null);
      }
      return;
    }
  });

  const handleSkip = () => {
    props.onEnvironment(null);
  }

  const handleSave = () => {
    props.onEnvironment({
      mc_loader: selectedLoader(),
      mc_version: selectedVersion()
    })
  }

  return (<div class="flex flex-col gap-2">
    <Important>Select modpack environment</Important>
    <Show when={!isLoading()} fallback={<LoadingMessage message="Loading minecraft versions..." />}>
      <div class="sect flex flex-col gap-4">
        <div class="flex flex-col">
          <strong>
            Select minecraft version:
          </strong>
          <select
            class="border-sep p-2"
            onChange={(e) => setSelectedVersion(e.target.value as AnyGameVersion)}
            value={selectedVersion()}
          >
            <For each={showVersions()}>
              {(ver) => <option value={ver}>{ver}</option>}
            </For>
          </select>
          <small class="mt-1">
            <strong>Note:</strong> Simple Mod Sync might not have a release version for all these minecraft versions.
          </small>
        </div>
        <div class="flex flex-col">
          <strong>
            Select mod loader:
          </strong>
          <select
            class="border-sep p-2"
            onChange={(e) => setSelectedLoader(e.target.value as LoaderType)}
          >
            <For each={LOADER_TYPES}>
              {(loader) => <option value={loader}>{loader}</option>}
            </For>
          </select>
        </div>

        <nav class="flex flex-col md:flex-row gap-2">
          <Button
              class="skip"
              variant="default"
              onClick={handleSkip}
          >
            Skip
          </Button>
          <Button
              class="save"
              variant="primary"
              disabled={!showVersions()}
              onClick={handleSave}
          >
            Continue
          </Button>
        </nav>
      </div>
    </Show>
  </div>)
}