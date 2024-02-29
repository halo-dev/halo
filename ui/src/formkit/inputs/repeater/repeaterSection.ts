import {
  isComponent,
  isDOM,
  type FormKitSchemaNode,
  type FormKitExtendableSchemaRoot,
  type FormKitSchemaCondition,
  type FormKitSectionsSchema,
} from "@formkit/core";
import {
  extendSchema,
  type FormKitSchemaExtendableSection,
  type FormKitSection,
} from "@formkit/inputs";

export function createRepeaterSection() {
  return (
    section: string,
    el: string | null | (() => FormKitSchemaNode),
    fragment = false
  ) => {
    return createSection(
      section,
      el,
      fragment
    ) as FormKitSection<FormKitSchemaExtendableSection>;
  };
}

function createSection(
  section: string,
  el: string | null | (() => FormKitSchemaNode),
  fragment = false
): FormKitSection<
  FormKitExtendableSchemaRoot | FormKitSchemaExtendableSection
> {
  return (
    ...children: Array<
      FormKitSchemaExtendableSection | string | FormKitSchemaCondition
    >
  ) => {
    const extendable = (extensions: FormKitSectionsSchema) => {
      const node = !el || typeof el === "string" ? { $el: el } : el();
      if ("string" != typeof node) {
        if (isDOM(node) || isComponent(node) || "$formkit" in node) {
          if (children.length && !node.children) {
            node.children = [
              ...children.map((child) =>
                typeof child === "function" ? child(extensions) : child
              ),
            ];
          }
          if (!node.meta) {
            node.meta = { section };
          }
          if (isDOM(node)) {
            node.attrs = {
              class: `$classes.${section}`,
              ...(node.attrs || {}),
            };
          }
          if ("$formkit" in node) {
            node.outerClass = `$classes.${section}`;
          }
        }
      }
      return {
        if: `$slots.${section}`,
        then: `$slots.${section}`,
        else:
          section in extensions
            ? extendSchema(node as FormKitSchemaNode, extensions[section])
            : node,
      } as FormKitSchemaCondition;
    };
    extendable._s = section;
    return fragment ? createRoot(extendable) : extendable;
  };
}

/**
 *
 * Returns an extendable schema root node.
 *
 * @param rootSection - Creates the root node.
 *
 * @returns {@link @formkit/core#FormKitExtendableSchemaRoot | FormKitExtendableSchemaRoot}
 *
 * @internal
 */
export function createRoot(
  rootSection: FormKitSchemaExtendableSection
): FormKitExtendableSchemaRoot {
  return (extensions: FormKitSectionsSchema) => {
    return [rootSection(extensions)];
  };
}
