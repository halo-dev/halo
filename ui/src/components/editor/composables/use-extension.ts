import { randomUUID } from "@/utils/id";
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
    const resolvedExtensions = sort(extensions);
    const map = new Map<string, AnyExtension>();
    resolvedExtensions.forEach((extension) => {
      if (!extension.name) {
        console.warn(
          `Extension name is missing for Extension, type: ${extension.type}.`
        );
        const key = randomUUID().toString();
        map.set(key, extension);
        return;
      }
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
