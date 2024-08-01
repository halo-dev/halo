import {
  callOrReturn,
  CoreEditor,
  getExtensionField,
  isActive,
  type NodeConfig,
} from "@/tiptap";

/**
 * Check if a list is active
 *
 * @param state - The state of the editor
 * @returns Whether a list is active
 * @example
 * ```ts
 * const isActive = isListActive(editor.state);
 * ```
 **/
export const isListActive = (editor: CoreEditor) => {
  const extensions = editor.extensionManager.extensions;
  const listExtensions = extensions.filter((extension) => {
    const context = {
      name: extension.name,
      options: extension.options,
      storage: extension.storage,
    };

    const group = callOrReturn(
      getExtensionField<NodeConfig["group"]>(extension, "group", context)
    );

    if (typeof group !== "string") {
      return false;
    }

    return group.split(" ").includes("list");
  });

  return listExtensions.some((extension) => {
    return isActive(editor.state, extension.name);
  });
};
