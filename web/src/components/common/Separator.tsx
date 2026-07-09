import {JSX} from "solid-js";


interface SeparatorProps {
  children?: JSX.Element,
  class?: string,
}

export default function Separator(props: SeparatorProps) {
  return <div class={`my-6 flex px-2 w-full justify-center items-center ${props.class || ""}`}>
    <img
      src="/assets/separator.png"
      class="mr-2 w-fit h-6 select-none"
      alt="------------"
      aria-hidden
    />
    <strong class="min-w-40 text-center">
      {props.children}
    </strong>
    <img
      src="/assets/separator.png"
      class="ml-2 w-fit h-6 -scale-x-100 select-none"
      alt="------------"
      aria-hidden
    />
  </div>
}