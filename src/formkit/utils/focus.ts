export function setFocus(id: string) {
  const inputElement = document.getElementById(id);
  if (inputElement instanceof HTMLInputElement) {
    const timer = setTimeout(() => {
      inputElement?.focus();
      clearTimeout(timer);
    }, 0);
  }
}
