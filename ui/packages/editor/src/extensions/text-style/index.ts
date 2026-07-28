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
    addHaloEditorMetadata() {
      return {
        contributions: [
          {
            targets: [{ kind: "mark", name: "textStyle" }],
            metadata: {
              ai: {
                description:
                  "Inline text styling carried by a span, including color, font family, font size, background color, or line height.",
                exposure: "available",
                useWhen: [
                  "A specific inline visual style is explicitly useful or requested.",
                ],
                avoidWhen: [
                  "Semantic marks such as strong, emphasis, or code better express the meaning.",
                ],
                attributeGuidance: {
                  backgroundColor: {
                    description: "CSS background color for the inline text.",
                    format: "CSS color",
                    examples: ["#fff3a3", "rgb(255, 243, 163)"],
                    omitWhen: ["No custom background color is needed."],
                  },
                  color: {
                    description: "CSS foreground color for the inline text.",
                    format: "CSS color",
                    examples: ["#1f2937", "rgb(31, 41, 55)"],
                    omitWhen: ["The inherited text color is appropriate."],
                  },
                  fontFamily: {
                    description: "CSS font-family for the inline text.",
                    format: "CSS font-family",
                    examples: ["serif", '"JetBrains Mono", monospace'],
                    omitWhen: ["The inherited font is appropriate."],
                  },
                  fontSize: {
                    description: "CSS font-size for the inline text.",
                    format: "CSS length",
                    examples: ["14px", "1.25rem"],
                    omitWhen: ["The inherited font size is appropriate."],
                  },
                  lineHeight: {
                    description: "CSS line-height for the inline text.",
                    format: "CSS line-height",
                    examples: ["1.5", "24px"],
                    omitWhen: ["The inherited line height is appropriate."],
                  },
                },
                generation: {
                  mode: "direct-html",
                },
                examples: [
                  '<p><span style="color: #1f2937; font-size: 18px">Styled text</span></p>',
                  '<p><span style="background-color: #fff3a3; font-family: serif">Highlighted serif text</span></p>',
                  '<p><span style="line-height: 1.8">Text with a custom line height</span></p>',
                ],
              },
            },
          },
        ],
      };
    },
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
