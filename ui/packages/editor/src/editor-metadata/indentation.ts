import {
  callOrReturn,
  getExtensionField,
  type AnyExtension,
  type Editor,
  type NodeConfig,
  type ParentConfig,
} from "@/tiptap";
import type { NodeType } from "@/tiptap/pm";

export const DEFAULT_INDENT_RANGE = 24;
export const DEFAULT_MIN_INDENT_LEVEL = 0;
export const DEFAULT_INDENT_LEVEL_COUNT = 10;

export const INDENT_TRANSACTION_META = "haloIndentTransaction";
export const DROP_INDENT_TRANSACTION_META = "haloDropIndentApplied";

export interface HaloEditorIndentationOptions {
  indentRange: number;
  minIndentLevel: number;
  maxIndentLevel: number | null;
  defaultIndentLevel: number;
}

export interface HaloEditorIndentationSettings {
  indentRange: number;
  minIndentLevel: number;
  maxIndentLevel: number;
  defaultIndentLevel: number;
}

export interface HaloEditorNodeIndentationMetadata {
  /**
   * Let another keyboard handler process Tab/Shift-Tab while the text cursor
   * is inside this node. The node itself can still be indented through a node
   * selection or a leading gap cursor.
   */
  keyboard?: "block" | "passthrough";

  /** Keep parsing the legacy first-line indentation attribute for this node. */
  legacyLineIndent?: boolean;
}

export interface ResolvedHaloEditorNodeIndentationMetadata extends HaloEditorNodeIndentationMetadata {
  enabled: boolean;
  keyboard: "block" | "passthrough";
  legacyLineIndent: boolean;
  settings: HaloEditorIndentationSettings;
}

declare module "@tiptap/core" {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  interface NodeConfig<Options = any, Storage = any> {
    /**
     * Describes how Halo's generic block indentation integrates with a node.
     * Block-group nodes are indentable by default. Set this to `false` to opt
     * out, or provide metadata for special keyboard/legacy behavior.
     */
    haloEditorIndentation?:
      | boolean
      | HaloEditorNodeIndentationMetadata
      | ((this: {
          name: string;
          options: Options;
          storage: Storage;
          parent: ParentConfig<
            NodeConfig<Options, Storage>
          >["haloEditorIndentation"];
        }) => boolean | HaloEditorNodeIndentationMetadata);
  }
}

declare module "@tiptap/pm/model" {
  interface NodeSpec {
    haloEditorIndentation?: ResolvedHaloEditorNodeIndentationMetadata;
  }
}

export function getHaloEditorIndentationSettings(
  editor: Editor
): HaloEditorIndentationSettings {
  for (const type of Object.values(editor.schema.nodes)) {
    const metadata = type.spec.haloEditorIndentation;
    if (metadata) {
      return metadata.settings;
    }
  }
  return defaultIndentationSettings();
}

export function resolveHaloEditorIndentationSettings(
  options: HaloEditorIndentationOptions
): HaloEditorIndentationSettings {
  const indentRange = positiveFiniteOr(
    options.indentRange,
    DEFAULT_INDENT_RANGE
  );
  const minIndentLevel = finiteOr(
    options.minIndentLevel,
    DEFAULT_MIN_INDENT_LEVEL
  );
  const configuredMax =
    options.maxIndentLevel === null
      ? minIndentLevel + indentRange * DEFAULT_INDENT_LEVEL_COUNT
      : finiteOr(options.maxIndentLevel, minIndentLevel);
  const maxIndentLevel = Math.max(minIndentLevel, configuredMax);
  const defaultIndentLevel = clampIndentLevel(
    finiteOr(options.defaultIndentLevel, minIndentLevel),
    { minIndentLevel, maxIndentLevel }
  );
  return {
    indentRange,
    minIndentLevel,
    maxIndentLevel,
    defaultIndentLevel,
  };
}

export function resolveNodeIndentationMetadata(
  extension: AnyExtension,
  configuredNames: readonly string[] | null = null,
  settings: HaloEditorIndentationSettings = defaultIndentationSettings()
): ResolvedHaloEditorNodeIndentationMetadata {
  if (extension.type !== "node") {
    return disabledNodeIndentation(settings);
  }

  const context = {
    name: extension.name,
    options: extension.options,
    storage: extension.storage,
  };
  const declaration = callOrReturn(
    getExtensionField<NodeConfig["haloEditorIndentation"]>(
      extension,
      "haloEditorIndentation",
      context
    )
  );
  const group = callOrReturn(
    getExtensionField<NodeConfig["group"]>(extension, "group", context)
  );
  const groups = typeof group === "string" ? group.split(" ") : [];
  const inferredEnabled = groups.includes("block") && !groups.includes("list");
  const enabled = configuredNames
    ? configuredNames.includes(extension.name)
    : declaration === false
      ? false
      : declaration === true || typeof declaration === "object"
        ? true
        : inferredEnabled;
  const metadata =
    declaration && typeof declaration === "object" ? declaration : {};

  return {
    enabled,
    keyboard: metadata.keyboard ?? "block",
    legacyLineIndent: metadata.legacyLineIndent ?? false,
    settings,
  };
}

export function getNodeIndentationMetadata(
  type: NodeType
): ResolvedHaloEditorNodeIndentationMetadata {
  return type.spec.haloEditorIndentation ?? disabledNodeIndentation();
}

export function isNodeIndentable(type: NodeType) {
  return getNodeIndentationMetadata(type).enabled;
}

export function clampIndentLevel(
  value: number,
  settings: Pick<
    HaloEditorIndentationSettings,
    "minIndentLevel" | "maxIndentLevel"
  >
) {
  return Math.min(
    Math.max(value, settings.minIndentLevel),
    settings.maxIndentLevel
  );
}

export function indentLevelToValue(
  level: number,
  settings: Pick<
    HaloEditorIndentationSettings,
    "indentRange" | "minIndentLevel" | "maxIndentLevel"
  >
) {
  return clampIndentLevel(level * settings.indentRange, settings);
}

function disabledNodeIndentation(
  settings: HaloEditorIndentationSettings = defaultIndentationSettings()
): ResolvedHaloEditorNodeIndentationMetadata {
  return {
    enabled: false,
    keyboard: "block",
    legacyLineIndent: false,
    settings,
  };
}

function defaultIndentationSettings(): HaloEditorIndentationSettings {
  return resolveHaloEditorIndentationSettings({
    indentRange: DEFAULT_INDENT_RANGE,
    minIndentLevel: DEFAULT_MIN_INDENT_LEVEL,
    maxIndentLevel: null,
    defaultIndentLevel: DEFAULT_MIN_INDENT_LEVEL,
  });
}

function finiteOr(value: number, fallback: number) {
  return Number.isFinite(value) ? value : fallback;
}

function positiveFiniteOr(value: number, fallback: number) {
  return Number.isFinite(value) && value > 0 ? value : fallback;
}
