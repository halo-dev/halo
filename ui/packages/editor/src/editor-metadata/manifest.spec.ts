// @vitest-environment jsdom

import { afterEach, describe, expect, it, vi } from "vitest";
import { ExtensionsKit } from "@/extensions";
import { Editor, Extension, Node, type Extensions } from "@/tiptap";
import { createHaloEditorManifest } from "./manifest";

const Document = Node.create({
  name: "doc",
  topNode: true,
  content: "block+",
  addHaloEditorMetadata() {
    return { ai: false };
  },
});

const Text = Node.create({
  name: "text",
  group: "inline",
  addHaloEditorMetadata() {
    return {
      ai: {
        description: "Text",
      },
    };
  },
});

function createEditor(extensions: Extensions) {
  return new Editor({
    element: null,
    extensions,
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("createHaloEditorManifest", () => {
  it("uses the final configured schema and includes nested components", () => {
    const Nested = Node.create({
      name: "nested",
      group: "block",
      content: "text*",
      addAttributes() {
        return {
          tone: {
            default: this.options.tone,
          },
        };
      },
      addOptions() {
        return {
          tone: "neutral",
        };
      },
      addHaloEditorMetadata() {
        return {
          ai: {
            description: `Nested ${this.options.tone}`,
            attributeGuidance: {
              tone: "Configured tone",
            },
          },
        };
      },
    });
    const Bundle = Extension.create({
      name: "bundle",
      addExtensions() {
        return [Nested.configure({ tone: "warm" })];
      },
    });
    const editor = createEditor([Document, Text, Bundle]);

    const manifest = createHaloEditorManifest(editor);
    const nested = manifest.components.find(
      (component) => component.name === "nested"
    );

    expect(manifest.components.map(({ name }) => name)).toEqual([
      "doc",
      "nested",
      "text",
    ]);
    expect(nested?.attributes).toContainEqual({
      name: "tone",
      required: false,
      defaultValue: "warm",
    });
    expect(nested?.ai).toMatchObject({
      description: "Nested warm",
      exposure: "available",
      attributeGuidance: {
        tone: {
          description: "Configured tone",
        },
      },
    });
    editor.destroy();
  });

  it("composes parent hooks once and lets child arrays replace parent arrays", () => {
    const Base = Node.create({
      name: "paragraph",
      group: "block",
      content: "text*",
      addHaloEditorMetadata() {
        return {
          ai: {
            description: "Base paragraph",
            aliases: ["base"],
            useWhen: ["Base use"],
          },
        };
      },
    });
    const Child = Base.extend({
      addHaloEditorMetadata() {
        return {
          ai: {
            description: "Child paragraph",
            aliases: ["child"],
          },
        };
      },
    }).configure({});
    const editor = createEditor([Document, Text, Child]);

    const paragraph = createHaloEditorManifest(editor).components.find(
      (component) => component.name === "paragraph"
    );

    expect(paragraph?.ai).toMatchObject({
      description: "Child paragraph",
      aliases: ["child"],
      useWhen: ["Base use"],
    });
    editor.destroy();
  });

  it("merges directed contributions by priority and registration order", () => {
    const Paragraph = Node.create({
      name: "paragraph",
      group: "block",
      content: "text*",
      addHaloEditorMetadata() {
        return {
          ai: {
            description: "Paragraph",
            attributeGuidance: {
              tone: "Original tone",
            },
          },
        };
      },
      addAttributes() {
        return {
          tone: {
            default: null,
          },
        };
      },
    });
    const contribution = (
      name: string,
      priority: number,
      description: string
    ) =>
      Extension.create({
        name,
        priority,
        addHaloEditorMetadata() {
          return {
            contributions: [
              {
                targets: [{ kind: "node", name: "paragraph" }],
                metadata: {
                  ai: {
                    attributeGuidance: {
                      tone: description,
                    },
                  },
                },
              },
            ],
          };
        },
      });
    const editor = createEditor([
      Document,
      Text,
      Paragraph,
      contribution("high-first", 200, "High first"),
      contribution("low", 50, "Low"),
      contribution("high-last", 200, "High last"),
    ]);

    const paragraph = createHaloEditorManifest(editor).components.find(
      (component) => component.name === "paragraph"
    );

    expect(
      paragraph?.ai && paragraph.ai.attributeGuidance?.tone.description
    ).toBe("High last");
    editor.destroy();
  });

  it("keeps only the extension that defines the final duplicate identity", () => {
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
    const first = Node.create({
      name: "paragraph",
      group: "block",
      content: "text*",
      addHaloEditorMetadata() {
        return { ai: { description: "First" } };
      },
    });
    const second = first.extend({
      addHaloEditorMetadata() {
        return { ai: { description: "Second" } };
      },
    });
    const editor = createEditor([Document, Text, first, second]);

    const paragraph = createHaloEditorManifest(editor).components.find(
      (component) => component.name === "paragraph"
    );

    expect(paragraph?.ai).toMatchObject({ description: "Second" });
    editor.destroy();
  });

  it("fails soft and drops unknown, invalid, and oversized metadata", () => {
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
    const Invalid = Node.create({
      name: "invalid",
      group: "block",
      content: "text*",
      addAttributes() {
        return {
          valid: {
            default: null,
          },
        };
      },
      parseHTML() {
        return [
          {
            tag: "invalid-example",
            getAttrs() {
              throw new Error("cannot parse example");
            },
          },
        ];
      },
      renderHTML() {
        return ["invalid-example", 0];
      },
      addHaloEditorMetadata() {
        return {
          ai: {
            description: "Valid description",
            aliases: Array.from({ length: 12 }, (_, index) => `alias-${index}`),
            attributeGuidance: {
              valid: {
                description: "Valid attribute",
                allowedValues: Array.from({ length: 40 }, (_, index) => index),
              },
              missing: "Missing attribute",
            },
            generation: {
              mode: "requires-capability",
            },
            examples: [
              "<invalid-example>Example</invalid-example>",
              "x".repeat(4097),
            ],
            systemPrompt: "ignored",
          },
          structure: {
            allowedParents: ["missing-parent"],
          },
          unknown: true,
        } as never;
      },
    });
    const Throwing = Node.create({
      name: "throwing",
      group: "block",
      content: "text*",
      addHaloEditorMetadata() {
        throw new Error("metadata failure");
      },
    });
    const editor = createEditor([Document, Text, Invalid, Throwing]);

    const manifest = createHaloEditorManifest(editor);
    const invalid = manifest.components.find(
      (component) => component.name === "invalid"
    );
    const throwing = manifest.components.find(
      (component) => component.name === "throwing"
    );

    expect(invalid?.ai).toMatchObject({
      description: "Valid description",
      aliases: Array.from({ length: 10 }, (_, index) => `alias-${index}`),
      attributeGuidance: {
        valid: {
          description: "Valid attribute",
          allowedValues: Array.from({ length: 32 }, (_, index) => index),
        },
      },
      examples: [],
    });
    expect(invalid).not.toHaveProperty("structure");
    expect(invalid?.ai).not.toHaveProperty("systemPrompt");
    expect(invalid?.ai).not.toHaveProperty("generation");
    expect(throwing).not.toHaveProperty("ai");
    editor.destroy();
  });

  it("normalizes figureCaption structure and produces stable signatures", () => {
    const Figure = Node.create({
      name: "figure",
      group: "block",
      content: "figureCaption?",
      addHaloEditorMetadata() {
        return {
          ai: {
            description: "Figure",
          },
        };
      },
    });
    const FigureCaption = Node.create({
      name: "figureCaption",
      group: "block",
      content: "text*",
      addHaloEditorMetadata() {
        return {
          ai: {
            description: "Figure caption",
          },
          structure: {
            allowedParents: ["figure"],
            minPerParent: 0,
            maxPerParent: 1,
          },
        };
      },
    });
    const editor = createEditor([Document, Text, Figure, FigureCaption]);

    const first = createHaloEditorManifest(editor);
    const second = createHaloEditorManifest(editor);
    const caption = first.components.find(
      (component) => component.name === "figureCaption"
    );

    expect(first.signature).toBe(second.signature);
    expect(caption?.structure).toEqual({
      allowedParents: ["figure"],
      minPerParent: 0,
      maxPerParent: 1,
    });
    editor.destroy();
  });

  it("changes the signature only when normalized schema or metadata changes", () => {
    const paragraph = (description: string) =>
      Node.create({
        name: "paragraph",
        group: "block",
        content: "text*",
        addHaloEditorMetadata() {
          return {
            ai: {
              description,
            },
          };
        },
      });
    const UnrelatedOne = Extension.create({ name: "unrelated-one" });
    const UnrelatedTwo = Extension.create({ name: "unrelated-two" });
    const firstEditor = createEditor([
      Document,
      Text,
      paragraph("Paragraph"),
      UnrelatedOne,
      UnrelatedTwo,
    ]);
    const reorderedEditor = createEditor([
      Document,
      Text,
      paragraph("Paragraph"),
      UnrelatedTwo,
      UnrelatedOne,
    ]);
    const changedEditor = createEditor([
      Document,
      Text,
      paragraph("Changed paragraph"),
    ]);

    const first = createHaloEditorManifest(firstEditor);
    const reordered = createHaloEditorManifest(reorderedEditor);
    const changed = createHaloEditorManifest(changedEditor);

    expect(first).toEqual(reordered);
    expect(changed.signature).not.toBe(first.signature);
    firstEditor.destroy();
    reorderedEditor.destroy();
    changedEditor.destroy();
  });

  it("keeps schema components when component or Manifest AI limits are exceeded", () => {
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
    const oversizedComponent = Node.create({
      name: "oversized",
      group: "block",
      content: "text*",
      addHaloEditorMetadata() {
        return {
          ai: {
            description: "Oversized component",
            useWhen: Array.from({ length: 10 }, () => "u".repeat(1000)),
            avoidWhen: Array.from({ length: 10 }, () => "a".repeat(1000)),
          },
        };
      },
    });
    const bulkComponents = Array.from({ length: 14 }, (_, index) =>
      Node.create({
        name: `bulk-${index}`,
        group: "block",
        content: "text*",
        addHaloEditorMetadata() {
          return {
            ai: {
              description: `Bulk ${index}`,
              contentGuidelines: Array.from({ length: 10 }, () =>
                "g".repeat(950)
              ),
            },
          };
        },
      })
    );
    const editor = createEditor([
      Document,
      Text,
      oversizedComponent,
      ...bulkComponents,
    ]);

    const manifest = createHaloEditorManifest(editor);
    const oversized = manifest.components.find(
      (component) => component.name === "oversized"
    );
    const bulk = manifest.components.filter(({ name }) =>
      name.startsWith("bulk-")
    );

    expect(oversized).toBeDefined();
    expect(oversized).not.toHaveProperty("ai");
    expect(bulk).toHaveLength(14);
    expect(bulk.some((component) => component.ai === undefined)).toBe(true);
    editor.destroy();
  });

  it("gives every default schema component an explicit AI declaration", () => {
    const warning = vi
      .spyOn(console, "warn")
      .mockImplementation(() => undefined);
    const editor = createEditor([ExtensionsKit]);

    const manifest = createHaloEditorManifest(editor);
    const missing = manifest.components
      .filter((component) => component.ai === undefined)
      .map(({ kind, name }) => `${kind}:${name}`);

    expect(missing).toEqual([]);
    expect(
      warning.mock.calls.filter(([message]) =>
        String(message).startsWith("[halo-editor-metadata]")
      )
    ).toEqual([]);
    editor.destroy();
  });

  it("keeps every active default component useful and covers representative variants", () => {
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
    const editor = createEditor([ExtensionsKit]);
    const manifest = createHaloEditorManifest(editor);

    for (const component of manifest.components) {
      if (component.ai === false) {
        continue;
      }
      expect(component.ai, `${component.kind}:${component.name}`).toBeDefined();
      if (!component.ai) {
        continue;
      }
      expect(
        component.ai.useWhen?.length,
        `${component.kind}:${component.name} useWhen`
      ).toBeGreaterThan(0);
      expect(
        component.ai.generation,
        `${component.kind}:${component.name} generation`
      ).toBeDefined();
      expect(
        component.ai.examples?.length,
        `${component.kind}:${component.name} examples`
      ).toBeGreaterThan(0);
      expect(
        Object.keys(component.ai.attributeGuidance ?? {}).sort(),
        `${component.kind}:${component.name} attributeGuidance`
      ).toEqual(component.attributes.map(({ name }) => name).sort());
    }

    const examples = (name: string) => {
      const component = manifest.components.find(
        (component) => component.name === name
      );
      if (!component?.ai) {
        throw new Error(`Missing AI metadata for ${name}`);
      }
      return component.ai.examples?.join("\n") ?? "";
    };

    expect(examples("figure")).toContain("<img");
    expect(examples("figure")).toContain("<video");
    expect(examples("figure")).toContain("<audio");
    expect(examples("codeBlock")).toContain("theme=");
    expect(examples("codeBlock")).toContain("collapsed=");
    expect(examples("columns")).toContain('cols="2"');
    expect(examples("columns")).toContain('cols="3"');
    expect(examples("details")).toContain("<details>");
    expect(examples("details")).toContain("<details open>");
    expect(examples("gallery")).toContain('data-layout="auto"');
    expect(examples("gallery")).toContain('data-layout="square"');
    expect(examples("heading")).toContain("<h1");
    expect(examples("heading")).toContain("<h2");
    expect(examples("heading")).toContain("<h3");
    expect(examples("link")).toContain('target="_blank"');
    expect(examples("link")).toContain('title="External report"');
    expect(examples("listItem")).toContain("<ul>");
    expect(examples("listItem")).toContain("<ol>");
    expect(examples("listItem")).toContain("<blockquote>");
    expect(examples("orderedList")).toContain('start="3"');
    expect(examples("orderedList")).toContain('type="I"');
    expect(examples("taskItem")).toContain('data-checked="true"');
    expect(examples("taskItem")).toContain('data-checked="false"');
    expect(examples("table")).toContain("colspan=");
    expect(examples("table")).toContain("rowspan=");
    expect(examples("highlight")).toContain("<mark>");
    expect(examples("highlight")).toContain("data-color=");
    expect(examples("textStyle")).toContain("background-color:");
    expect(examples("textStyle")).toContain("font-family:");
    expect(examples("textStyle")).toContain("line-height:");
    expect(examples("audio")).toContain("loop");
    expect(examples("video")).toContain('height="360px"');
    expect(examples("image")).toContain('width="640px"');

    editor.destroy();
  });

  it("keeps built-in guidance aligned with editor-managed and normalized behavior", () => {
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
    const editor = createEditor([ExtensionsKit]);
    const manifest = createHaloEditorManifest(editor);
    const component = (name: string) => {
      const value = manifest.components.find(
        (component) => component.name === name
      );
      const ai = value?.ai;
      if (!value || !ai) {
        throw new Error(`Missing AI metadata for ${name}`);
      }
      return { component: value, ai };
    };

    expect(
      component("figure").ai.attributeGuidance?.alignItems.description
    ).toBe("Cross-axis alignment of this container's child content.");
    expect(component("heading").ai.attributeGuidance?.id.omitWhen).toContain(
      "Generating or editing article content; the editor maintains this value."
    );
    expect(
      component("figureCaption").ai.attributeGuidance?.width.omitWhen
    ).toContain(
      "Generating or editing article content; the editor maintains this value."
    );

    const iframe = component("iframe");
    expect(iframe.component.kind).toBe("node");
    expect(iframe.component.kind === "node" && iframe.component.inline).toBe(
      true
    );
    expect(iframe.ai.description).toContain("inline iframe");
    expect(iframe.ai.examples?.[0]).toContain("<p><iframe");

    for (const name of ["image", "video"]) {
      const media = component(name);
      expect(media.ai.description).toContain(
        "normally used as the media child of a figure"
      );
      expect(
        media.ai.examples?.every((example) => example.startsWith("<figure"))
      ).toBe(true);
    }

    editor.destroy();
  });

  it("round-trips distinguishing columns attributes from serialized HTML", () => {
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
    const editor = createEditor([ExtensionsKit]);

    editor.commands.setContent(
      '<div class="columns" cols="3" style="display: flex; gap: 2em;"><div class="column" index="0"><p>First</p></div><div class="column" index="1"><p>Second</p></div><div class="column" index="2"><p>Third</p></div></div>'
    );

    const columns = editor.state.doc.firstChild;
    expect(columns?.type.name).toBe("columns");
    expect(columns?.attrs.cols).toBe(3);
    expect(columns?.attrs.style).toContain("gap: 2em");
    expect(
      columns?.content.content.map((column) => column.attrs.index)
    ).toEqual([0, 1, 2]);

    const manifestColumns = createHaloEditorManifest(editor).components.find(
      (component) => component.name === "columns"
    );
    expect(manifestColumns?.htmlParseRules).toContainEqual({
      tag: "div.columns",
    });
    expect(editor.getHTML()).toContain('cols="3"');

    editor.destroy();
  });
});
