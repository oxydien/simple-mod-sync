import {createSignal, Show} from "solid-js";
import Icon from "./Icon";


interface InputProps {
  placeholder?: string;
  value?: string;
  onChange?: (value: string) => void;
  onBlur?: () => void;
  icon?: string,
  class?: string,
  noLimit?: boolean,
}


export default function Input(props: InputProps) {
  const [caretOffset, setCaretOffset] = createSignal(props?.value?.length ?? 0);

  const updateCaret = (e: Event) => {
    setTimeout(() => {
      setCaretOffset((e.target as HTMLInputElement).selectionStart ?? caretOffset())
    }, 30)
  }

  const inputEl = <input
    placeholder={props.placeholder}
    value={props.value || ""}
    onInput={(e) => props.onChange && props.onChange(e.target.value)}
    onKeyDown={updateCaret}
    onClick={updateCaret}
    onBlur={props.onBlur}
    maxlength={props.noLimit ? 9999 : 40}
    class="font-mono"
  />

  const handleWrapperClick = (_: MouseEvent) => {
    (inputEl as HTMLInputElement).focus();
  }

  return <div
    onClick={handleWrapperClick}
    class={`input-wrapper flex font-mono flex-row items-center ${props.noLimit ? "input-wrapper-no-caret" : ""} ${props.class || ""}`}
    style={{
      "--_caret-offset": caretOffset()
    }}
  >
    <Show when={props.icon}>
      <Icon name={props.icon as string}/>
    </Show>
    {inputEl}
  </div>
}