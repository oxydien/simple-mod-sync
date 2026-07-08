interface LoadingSpinnerProps {
  size?: number;
  /** in CSS duration */
  speed?: string;
  class?: string;
}

export default function LoadingSpinner(props: LoadingSpinnerProps) {
  const size = () => props.size || 64;
  const speed = () => props.speed || "320ms";

  return (
    <div
      style={{
        width: `${size()}px`,
        height: `${size()}px`,
        animation: `pixel-spin ${speed()} steps(7) infinite`,
      }}
      class={`loading-spinner ${props.class || ""}`}
    />
  );
}