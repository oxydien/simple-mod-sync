

interface IconProps {
  name: string,
  class?: string,
}

export default function Icon(props: IconProps) {
  return <img src={`/src/public/assets/icons/${props.name}.png`} alt={`${props.name} icon`} style={{ width: "1em", height: "1em" }} class={props.class} aria-hidden />
}