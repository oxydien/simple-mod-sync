import { createSignal, createEffect, onCleanup } from "solid-js";

export default function DragChecker(elId: string): [() => boolean, (isDragging: boolean) => void] {
  const [isDragging, setIsDragging] = createSignal(false);

  createEffect(() => {
    const el = document.getElementById(elId);
    if (!el) return;

    const handleDragEnter = (_e: DragEvent) => {
      const rect = el.getBoundingClientRect();
      if (_e.clientX >= rect.left && _e.clientX <= rect.right &&
        _e.clientY >= rect.top && _e.clientY <= rect.bottom) {
        setIsDragging(true);
      }
    };

    const handleDragOver = (e: DragEvent) => {
      e.preventDefault();
      const rect = el.getBoundingClientRect();
      if (e.clientX < rect.left || e.clientX > rect.right ||
        e.clientY < rect.top || e.clientY > rect.bottom) {
        setIsDragging(false);
        return;
      }
      setIsDragging(true);
    };

    const handleDragLeave = (e: DragEvent) => {
      const rect = el.getBoundingClientRect();
      if (e.clientX < rect.left || e.clientX > rect.right ||
        e.clientY < rect.top || e.clientY > rect.bottom) {
        setIsDragging(false);
      }
    };

    const handleDrop = (e: DragEvent) => {
      e.preventDefault();
      setIsDragging(false);
    };

    const handleDragEnd = (_e: DragEvent) => {
      setIsDragging(false);
    };

    window.addEventListener("dragenter", handleDragEnter);
    window.addEventListener("dragover", handleDragOver);
    window.addEventListener("dragleave", handleDragLeave);
    window.addEventListener("drop", handleDrop);
    window.addEventListener("dragend", handleDragEnd);

    onCleanup(() => {
      window.removeEventListener("dragenter", handleDragEnter);
      window.removeEventListener("dragover", handleDragOver);
      window.removeEventListener("dragleave", handleDragLeave);
      window.removeEventListener("drop", handleDrop);
      window.removeEventListener("dragend", handleDragEnd);
    });
  });

  return [isDragging, setIsDragging];
}
