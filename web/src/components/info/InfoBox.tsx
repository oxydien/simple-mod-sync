import {JSX, Show} from "solid-js";
import Icon from "../common/Icon";

export interface InfoBoxProps {
  class?: string;
  variant?: "info" | "warning";
  children?: JSX.Element;
}

export default function InfoBox({...props}: InfoBoxProps) {
  const variant = props.variant || "info";

  return <div class={`info-box info-box-${variant} ${props.class || ""}`}>
    <Show when={variant === "info"}>
      <Icon name="info" class="info-box-icon" />
    </Show>
    <Show when={variant === "warning"}>
      <Icon name="warning" class="info-box-icon" />
    </Show>
    {props.children}
  </div>
}