import type { Editor, VueEditor } from "@/tiptap";

/**
 * Copy the selected content from the editor to the clipboard
 * Supports both HTML and plain text formats
 *
 * @param editor - TipTap editor instance
 * @returns Promise<boolean> - Whether the copy operation was successful
 */
export async function copySelectionToClipboard(
  editor: Editor | VueEditor
): Promise<boolean> {
  try {
    const slice = editor.state.selection.content();
    const { dom, text } = editor.view.serializeForClipboard(slice);
    if (navigator.clipboard && window.ClipboardItem) {
      try {
        const htmlBlob = new Blob([dom.innerHTML], { type: "text/html" });
        const textBlob = new Blob([text], { type: "text/plain" });
        const clipboardItem = new ClipboardItem({
          "text/html": htmlBlob,
          "text/plain": textBlob,
        });
        await navigator.clipboard.write([clipboardItem]);
        return true;
      } catch {
        await navigator.clipboard.writeText(text);
        return true;
      }
    }
    await navigator.clipboard.writeText(text);
    return true;
  } catch (error) {
    console.error("Failed to copy to clipboard:", error);
    return false;
  }
}
