import type { Editor } from "@/tiptap";
import { DOMSerializer } from "@/tiptap/pm";
import { findTable } from "@/utils/table";

export async function writeTableToClipboard(editor: Editor) {
  const table = findTable(editor.state.selection);
  if (!table || typeof navigator === "undefined" || !navigator.clipboard) {
    return false;
  }

  const container = document.createElement("div");
  container.appendChild(
    DOMSerializer.fromSchema(editor.schema).serializeNode(table.node)
  );
  const html = container.innerHTML;
  const text = table.node.content.content
    .map((row) =>
      row.content.content
        .map((cell) => cell.textContent.replace(/\s+/g, " ").trim())
        .join("\t")
    )
    .join("\n");
  if (
    typeof ClipboardItem !== "undefined" &&
    typeof navigator.clipboard.write === "function"
  ) {
    await navigator.clipboard.write([
      new ClipboardItem({
        "text/html": new Blob([html], { type: "text/html" }),
        "text/plain": new Blob([text], { type: "text/plain" }),
      }),
    ]);
    return true;
  }

  await navigator.clipboard.writeText(html);
  return true;
}
