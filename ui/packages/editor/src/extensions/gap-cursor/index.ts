import {
  Extension,
  callOrReturn,
  getExtensionField,
  type AnyExtension,
  type NodeConfig,
  type ParentConfig,
} from "@/tiptap/core";
import { GapCursor, Plugin, PluginKey } from "@/tiptap/pm";
import { isGapCursorPosition } from "@/utils";
import {
  createGapCursorKeydownHandler,
  createTextblockAtGapCursor,
} from "./gap-cursor-commands";
import { HaloGapCursor } from "./gap-cursor-selection";
import {
  drawGapCursor,
  GapCursorPositioner,
  handleGapCursorMouseDown,
} from "./gap-cursor-view";

declare module "@tiptap/core" {
  interface NodeConfig<Options, Storage> {
    /**
     * Overrides whether this node exposes before/after gap stops. Structural
     * block nodes are detected automatically when this is not configured.
     */
    createGapCursor?:
      | boolean
      | null
      | ((this: {
          name: string;
          options: Options;
          storage: Storage;
          parent: ParentConfig<NodeConfig<Options>>["createGapCursor"];
        }) => boolean | null);
  }
}

/**
 * Adds ProseMirror's GapCursor selection and Halo's structural block editing
 * behavior. The selection stays at real positions between document nodes.
 */
export const ExtensionGapCursor = Extension.create({
  name: "gapCursor",

  // Structural navigation runs before the core keymap. Suggestion menus use a
  // higher priority and still receive navigation keys first while active.
  priority: 900,

  addProseMirrorPlugins() {
    return [createGapCursorPlugin()];
  },

  extendNodeSchema(extension) {
    const context = {
      name: extension.name,
      options: extension.options,
      storage: extension.storage,
    };

    const createGapCursor = callOrReturn(
      getExtensionField<NodeConfig["createGapCursor"]>(
        extension,
        "createGapCursor",
        context
      )
    );
    const allowGapCursor = callOrReturn(
      getExtensionField<NodeConfig["allowGapCursor"]>(
        extension,
        "allowGapCursor",
        context
      )
    );

    return {
      // Keep Tiptap's official meaning: this controls gap positions inside the
      // node as a parent, not whether the node is a structural gap target.
      allowGapCursor: allowGapCursor ?? null,
      createGapCursor:
        createGapCursor ?? inferCreateGapCursor(extension, context),
    };
  },
});

function createGapCursorPlugin() {
  return new Plugin({
    key: new PluginKey("halo-gap-cursor"),
    view: (view) => new GapCursorPositioner(view),
    props: {
      decorations: drawGapCursor,
      createSelectionBetween(_view, $anchor, $head) {
        if ($anchor.pos !== $head.pos) {
          return null;
        }
        if (!isGapCursorPosition($head)) {
          return null;
        }
        return new HaloGapCursor($head);
      },
      handleKeyDown: createGapCursorKeydownHandler(),
      handleTextInput(view, _from, _to, text) {
        const tr = createTextblockAtGapCursor(view.state);
        if (!tr) {
          return false;
        }

        tr.insertText(text).scrollIntoView();
        view.dispatch(tr);
        return true;
      },
      handleDOMEvents: {
        mousedown: handleGapCursorMouseDown,
        beforeinput(view, event) {
          if (event.inputType !== "insertCompositionText") {
            return false;
          }
          if (!(view.state.selection instanceof GapCursor)) {
            return false;
          }

          const tr = createTextblockAtGapCursor(view.state);
          if (tr) {
            view.dispatch(tr);
          }
          return false;
        },
      },
    },
  });
}

function inferCreateGapCursor(
  extension: AnyExtension,
  context: Pick<AnyExtension, "name" | "options" | "storage">
): boolean | null {
  const group = callOrReturn(
    getExtensionField<NodeConfig["group"]>(extension, "group", context)
  );
  if (typeof group !== "string") {
    return null;
  }
  if (!group.split(/\s+/).includes("block")) {
    return null;
  }

  const isInline = callOrReturn(
    getExtensionField<NodeConfig["inline"]>(extension, "inline", context)
  );
  if (isInline) {
    return null;
  }

  const hasNodeView = Boolean(
    getExtensionField<NodeConfig["addNodeView"]>(extension, "addNodeView")
  );
  if (hasNodeView) {
    return true;
  }

  const isAtom = callOrReturn(
    getExtensionField<NodeConfig["atom"]>(extension, "atom", context)
  );
  if (isAtom) {
    return true;
  }

  const isIsolating = callOrReturn(
    getExtensionField<NodeConfig["isolating"]>(extension, "isolating", context)
  );
  if (isIsolating) {
    return true;
  }

  const isCode = callOrReturn(
    getExtensionField<NodeConfig["code"]>(extension, "code", context)
  );
  return isCode ? true : null;
}

export { HaloGapCursor } from "./gap-cursor-selection";
