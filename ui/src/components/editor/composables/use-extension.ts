import {
  getExtensionField,
  type AnyConfig,
  type AnyExtension,
  type Extensions,
} from "@halo-dev/richtext-editor";

export function useExtension() {
  const filterDuplicateExtensions = (extensions: Extensions | undefined) => {
    if (!extensions) {
      return;
    }

    const resolvedExtensions = sort(flatten(extensions));

    const map = new Map<string, AnyExtension>();

    resolvedExtensions.forEach((extension) => {
      const key = `${extension.type}-${extension.name}`;
      if (map.has(key)) {
        console.warn(
          `Duplicate found for Extension, type: ${extension.type}, name: ${extension.name}. Keeping the later one.`
        );
      }
      map.set(key, extension);
    });

    return Array.from(map.values());
  };

  /**
   * Create a flattened array of extensions by traversing the `addExtensions` field.
   * @param extensions An array of Tiptap extensions
   * @returns A flattened array of Tiptap extensions
   */
  const flatten = (extensions: Extensions): Extensions => {
    return (
      extensions
        .map((extension) => {
          const context = {
            name: extension.name,
            options: extension.options,
            storage: extension.storage,
          };

          const addExtensions = getExtensionField<AnyConfig["addExtensions"]>(
            extension,
            "addExtensions",
            context
          );

          if (addExtensions) {
            return [extension, ...flatten(addExtensions())];
          }

          return extension;
        })
        // `Infinity` will break TypeScript so we set a number that is probably high enough
        .flat(10)
    );
  };

  /**
   * Sort extensions by priority.
   * @param extensions An array of Tiptap extensions
   * @returns A sorted array of Tiptap extensions by priority
   */
  const sort = (extensions: Extensions): Extensions => {
    const defaultPriority = 100;

    return extensions.sort((a, b) => {
      const priorityA =
        getExtensionField<AnyConfig["priority"]>(a, "priority") ||
        defaultPriority;
      const priorityB =
        getExtensionField<AnyConfig["priority"]>(b, "priority") ||
        defaultPriority;

      if (priorityA > priorityB) {
        return -1;
      }

      if (priorityA < priorityB) {
        return 1;
      }

      return 0;
    });
  };

  return {
    filterDuplicateExtensions,
  };
}
