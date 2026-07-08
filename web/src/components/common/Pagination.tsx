import { For, Show } from "solid-js";
import Button from "./Button";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination(props: PaginationProps) {
  const windowSize = 5;

  const visiblePages = () => {
    const total = props.totalPages;
    const current = props.currentPage;

    if (total <= windowSize) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }

    let start = current - Math.floor(windowSize / 2);

    if (start < 1) {
      start = 1;
    }

    let end = start + windowSize - 1;

    if (end > total) {
      end = total;
      start = Math.max(1, end - windowSize + 1);
    }

    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  };

  return (
    <Show when={props.totalPages > 1}>
      <div class="pagination-controls" style={{ display: "flex", gap: "0.5rem" }}>

        {/* Previous Button */}
        <Button
          variant="default"
          disabled={props.currentPage <= 1}
          onClick={() => props.onPageChange(props.currentPage - 1)}
        >
          Prev
        </Button>

        {/* Windowed Page Numbers */}
        <For each={visiblePages()}>
          {(page) => (
            <Button
              variant={props.currentPage === page ? "primary" : "default"}
              disabled={props.currentPage === page}
              onClick={() => props.onPageChange(page)}
            >
              {page}
            </Button>
          )}
        </For>

        {/* Next Button */}
        <Button
          variant="default"
          disabled={props.currentPage >= props.totalPages}
          onClick={() => props.onPageChange(props.currentPage + 1)}
        >
          Next
        </Button>

      </div>
    </Show>
  );
}