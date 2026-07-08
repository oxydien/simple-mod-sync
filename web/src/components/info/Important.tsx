import {JSX} from "solid-js";


export default function Important({children}: {children: JSX.Element}) {
  return <strong><u class="decoration-dashed text-lg">{children}</u></strong>
}