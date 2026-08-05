import { TableRow as BuiltInTableRow } from "@tiptap/extension-table";

const TableRow = BuiltInTableRow.extend({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A row of header or data cells inside a table. It is not a top-level content block.",
        aliases: ["tr"],
        exposure: "recommended",
        useWhen: ["Adding one record or header row to a table."],
        avoidWhen: ["There is no containing table."],
        attributeGuidance: {
          style: {
            description:
              "CSS declarations for row presentation, including height.",
            format: "CSS declarations",
            examples: ["height: 60px;"],
            omitWhen: ["Default row sizing is appropriate."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<table><tbody><tr><td><p>Value</p></td></tr></tbody></table>",
          "<table><tbody><tr><th><p>Label</p></th><th><p>Status</p></th></tr></tbody></table>",
        ],
      },
      structure: {
        allowedParents: ["table"],
        minPerParent: 1,
        description:
          "tableRow may appear only inside table, and each table contains at least one row.",
      },
    };
  },

  addAttributes() {
    return {
      style: {
        default: "height: 60px;",
        parseHTML: (element: HTMLElement) => element.getAttribute("style"),
      },
    };
  },
});

export default TableRow;
