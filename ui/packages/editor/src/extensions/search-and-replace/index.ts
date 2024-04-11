import { Editor, Extension } from "@/tiptap/vue-3";
import {
  SearchAndReplacePlugin,
  searchAndReplacePluginKey,
} from "./SearchAndReplacePlugin";
import SearchAndReplaceVue from "./SearchAndReplace.vue";
import { h, markRaw, render } from "vue";
import { EditorState } from "@/tiptap/pm";
import type { ExtensionOptions } from "@/types";
import { i18n } from "@/locales";
import { ToolbarItem } from "@/components";
import MdiTextBoxSearchOutline from "~icons/mdi/text-box-search-outline";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    searchAndReplace: {
      /**
       * @description Replace first instance of search result with given replace term.
       */
      replace: () => ReturnType;
      /**
       * @description Replace all instances of search result with given replace term.
       */
      replaceAll: () => ReturnType;
      /**
       * @description Find next instance of search result.
       */
      findNext: () => ReturnType;
      /**
       * @description Find previous instance of search result.
       */
      findPrevious: () => ReturnType;
      /**
       * @description Open search panel.
       */
      openSearch: () => ReturnType;
      /**
       * @description Close search panel.
       */
      closeSearch: () => ReturnType;
    };
  }
}

const instance = h<any>(SearchAndReplaceVue);
function isShowSearch() {
  const searchAndReplaceInstance = instance.component;
  if (searchAndReplaceInstance) {
    return searchAndReplaceInstance.props.visible;
  }
  return false;
}
const SearchAndReplace = Extension.create<ExtensionOptions>({
  name: "searchAndReplace",

  // @ts-ignore
  addOptions() {
    return {
      getToolbarItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 230,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: isShowSearch(),
              icon: markRaw(MdiTextBoxSearchOutline),
              title: i18n.global.t(
                "editor.extensions.search_and_replace.title"
              ),
              action: () => {
                const searchAndReplaceInstance = instance.component;
                if (searchAndReplaceInstance) {
                  const visible = searchAndReplaceInstance.props.visible;
                  if (visible) {
                    editor.commands.closeSearch();
                  } else {
                    editor.commands.openSearch();
                  }
                }
              },
            },
          },
        ];
      },
    };
  },

  addCommands() {
    return {
      replace:
        () =>
        ({
          state,
          dispatch,
        }: {
          state: EditorState;
          dispatch: ((args?: any) => any) | undefined;
        }) => {
          const searchAndReplaceState =
            searchAndReplacePluginKey.getState(state);
          if (!searchAndReplaceState) {
            return false;
          }
          const { replaceTerm, results, findIndex } = searchAndReplaceState;
          const result = results[findIndex];
          if (!result) {
            return false;
          }

          const { from, to } = result;

          if (dispatch) {
            const tr = state.tr;
            tr.insertText(replaceTerm, from, to);
            tr.setMeta(searchAndReplacePluginKey, {
              setFindIndex: findIndex,
              refresh: true,
            });
            dispatch(tr);
          }

          return false;
        },

      replaceAll:
        () =>
        ({
          state,
          dispatch,
        }: {
          state: EditorState;
          dispatch: ((args?: any) => any) | undefined;
        }) => {
          const searchAndReplaceState =
            searchAndReplacePluginKey.getState(state);
          if (!searchAndReplaceState) {
            return false;
          }
          const { replaceTerm, results } = searchAndReplaceState;
          const tr = state.tr;
          let offset = 0;
          results.forEach((result) => {
            const { from, to } = result;
            tr.insertText(replaceTerm, offset + from, offset + to);
            // when performing multi-text replacement, it is necessary
            // to calculate the offset between 'form' and 'to'.
            offset = offset + replaceTerm.length - (to - from);
          });

          if (dispatch) {
            dispatch(tr);
          }
          return false;
        },

      findNext:
        () =>
        ({
          state,
          dispatch,
        }: {
          state: EditorState;
          dispatch: ((args?: any) => any) | undefined;
        }) => {
          if (dispatch) {
            const tr = state.tr;
            const searchAndReplaceState =
              searchAndReplacePluginKey.getState(state);
            if (!searchAndReplaceState) {
              return false;
            }
            const { findIndex } = searchAndReplaceState;

            tr.setMeta(searchAndReplacePluginKey, {
              setFindIndex: findIndex + 1,
            });
            dispatch(tr);
          }
          return false;
        },

      findPrevious:
        () =>
        ({
          state,
          dispatch,
        }: {
          state: EditorState;
          dispatch: ((args?: any) => any) | undefined;
        }) => {
          if (dispatch) {
            const searchAndReplaceState =
              searchAndReplacePluginKey.getState(state);
            if (!searchAndReplaceState) {
              return false;
            }
            const { findIndex } = searchAndReplaceState;
            const tr = state.tr;
            tr.setMeta(searchAndReplacePluginKey, {
              setFindIndex: findIndex - 1,
            });
            dispatch(tr);
          }
          return false;
        },

      openSearch:
        () =>
        ({
          state,
          dispatch,
        }: {
          state: EditorState;
          dispatch: ((args?: any) => any) | undefined;
        }) => {
          const searchAndReplaceState =
            searchAndReplacePluginKey.getState(state);
          if (!searchAndReplaceState) {
            return false;
          }
          const searchAndReplaceInstance = instance.component;
          if (searchAndReplaceInstance) {
            searchAndReplaceInstance.props.visible = true;
            const tr = state.tr;
            tr.setMeta(searchAndReplacePluginKey, {
              setEnable: true,
            });
            if (dispatch) {
              dispatch(tr);
            }
          }
          return false;
        },

      closeSearch:
        () =>
        ({
          state,
          dispatch,
        }: {
          state: EditorState;
          dispatch: ((args?: any) => any) | undefined;
        }) => {
          const searchAndReplaceState =
            searchAndReplacePluginKey.getState(state);
          if (!searchAndReplaceState) {
            return false;
          }
          const searchAndReplaceInstance = instance.component;
          if (searchAndReplaceInstance) {
            searchAndReplaceInstance.props.visible = false;
            const tr = state.tr;
            tr.setMeta(searchAndReplacePluginKey, {
              setEnable: false,
            });
            if (dispatch) {
              dispatch(tr);
            }
          }
          return false;
        },
    };
  },

  addProseMirrorPlugins() {
    const containerDom = document.createElement("div");
    containerDom.style.position = "sticky";
    containerDom.style.top = "0";
    containerDom.style.zIndex = "50";
    instance.props = {
      editor: this.editor,
      pluginKey: searchAndReplacePluginKey,
      visible: false,
    };
    render(instance, containerDom);
    return [
      SearchAndReplacePlugin({
        editor: this.editor as Editor,
        element: containerDom,
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

export default SearchAndReplace;
