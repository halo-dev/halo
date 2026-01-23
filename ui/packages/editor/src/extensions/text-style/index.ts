import {
  TextStyleKit,
  type TextStyleKitOptions,
} from "@tiptap/extension-text-style";

export type ExtensionTextStyleOptions = Partial<TextStyleKitOptions>;

export const ExtensionTextStyle =
  TextStyleKit.extend<ExtensionTextStyleOptions>({
    // Set the priority of this extension to 110 to ensure it loads before other extensions.
    // It must load before the highlight plugin, otherwise, it will cause span and mark to display in parallel.
    priority: 110,
  }).configure({
    backgroundColor: {
      types: ["textStyle"],
    },
    color: {
      types: ["textStyle"],
    },
    fontFamily: {
      types: ["textStyle"],
    },
    fontSize: {
      types: ["textStyle"],
    },
    lineHeight: {
      types: ["textStyle"],
    },
  });
