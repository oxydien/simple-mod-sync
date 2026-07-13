import {JSX, mergeProps, Show, splitProps} from "solid-js";
import Icon from "./Icon";

interface ButtonProps {
  children?: JSX.Element;
  variant?: "default" | "primary" | "destructive";
  onClick?: () => void;
  disabled?: boolean;
  type?: "button" | "submit" | "reset";
  link?: string;
  target?: "_blank" | "_self" | "_parent" | "_top";
  class?: string;
}

export default function Button(passedProps: ButtonProps) {
  const merged = mergeProps({ variant: "default", class: "" }, passedProps);
  const [local, rest] = splitProps(merged, ["children", "class", "variant", "link"]);
  const isExternal = () => local.link?.startsWith("http://") || local.link?.startsWith("https://");

  return (
    <Show
      when={local.link}
      fallback={
        <button class={`btn btn-${local.variant} ${local.class}`} {...rest}>
          {local.children}
        </button>
      }
    >
      <a
        href={local.link}
        rel="noopener noreferrer"
        class={`btn btn-${local.variant} ${local.class}`}
        {...rest}
      >
        {local.children}
        <Show when={isExternal()}>
          <Icon name="external" />
        </Show>
      </a>
    </Show>
  );
}
