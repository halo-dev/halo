import TiptapTextStyle, {
  type TextStyleOptions,
} from "@tiptap/extension-text-style";

const TextStyle = TiptapTextStyle.extend<TextStyleOptions>({
  // Set the priority of this extension to 110 to ensure it loads before other extensions.
  // It must load before the highlight plugin, otherwise, it will cause span and mark to display in parallel.
  priority: 110,
});

export default TextStyle;
