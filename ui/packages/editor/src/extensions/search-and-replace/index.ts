import {
  FindAndReplace,
  type FindAndReplaceOptions,
} from "@tiptap/extension-find-and-replace";
import { h, markRaw, reactive, render } from "vue";
import MingcuteListSearchLine from "~icons/mingcute/list-search-line";
import { ToolbarItem } from "@/components";
import { i18n } from "@/locales";
import { Editor, Extension } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import SearchAndReplaceVue from "./SearchAndReplace.vue";
import { SearchAndReplacePanelPlugin } from "./SearchAndReplacePanelPlugin";
import type { SearchAndReplacePanelState } from "./types";

export interface ExtensionSearchAndReplaceOptions
  extends FindAndReplaceOptions, ExtensionOptions {}

export interface ExtensionSearchAndReplaceStorage {
  panel: SearchAndReplacePanelState;
}

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    searchAndReplace: {
      /**
       * @description Open the search panel.
       */
      openSearch: () => ReturnType;
      /**
       * @description Close the search panel.
       */
      closeSearch: () => ReturnType;
    };
  }

  interface Storage {
    searchAndReplace: ExtensionSearchAndReplaceStorage;
  }
}

export const ExtensionSearchAndReplace = Extension.create<
  ExtensionSearchAndReplaceOptions,
  ExtensionSearchAndReplaceStorage
>({
  name: "searchAndReplace",

  addOptions() {
    return {
      searchTerm: "",
      replaceTerm: "",
      caseSensitive: false,
      useRegex: false,
      wholeWord: false,
      searchDebounceMs: 250,
      injectCSS: false,
      injectNonce: undefined,
      getToolbarItems({ editor }: { editor: Editor }) {
        const panel = editor.storage.searchAndReplace.panel;

        return [
          {
            priority: 230,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: panel.visible,
              icon: markRaw(MingcuteListSearchLine),
              title: i18n.global.t(
                "editor.extensions.search_and_replace.title"
              ),
              action: () => {
                if (panel.visible) {
                  editor.commands.closeSearch();
                } else {
                  editor.commands.openSearch();
                }
              },
            },
          },
        ];
      },
    };
  },

  addStorage() {
    return {
      panel: reactive({
        visible: false,
        searchTerm: this.options.searchTerm,
        replaceTerm: this.options.replaceTerm,
        caseSensitive: this.options.caseSensitive,
        useRegex: this.options.useRegex,
        wholeWord: this.options.wholeWord,
      }),
    };
  },

  addExtensions() {
    return [
      FindAndReplace.configure({
        searchTerm: this.options.searchTerm,
        replaceTerm: this.options.replaceTerm,
        caseSensitive: this.options.caseSensitive,
        useRegex: this.options.useRegex,
        wholeWord: this.options.wholeWord,
        searchDebounceMs: this.options.searchDebounceMs,
        injectCSS: this.options.injectCSS,
        injectNonce: this.options.injectNonce,
      }),
    ];
  },

  addCommands() {
    return {
      openSearch:
        () =>
        ({ commands }) => {
          const panel = this.storage.panel;
          panel.visible = true;
          commands.setSearchTerm(panel.searchTerm);
          return true;
        },
      closeSearch:
        () =>
        ({ commands }) => {
          this.storage.panel.visible = false;
          return commands.clearSearch();
        },
    };
  },

  addProseMirrorPlugins() {
    if (typeof document === "undefined") {
      return [];
    }

    return [
      SearchAndReplacePanelPlugin({
        createPanel: () => {
          const container = document.createElement("div");
          container.style.position = "sticky";
          container.style.top = "0";
          container.style.zIndex = "50";

          render(
            h(SearchAndReplaceVue, {
              editor: this.editor,
              panel: this.storage.panel,
              onSearchTermChange: (value: string) => {
                this.storage.panel.searchTerm = value;
              },
              onReplaceTermChange: (value: string) => {
                this.storage.panel.replaceTerm = value;
              },
              onCaseSensitiveChange: (value: boolean) => {
                this.storage.panel.caseSensitive = value;
              },
              onUseRegexChange: (value: boolean) => {
                this.storage.panel.useRegex = value;
              },
              onWholeWordChange: (value: boolean) => {
                this.storage.panel.wholeWord = value;
              },
            }),
            container
          );

          return {
            element: container,
            destroy: () => render(null, container),
          };
        },
      }),
    ];
  },

  addKeyboardShortcuts() {
    return {
      "Mod-f": () => {
        this.editor.commands.openSearch();
        return true;
      },
    };
  },
});

export type { SearchAndReplacePanelState } from "./types";
