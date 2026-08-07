import { getMarkRange } from "@tiptap/core";
import TiptapLink, { type LinkOptions } from "@tiptap/extension-link";
import { Plugin, PluginKey, TextSelection } from "@tiptap/pm/state";
import type { ExtensionOptions } from "@/types";

export type ExtensionLinkOptions = ExtensionOptions & Partial<LinkOptions>;

export const ExtensionLink = TiptapLink.extend<ExtensionLinkOptions>({
  addProseMirrorPlugins() {
    const linkType = this.type;

    return [
      ...(this.parent?.() ?? []),
      new Plugin({
        key: new PluginKey("haloLinkDoubleClickSelection"),
        props: {
          handleDoubleClick(view, pos, event) {
            const target = event.target;

            if (!(target instanceof Element)) {
              return false;
            }

            const link = target.closest("a");

            if (!link || !view.dom.contains(link)) {
              return false;
            }

            const range = getMarkRange(view.state.doc.resolve(pos), linkType);

            if (!range) {
              return false;
            }

            view.dispatch(
              view.state.tr.setSelection(
                TextSelection.create(view.state.doc, range.from, range.to)
              )
            );

            return true;
          },
        },
      }),
    ];
  },

  addHaloEditorMetadata() {
    return {
      ai: {
        description: "A hyperlink applied to inline content.",
        exposure: "recommended",
        useWhen: ["Linking a relevant source, page, download, or reference."],
        avoidWhen: ["No trustworthy destination URL is available."],
        contentGuidelines: [
          "Use descriptive link text instead of repeating the raw URL when possible.",
        ],
        attributeGuidance: {
          href: {
            description: "Destination URL for the link.",
            format: "absolute or site-relative URL",
            examples: ["https://www.halo.run/", "/docs/getting-started"],
          },
          target: {
            description: "Browsing context in which to open the destination.",
            allowedValues: ["_blank", "_self", "_parent", "_top", null],
            omitWhen: ["The current browsing context should be used."],
          },
          rel: {
            description: "Space-separated relationship tokens.",
            format: "HTML link types",
            examples: ["nofollow", "noopener noreferrer"],
            omitWhen: ["No relationship tokens are needed."],
          },
          title: {
            description: "Optional advisory text describing the destination.",
            omitWhen: [
              "It would repeat the visible link text or add no useful context.",
            ],
          },
          class: {
            description:
              "Optional CSS classes supplied by an integration or theme.",
            omitWhen: ["No integration-specific link styling is required."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          '<p>Read the <a href="https://www.halo.run/">Halo documentation</a>.</p>',
          '<p>Open the <a href="https://example.com/report" target="_blank" rel="noopener noreferrer" title="External report">external report</a>.</p>',
        ],
      },
    };
  },

  addOptions() {
    return {
      ...this.parent?.(),
      ...{
        HTMLAttributes: {
          rel: null,
        },
      },
    };
  },

  renderHTML({ HTMLAttributes }) {
    return ["a", HTMLAttributes, 0];
  },

  addPasteRules() {
    // Remove the function of pasted text parsing as a link
    return [];
  },
}).configure({
  autolink: false,
  openOnClick: false,
});
