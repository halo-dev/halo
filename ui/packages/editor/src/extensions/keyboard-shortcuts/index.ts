import { markRaw } from "vue";
import MingcuteKeyboardLine from "~icons/mingcute/keyboard-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import {
  defineHaloKeyboardShortcuts,
  getHaloKeyboardShortcutBindings,
  requestHaloKeyboardShortcutHelp,
} from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import { Extension, Plugin, type Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { matchShortcut } from "@/utils";

function focusEditorToolbar(editor: Editor) {
  const editorRoot = editor.view.dom.closest(".halo-rich-text-editor");
  const firstItem = editorRoot?.querySelector<HTMLElement>(
    '[role="toolbar"] button:not(:disabled)'
  );
  if (!firstItem) {
    return false;
  }
  firstItem.focus();
  return true;
}

export const ExtensionKeyboardShortcuts = Extension.create<ExtensionOptions>({
  name: "haloKeyboardShortcuts",
  // Run Halo's exact Option/Alt dispatcher before Tiptap extensions that use
  // the same physical key, such as the default Mod-Alt-C code-block binding.
  priority: 1000,

  addProseMirrorPlugins() {
    return [
      new Plugin({
        props: {
          handleKeyDown: (_view, event) => {
            // Tiptap/ProseMirror remains authoritative for ordinary shortcuts.
            // This dispatcher only covers Option/Alt combinations whose
            // event.key may be changed by the operating system or layout.
            if (!event.altKey) {
              return false;
            }
            for (const binding of getHaloKeyboardShortcutBindings(
              this.editor
            )) {
              if (!matchShortcut(event, binding.key)) {
                continue;
              }
              if (binding.command({ editor: this.editor })) {
                return true;
              }
            }
            return false;
          },
        },
      }),
    ];
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 10000,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: false,
            icon: markRaw(MingcuteKeyboardLine),
            title: i18n.global.t("editor.shortcuts.title"),
            shortcutId: "editor.shortcuts.openHelp",
            action: () => requestHaloKeyboardShortcutHelp(editor),
          },
        };
      },
    };
  },

  addKeyboardShortcuts() {
    return defineHaloKeyboardShortcuts(this, [
      {
        id: "editor.shortcuts.openHelp",
        keys: ["Alt-Mod-?"],
        label: () => i18n.global.t("editor.shortcuts.commands.open_help"),
        category: "general",
        priority: 0,
        command: () => {
          requestHaloKeyboardShortcutHelp(this.editor);
          return true;
        },
      },
      {
        id: "editor.navigation.toolbar",
        keys: ["Alt-F10"],
        label: () => i18n.global.t("editor.shortcuts.commands.focus_toolbar"),
        category: "navigation",
        priority: 10,
        command: () => focusEditorToolbar(this.editor),
      },
      {
        id: "editor.general.copy",
        keys: ["Mod-c"],
        label: () => i18n.global.t("editor.shortcuts.commands.copy"),
        category: "general",
        priority: 30,
        command: () => false,
      },
      {
        id: "editor.general.cut",
        keys: ["Mod-x"],
        label: () => i18n.global.t("editor.shortcuts.commands.cut"),
        category: "general",
        priority: 40,
        command: () => false,
      },
      {
        id: "editor.general.paste",
        keys: ["Mod-v"],
        label: () => i18n.global.t("editor.shortcuts.commands.paste"),
        category: "general",
        priority: 50,
        command: () => false,
      },
      {
        id: "editor.general.pastePlainText",
        keys: ["Mod-Shift-v"],
        label: () =>
          i18n.global.t("editor.shortcuts.commands.paste_plain_text"),
        category: "general",
        priority: 55,
        command: () => false,
      },
      {
        id: "editor.general.selectAll",
        keys: ["Mod-a"],
        label: () => i18n.global.t("editor.shortcuts.commands.select_all"),
        category: "general",
        priority: 60,
        command: () => false,
      },
    ]);
  },
});
