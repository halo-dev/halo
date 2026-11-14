import type { Editor } from "@tiptap/core";
import type { MarkType, NodeType } from "@tiptap/pm/model";

export type HaloEditorAIAttributeValue = string | number | boolean | null;

export interface HaloEditorAIAttributeGuidance {
  description: string;
  format?: string;
  allowedValues?: HaloEditorAIAttributeValue[];
  examples?: HaloEditorAIAttributeValue[];
  useWhen?: string[];
  omitWhen?: string[];
  guidelines?: string[];
}

export type HaloEditorAIAttributeGuidanceDeclaration =
  | string
  | Partial<HaloEditorAIAttributeGuidance>;

export interface HaloEditorAIGeneration {
  mode: "direct-html" | "requires-capability" | "read-only";
  requiredCapabilities?: string[];
  guidelines?: string[];
}

export interface HaloEditorAIMetadata {
  description: string;
  aliases?: string[];
  exposure: "recommended" | "available";
  useWhen?: string[];
  avoidWhen?: string[];
  contentGuidelines?: string[];
  attributeGuidance?: Record<string, HaloEditorAIAttributeGuidance>;
  generation?: HaloEditorAIGeneration;
  examples?: string[];
}

export interface HaloEditorAIMetadataPatch {
  description?: string;
  aliases?: string[];
  exposure?: "recommended" | "available";
  useWhen?: string[];
  avoidWhen?: string[];
  contentGuidelines?: string[];
  attributeGuidance?: Record<string, HaloEditorAIAttributeGuidanceDeclaration>;
  generation?: Partial<HaloEditorAIGeneration>;
  examples?: string[];
}

export interface HaloEditorStructureMetadata {
  allowedParents: string[];
  minPerParent?: number;
  maxPerParent?: number;
  description?: string;
}

export type HaloEditorStructureMetadataPatch =
  Partial<HaloEditorStructureMetadata>;

export interface HaloEditorComponentMetadataPatch {
  ai?: false | HaloEditorAIMetadataPatch;
  structure?: HaloEditorStructureMetadataPatch;
}

export interface HaloEditorComponentTarget {
  kind: "node" | "mark";
  name: string;
}

export interface HaloEditorMetadataContribution {
  targets: HaloEditorComponentTarget[];
  metadata: HaloEditorComponentMetadataPatch;
}

export interface HaloEditorMetadataDeclaration extends HaloEditorComponentMetadataPatch {
  contributions?: HaloEditorMetadataContribution[];
}

export interface HaloEditorExtensionMetadataDeclaration {
  contributions?: HaloEditorMetadataContribution[];
}

export interface HaloEditorAttribute {
  name: string;
  required: boolean;
  defaultValue?: unknown;
}

export interface HaloEditorHTMLParseRule {
  tag?: string;
  style?: string;
  priority?: number;
}

interface HaloEditorComponentBase {
  name: string;
  attributes: HaloEditorAttribute[];
  htmlParseRules: HaloEditorHTMLParseRule[];
  ai?: false | HaloEditorAIMetadata;
  structure?: HaloEditorStructureMetadata;
}

export interface HaloEditorNodeComponent extends HaloEditorComponentBase {
  kind: "node";
  content: string;
  group: string;
  inline: boolean;
  atom: boolean;
  leaf: boolean;
  code: boolean;
  whitespace: "normal" | "pre";
  selectable: boolean;
  draggable: boolean;
  defining: boolean;
  isolating: boolean;
}

export interface HaloEditorMarkComponent extends HaloEditorComponentBase {
  kind: "mark";
  excludes: string;
  inclusive: boolean;
  spanning: boolean;
}

export type HaloEditorComponent =
  | HaloEditorNodeComponent
  | HaloEditorMarkComponent;

export interface HaloEditorManifest {
  version: 1;
  signature: string;
  components: HaloEditorComponent[];
}

export interface HaloEditorMetadataContext<
  Options = unknown,
  Storage = unknown,
  Type = NodeType | MarkType | null,
> {
  name: string;
  options: Options;
  storage: Storage;
  editor: Editor;
  type: Type;
}

declare module "@tiptap/core" {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  interface NodeConfig<Options = any, Storage = any> {
    addHaloEditorMetadata?: (
      this: HaloEditorMetadataContext<Options, Storage, NodeType>
    ) => HaloEditorMetadataDeclaration | undefined;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  interface MarkConfig<Options = any, Storage = any> {
    addHaloEditorMetadata?: (
      this: HaloEditorMetadataContext<Options, Storage, MarkType>
    ) => HaloEditorMetadataDeclaration | undefined;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  interface ExtensionConfig<Options = any, Storage = any> {
    addHaloEditorMetadata?: (
      this: HaloEditorMetadataContext<Options, Storage, null>
    ) => HaloEditorExtensionMetadataDeclaration | undefined;
  }
}
