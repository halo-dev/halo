export function setFocus(id: string) {
  const inputElement = document.getElementById(id);
  if (
    inputElement instanceof HTMLInputElement ||
    inputElement instanceof HTMLTextAreaElement
  ) {
    const timer = setTimeout(() => {
      const end = inputElement.value.length;
      inputElement.setSelectionRange(end, end);
      inputElement?.focus();
      clearTimeout(timer);
    }, 0);
  }
}
