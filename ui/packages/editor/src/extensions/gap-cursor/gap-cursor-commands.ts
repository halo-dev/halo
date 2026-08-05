import { keydownHandler } from "@/tiptap/pm";
import {
  backspaceAtGapCursor,
  deleteForwardAtGapCursor,
  insertTextblockAndKeepGapCursor,
  insertTextblockAtGapCursor,
} from "./gap-cursor-editing";
import { createGapCursorArrowCommand } from "./gap-cursor-navigation";

export { createTextblockAtGapCursor } from "./gap-cursor-editing";

export function createGapCursorKeydownHandler() {
  return keydownHandler({
    ArrowLeft: createGapCursorArrowCommand("horizontal", -1),
    ArrowRight: createGapCursorArrowCommand("horizontal", 1),
    ArrowUp: createGapCursorArrowCommand("vertical", -1),
    ArrowDown: createGapCursorArrowCommand("vertical", 1),
    Enter: insertTextblockAndKeepGapCursor,
    Tab: insertTextblockAtGapCursor,
    Backspace: backspaceAtGapCursor,
    Delete: deleteForwardAtGapCursor,
  });
}
