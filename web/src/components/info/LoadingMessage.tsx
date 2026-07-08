import LoadingSpinner from "../LoadingSpinner";


interface LoadingMessageProps {
  message?: string;
  class?: string;
}

export default function LoadingMessage({...props}: LoadingMessageProps) {
  return (
    <div class={`flex flex-col justify-center items-center gap-2 w-full ${props.class || ""}`}>
      <LoadingSpinner />
      <span>
        {props.message || "Loading, please wait..."}
      </span>
    </div>
  )
}
