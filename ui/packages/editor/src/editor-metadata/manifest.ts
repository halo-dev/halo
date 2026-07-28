import { utf8ByteLength } from "@halo-dev/ui-shared";
import type { AnyExtension, Editor } from "@tiptap/core";
import {
  DOMParser as ProseMirrorDOMParser,
  DOMSerializer,
  Fragment,
  type MarkType,
  type NodeType,
  type Schema,
} from "@tiptap/pm/model";
import { isPlainObject, mergeWith, sortBy, uniqBy } from "es-toolkit";
import objectHash from "object-hash";
import type {
  HaloEditorAIAttributeGuidance,
  HaloEditorAIAttributeGuidanceDeclaration,
  HaloEditorAIGeneration,
  HaloEditorAIMetadata,
  HaloEditorAIMetadataPatch,
  HaloEditorAIAttributeValue,
  HaloEditorComponent,
  HaloEditorComponentMetadataPatch,
  HaloEditorHTMLParseRule,
  HaloEditorManifest,
  HaloEditorMetadataContribution,
  HaloEditorStructureMetadata,
  HaloEditorStructureMetadataPatch,
} from "./types";

const DEFAULT_PRIORITY = 100;
const MAX_TEXT_LENGTH = 1_000;
const MAX_GUIDANCE_ITEMS = 10;
const MAX_ALIAS_LENGTH = 100;
const MAX_ATTRIBUTE_VALUES = 32;
const MAX_HTML_EXAMPLES = 3;
const MAX_HTML_EXAMPLE_BYTES = 4 * 1024;
const MAX_COMPONENT_AI_BYTES = 16 * 1024;
const MAX_MANIFEST_AI_BYTES = 128 * 1024;

interface ResolvedDeclaration {
  self: HaloEditorComponentMetadataPatch;
  contributions: HaloEditorMetadataContribution[];
}

interface ContributionEntry {
  contribution: HaloEditorMetadataContribution;
  priority: number;
  registrationIndex: number;
  sequence: number;
}

type MetadataExtension = AnyExtension & {
  parent: MetadataExtension | null;
  config: AnyExtension["config"] & {
    addHaloEditorMetadata?: (this: Record<string, unknown>) => unknown;
  };
};

export function createHaloEditorManifest(editor: Editor): HaloEditorManifest {
  const extensions = editor.extensionManager.extensions;
  const declarationCache = new Map<AnyExtension, ResolvedDeclaration>();
  const finalExtensions = finalComponentExtensions(extensions, editor.schema);
  const metadata = new Map<string, HaloEditorComponentMetadataPatch>();

  for (const [key, extension] of finalExtensions) {
    metadata.set(
      key,
      resolveDeclaration(extension, editor, declarationCache).self
    );
  }

  const contributions = extensions
    .flatMap((extension, registrationIndex) => {
      const resolved = resolveDeclaration(extension, editor, declarationCache);
      return resolved.contributions.map(
        (contribution, sequence): ContributionEntry => ({
          contribution,
          priority: extensionPriority(extension),
          registrationIndex,
          sequence,
        })
      );
    })
    .sort(
      (left, right) =>
        left.priority - right.priority ||
        left.registrationIndex - right.registrationIndex ||
        left.sequence - right.sequence
    );

  for (const { contribution } of contributions) {
    for (const target of contribution.targets) {
      const key = componentKey(target.kind, target.name);
      if (!componentExists(editor.schema, target.kind, target.name)) {
        warn(
          `Ignored metadata contribution for missing ${target.kind} "${target.name}".`
        );
        continue;
      }
      metadata.set(
        key,
        mergeMetadataPatch(metadata.get(key) ?? {}, contribution.metadata)
      );
    }
  }

  const components = sortBy(
    [
      ...Object.entries(editor.schema.nodes).map(([name, type]) =>
        nodeComponent(editor, type, metadata.get(componentKey("node", name)))
      ),
      ...Object.entries(editor.schema.marks).map(([name, type]) =>
        markComponent(editor, type, metadata.get(componentKey("mark", name)))
      ),
    ],
    ["kind", "name"]
  );

  enforceManifestMetadataLimit(components);

  const unsigned = {
    version: 1 as const,
    components,
  };

  return {
    ...unsigned,
    signature: objectHash(unsigned, {
      unorderedArrays: false,
      unorderedObjects: true,
      unorderedSets: false,
    }),
  };
}

function finalComponentExtensions(extensions: AnyExtension[], schema: Schema) {
  const result = new Map<string, AnyExtension>();
  for (const extension of extensions) {
    if (extension.type !== "node" && extension.type !== "mark") {
      continue;
    }
    if (!componentExists(schema, extension.type, extension.name)) {
      continue;
    }
    result.set(componentKey(extension.type, extension.name), extension);
  }
  return result;
}

function resolveDeclaration(
  extension: AnyExtension,
  editor: Editor,
  cache: Map<AnyExtension, ResolvedDeclaration>
): ResolvedDeclaration {
  const cached = cache.get(extension);
  if (cached) {
    return cached;
  }

  let self: HaloEditorComponentMetadataPatch = {};
  const contributions: HaloEditorMetadataContribution[] = [];
  let previousHook: unknown;

  for (const current of extensionChain(extension)) {
    const hook = current.config.addHaloEditorMetadata;
    if (typeof hook !== "function" || hook === previousHook) {
      previousHook = hook;
      continue;
    }
    previousHook = hook;
    try {
      const declaration = sanitizeDeclaration(
        hook.call(metadataContext(extension, editor)),
        extension.name
      );
      self = mergeMetadataPatch(self, declaration.self);
      contributions.push(...declaration.contributions);
    } catch (error) {
      warn(
        `Ignored metadata hook from extension "${extension.name}" because it threw.`,
        error
      );
    }
  }

  const resolved = { self, contributions };
  cache.set(extension, resolved);
  return resolved;
}

function extensionChain(extension: AnyExtension) {
  const chain: MetadataExtension[] = [];
  let current: MetadataExtension | null = extension as MetadataExtension;
  while (current) {
    chain.unshift(current);
    current = current.parent;
  }
  return chain;
}

function metadataContext(extension: AnyExtension, editor: Editor) {
  const type =
    extension.type === "node"
      ? editor.schema.nodes[extension.name]
      : extension.type === "mark"
        ? editor.schema.marks[extension.name]
        : null;
  return {
    name: extension.name,
    options: extension.options,
    storage:
      (editor.extensionStorage as unknown as Record<string, unknown>)[
        extension.name
      ] ?? extension.storage,
    editor,
    type,
  };
}

function sanitizeDeclaration(
  value: unknown,
  extensionName: string
): ResolvedDeclaration {
  if (!isPlainObject(value)) {
    if (value !== undefined) {
      warn(`Ignored non-object metadata from extension "${extensionName}".`);
    }
    return { self: {}, contributions: [] };
  }

  const contributions: HaloEditorMetadataContribution[] = [];
  if (Array.isArray(value.contributions)) {
    for (const candidate of value.contributions) {
      const contribution = sanitizeContribution(candidate, extensionName);
      if (contribution) {
        contributions.push(contribution);
      }
    }
  } else if (value.contributions !== undefined) {
    warn(`Ignored non-array contributions from extension "${extensionName}".`);
  }

  return {
    self: sanitizeMetadataPatch(value, extensionName),
    contributions,
  };
}

function sanitizeContribution(
  value: unknown,
  extensionName: string
): HaloEditorMetadataContribution | undefined {
  if (!isPlainObject(value) || !Array.isArray(value.targets)) {
    warn(`Ignored invalid metadata contribution from "${extensionName}".`);
    return undefined;
  }

  const targets: HaloEditorMetadataContribution["targets"] =
    value.targets.flatMap((target) => {
      if (
        !isPlainObject(target) ||
        (target.kind !== "node" && target.kind !== "mark") ||
        !validName(target.name)
      ) {
        return [];
      }
      return [{ kind: target.kind, name: target.name }];
    });

  if (!targets.length || !isPlainObject(value.metadata)) {
    warn(`Ignored incomplete metadata contribution from "${extensionName}".`);
    return undefined;
  }

  return {
    targets: uniqueTargets(targets),
    metadata: sanitizeMetadataPatch(value.metadata, extensionName),
  };
}

function sanitizeMetadataPatch(
  value: Record<string, unknown>,
  source: string
): HaloEditorComponentMetadataPatch {
  const result: HaloEditorComponentMetadataPatch = {};
  if (value.ai === false) {
    result.ai = false;
  } else if (isPlainObject(value.ai)) {
    result.ai = sanitizeAIPatch(value.ai, source);
  } else if (value.ai !== undefined) {
    warn(`Ignored invalid AI metadata from "${source}".`);
  }

  if (isPlainObject(value.structure)) {
    result.structure = sanitizeStructurePatch(value.structure, source);
  } else if (value.structure !== undefined) {
    warn(`Ignored invalid structure metadata from "${source}".`);
  }
  return result;
}

function sanitizeAIPatch(
  value: Record<string, unknown>,
  source: string
): HaloEditorAIMetadataPatch {
  const result: HaloEditorAIMetadataPatch = {};
  assignText(result, "description", value.description, source);
  assignStringArray(result, "aliases", value.aliases, source, {
    itemLimit: MAX_ALIAS_LENGTH,
  });
  if (value.exposure === "recommended" || value.exposure === "available") {
    result.exposure = value.exposure;
  } else if (value.exposure !== undefined) {
    warn(`Ignored invalid exposure from "${source}".`);
  }
  assignStringArray(result, "useWhen", value.useWhen, source);
  assignStringArray(result, "avoidWhen", value.avoidWhen, source);
  assignStringArray(
    result,
    "contentGuidelines",
    value.contentGuidelines,
    source
  );

  if (isPlainObject(value.attributeGuidance)) {
    result.attributeGuidance = Object.fromEntries(
      Object.entries(value.attributeGuidance).flatMap(([name, guidance]) => {
        if (!validName(name)) {
          return [];
        }
        const normalized = sanitizeAttributeGuidance(guidance, source);
        return normalized ? [[name, normalized]] : [];
      })
    );
  } else if (value.attributeGuidance !== undefined) {
    warn(`Ignored invalid attribute guidance from "${source}".`);
  }

  if (isPlainObject(value.generation)) {
    result.generation = sanitizeGenerationPatch(value.generation, source);
  } else if (value.generation !== undefined) {
    warn(`Ignored invalid generation metadata from "${source}".`);
  }

  if (Array.isArray(value.examples)) {
    result.examples = sanitizeExamples(value.examples, source);
  } else if (value.examples !== undefined) {
    warn(`Ignored invalid HTML examples from "${source}".`);
  }
  return result;
}

function sanitizeAttributeGuidance(
  value: unknown,
  source: string
): HaloEditorAIAttributeGuidanceDeclaration | undefined {
  if (typeof value === "string") {
    return validText(value, MAX_TEXT_LENGTH, source, "attribute description");
  }
  if (!isPlainObject(value)) {
    warn(`Ignored invalid attribute guidance from "${source}".`);
    return undefined;
  }
  const result: Partial<HaloEditorAIAttributeGuidance> = {};
  assignText(result, "description", value.description, source);
  assignText(result, "format", value.format, source);
  assignAttributeValues(result, "allowedValues", value.allowedValues, source);
  assignAttributeValues(result, "examples", value.examples, source);
  assignStringArray(result, "useWhen", value.useWhen, source);
  assignStringArray(result, "omitWhen", value.omitWhen, source);
  assignStringArray(result, "guidelines", value.guidelines, source);
  return result;
}

function sanitizeGenerationPatch(
  value: Record<string, unknown>,
  source: string
): Partial<HaloEditorAIGeneration> {
  const result: Partial<HaloEditorAIGeneration> = {};
  if (
    value.mode === "direct-html" ||
    value.mode === "requires-capability" ||
    value.mode === "read-only"
  ) {
    result.mode = value.mode;
  } else if (value.mode !== undefined) {
    warn(`Ignored invalid generation mode from "${source}".`);
  }
  assignStringArray(
    result,
    "requiredCapabilities",
    value.requiredCapabilities,
    source
  );
  assignStringArray(result, "guidelines", value.guidelines, source);
  return result;
}

function sanitizeStructurePatch(
  value: Record<string, unknown>,
  source: string
): HaloEditorStructureMetadataPatch {
  const result: HaloEditorStructureMetadataPatch = {};
  assignStringArray(result, "allowedParents", value.allowedParents, source);
  if (value.minPerParent !== undefined) {
    result.minPerParent = value.minPerParent as number;
  }
  if (value.maxPerParent !== undefined) {
    result.maxPerParent = value.maxPerParent as number;
  }
  assignText(result, "description", value.description, source);
  return result;
}

function mergeMetadataPatch(
  base: HaloEditorComponentMetadataPatch,
  patch: HaloEditorComponentMetadataPatch
): HaloEditorComponentMetadataPatch {
  return mergeWith(
    mergeWith({}, base, replaceArrays),
    patch,
    replaceArrays
  ) as HaloEditorComponentMetadataPatch;
}

function replaceArrays(targetValue: unknown, sourceValue: unknown) {
  if (sourceValue === undefined) {
    return targetValue;
  }
  return Array.isArray(sourceValue) ? [...sourceValue] : undefined;
}

function nodeComponent(
  editor: Editor,
  type: NodeType,
  patch: HaloEditorComponentMetadataPatch | undefined
): HaloEditorComponent {
  const component: HaloEditorComponent = {
    kind: "node",
    name: type.name,
    content: type.spec.content ?? "",
    group: type.spec.group ?? "",
    inline: type.isInline,
    atom: type.isAtom,
    leaf: type.isLeaf,
    code: Boolean(type.spec.code),
    whitespace: type.spec.whitespace ?? (type.spec.code ? "pre" : "normal"),
    selectable: type.spec.selectable !== false,
    draggable: Boolean(type.spec.draggable),
    defining: Boolean(type.spec.defining),
    isolating: Boolean(type.spec.isolating),
    attributes: attributes(type.spec.attrs),
    htmlParseRules: htmlParseRules(type.spec.parseDOM),
  };
  return applyMetadata(editor, component, patch, type);
}

function markComponent(
  editor: Editor,
  type: MarkType,
  patch: HaloEditorComponentMetadataPatch | undefined
): HaloEditorComponent {
  const component: HaloEditorComponent = {
    kind: "mark",
    name: type.name,
    excludes: type.spec.excludes ?? "",
    inclusive: type.spec.inclusive !== false,
    spanning: type.spec.spanning !== false,
    attributes: attributes(type.spec.attrs),
    htmlParseRules: htmlParseRules(type.spec.parseDOM),
  };
  return applyMetadata(editor, component, patch, type);
}

function applyMetadata(
  editor: Editor,
  component: HaloEditorComponent,
  patch: HaloEditorComponentMetadataPatch | undefined,
  type: NodeType | MarkType
) {
  if (!patch) {
    return component;
  }
  const ai = normalizeAI(editor, component, patch.ai, type);
  if (ai !== undefined) {
    if (utf8ByteLength(JSON.stringify(ai)) <= MAX_COMPONENT_AI_BYTES) {
      component.ai = ai;
    } else {
      warn(`Ignored oversized AI metadata for "${component.name}".`);
    }
  }
  const structure = normalizeStructure(
    editor.schema,
    component.name,
    patch.structure
  );
  if (structure) {
    component.structure = structure;
  }
  return component;
}

function normalizeAI(
  editor: Editor,
  component: HaloEditorComponent,
  patch: HaloEditorComponentMetadataPatch["ai"],
  type: NodeType | MarkType
): false | HaloEditorAIMetadata | undefined {
  if (patch === false) {
    return false;
  }
  const description =
    patch &&
    validText(
      patch.description,
      MAX_TEXT_LENGTH,
      component.name,
      "description"
    );
  if (!patch || !description) {
    if (patch) {
      warn(
        `Ignored AI metadata without a description for "${component.name}".`
      );
    }
    return undefined;
  }

  const result: HaloEditorAIMetadata = {
    description,
    exposure: patch.exposure ?? "available",
  };
  copyDefined(result, patch, [
    "aliases",
    "useWhen",
    "avoidWhen",
    "contentGuidelines",
  ]);

  const attributeNames = new Set(
    component.attributes.map((attribute) => attribute.name)
  );
  if (patch.attributeGuidance) {
    const guidance = Object.fromEntries(
      Object.entries(patch.attributeGuidance).flatMap(([name, value]) => {
        if (!attributeNames.has(name)) {
          warn(
            `Ignored guidance for missing attribute "${component.name}.${name}".`
          );
          return [];
        }
        const normalized = normalizeAttributeGuidance(value, component.name);
        return normalized ? [[name, normalized]] : [];
      })
    );
    if (Object.keys(guidance).length) {
      result.attributeGuidance = guidance;
    }
  }

  const generation = normalizeGeneration(patch.generation, component.name);
  if (generation) {
    result.generation = generation;
  }

  if (patch.examples !== undefined) {
    result.examples = patch.examples.filter((example) =>
      validHTMLExample(editor.schema, example, component.name)
    );
  } else {
    const example = automaticExample(editor.schema, type);
    if (example) {
      result.examples = [example];
    }
  }
  return result;
}

function normalizeAttributeGuidance(
  value: HaloEditorAIAttributeGuidanceDeclaration,
  source: string
): HaloEditorAIAttributeGuidance | undefined {
  const patch = typeof value === "string" ? { description: value } : value;
  const description = validText(
    patch.description,
    MAX_TEXT_LENGTH,
    source,
    "attribute description"
  );
  if (!description) {
    warn(`Ignored attribute guidance without a description for "${source}".`);
    return undefined;
  }
  const result: HaloEditorAIAttributeGuidance = { description };
  copyDefined(result, patch, [
    "format",
    "allowedValues",
    "examples",
    "useWhen",
    "omitWhen",
    "guidelines",
  ]);
  return result;
}

function normalizeGeneration(
  patch: Partial<HaloEditorAIGeneration> | undefined,
  source: string
): HaloEditorAIGeneration | undefined {
  if (!patch?.mode) {
    if (patch) {
      warn(`Ignored generation metadata without a mode for "${source}".`);
    }
    return undefined;
  }
  if (
    patch.mode === "requires-capability" &&
    !patch.requiredCapabilities?.length
  ) {
    warn(`Ignored generation metadata without capabilities for "${source}".`);
    return undefined;
  }
  if (
    patch.mode !== "requires-capability" &&
    patch.requiredCapabilities !== undefined
  ) {
    warn(`Ignored inconsistent generation metadata for "${source}".`);
    return undefined;
  }
  const result: HaloEditorAIGeneration = { mode: patch.mode };
  copyDefined(result, patch, ["requiredCapabilities", "guidelines"]);
  return result;
}

function normalizeStructure(
  schema: Schema,
  source: string,
  patch: HaloEditorStructureMetadataPatch | undefined
): HaloEditorStructureMetadata | undefined {
  if (!patch) {
    return undefined;
  }
  if (
    !patch.allowedParents?.length ||
    patch.allowedParents.some((parent) => !schema.nodes[parent]) ||
    !validCount(patch.minPerParent) ||
    !validCount(patch.maxPerParent) ||
    (patch.minPerParent !== undefined &&
      patch.maxPerParent !== undefined &&
      patch.minPerParent > patch.maxPerParent)
  ) {
    warn(`Ignored invalid structure metadata for "${source}".`);
    return undefined;
  }
  return {
    allowedParents: [...patch.allowedParents],
    ...(patch.minPerParent === undefined
      ? {}
      : { minPerParent: patch.minPerParent }),
    ...(patch.maxPerParent === undefined
      ? {}
      : { maxPerParent: patch.maxPerParent }),
    ...(patch.description === undefined
      ? {}
      : { description: patch.description }),
  };
}

function attributes(specs: NodeType["spec"]["attrs"]) {
  return Object.entries(specs ?? {})
    .map(([name, spec]) => {
      const required = !("default" in spec);
      const defaultValue = required ? undefined : jsonValue(spec.default);
      return {
        name,
        required,
        ...(defaultValue === undefined ? {} : { defaultValue }),
      };
    })
    .sort((left, right) => left.name.localeCompare(right.name));
}

function htmlParseRules(rules: readonly unknown[] | undefined) {
  return (rules ?? []).flatMap((rule): HaloEditorHTMLParseRule[] => {
    if (!isPlainObject(rule)) {
      return [];
    }
    const result: HaloEditorHTMLParseRule = {};
    if (typeof rule.tag === "string") {
      result.tag = rule.tag;
    }
    if (typeof rule.style === "string") {
      result.style = rule.style;
    }
    if (typeof rule.priority === "number" && Number.isFinite(rule.priority)) {
      result.priority = rule.priority;
    }
    return Object.keys(result).length ? [result] : [];
  });
}

function validHTMLExample(schema: Schema, html: string, source: string) {
  if (typeof document === "undefined") {
    warn(`Ignored HTML example without a DOM for "${source}".`);
    return false;
  }
  try {
    const container = document.createElement("div");
    container.innerHTML = html;
    ProseMirrorDOMParser.fromSchema(schema).parseSlice(container);
    return true;
  } catch (error) {
    warn(`Ignored unparseable HTML example for "${source}".`, error);
    return false;
  }
}

function automaticExample(schema: Schema, type: NodeType | MarkType) {
  if (typeof document === "undefined") {
    return undefined;
  }
  try {
    const serializer = DOMSerializer.fromSchema(schema);
    const container = document.createElement("div");
    if ("isText" in type) {
      if (type.isText || type === schema.topNodeType) {
        return undefined;
      }
      const content = type.isTextblock ? schema.text("Example") : undefined;
      const node = type.createAndFill(null, content) ?? type.createAndFill();
      if (!node) {
        return undefined;
      }
      container.append(serializer.serializeNode(node));
    } else {
      const mark = type.create();
      const text = schema.text("Example", [mark]);
      container.append(serializer.serializeFragment(Fragment.from(text)));
    }
    const html = container.innerHTML;
    return html && utf8ByteLength(html) <= MAX_HTML_EXAMPLE_BYTES
      ? html
      : undefined;
  } catch {
    return undefined;
  }
}

function enforceManifestMetadataLimit(components: HaloEditorComponent[]) {
  let total = 0;
  for (const component of components) {
    if (component.ai === undefined) {
      continue;
    }
    const size = utf8ByteLength(JSON.stringify(component.ai));
    if (total + size > MAX_MANIFEST_AI_BYTES) {
      delete component.ai;
      warn(
        `Ignored AI metadata for "${component.name}" due to Manifest limits.`
      );
      continue;
    }
    total += size;
  }
}

function assignText<T extends object, K extends keyof T>(
  target: T,
  key: K,
  value: unknown,
  source: string
) {
  if (value === undefined) {
    return;
  }
  const normalized = validText(value, MAX_TEXT_LENGTH, source, String(key));
  if (normalized !== undefined) {
    target[key] = normalized as T[K];
  }
}

function assignStringArray<T extends object, K extends keyof T>(
  target: T,
  key: K,
  value: unknown,
  source: string,
  options: { itemLimit?: number } = {}
) {
  if (value === undefined) {
    return;
  }
  if (!Array.isArray(value)) {
    warn(`Ignored invalid ${String(key)} from "${source}".`);
    return;
  }
  if (value.length > MAX_GUIDANCE_ITEMS) {
    warn(`Limited ${String(key)} from "${source}".`);
  }
  const result = value.slice(0, MAX_GUIDANCE_ITEMS).flatMap((item) => {
    const normalized = validText(
      item,
      options.itemLimit ?? MAX_TEXT_LENGTH,
      source,
      String(key)
    );
    return normalized === undefined ? [] : [normalized];
  });
  target[key] = result as T[K];
}

function assignAttributeValues<T extends object, K extends keyof T>(
  target: T,
  key: K,
  value: unknown,
  source: string
) {
  if (value === undefined) {
    return;
  }
  if (!Array.isArray(value)) {
    warn(`Ignored invalid ${String(key)} from "${source}".`);
    return;
  }
  if (value.length > MAX_ATTRIBUTE_VALUES) {
    warn(`Limited ${String(key)} from "${source}".`);
  }
  target[key] = value
    .slice(0, MAX_ATTRIBUTE_VALUES)
    .filter(isAttributeValue) as T[K];
}

function sanitizeExamples(value: unknown[], source: string) {
  if (value.length > MAX_HTML_EXAMPLES) {
    warn(`Limited HTML examples from "${source}".`);
  }
  return value.slice(0, MAX_HTML_EXAMPLES).flatMap((example) => {
    if (
      typeof example !== "string" ||
      !example.trim() ||
      utf8ByteLength(example) > MAX_HTML_EXAMPLE_BYTES
    ) {
      warn(`Ignored invalid HTML example from "${source}".`);
      return [];
    }
    return [example];
  });
}

function validText(
  value: unknown,
  maximum: number,
  source: string,
  field: string
) {
  if (typeof value !== "string" || !value.trim() || value.length > maximum) {
    if (value !== undefined) {
      warn(`Ignored invalid ${field} from "${source}".`);
    }
    return undefined;
  }
  return value;
}

function validCount(value: unknown) {
  return value === undefined || (Number.isInteger(value) && Number(value) >= 0);
}

function validName(value: unknown): value is string {
  return (
    typeof value === "string" && Boolean(value.trim()) && value.length <= 100
  );
}

function isAttributeValue(value: unknown): value is HaloEditorAIAttributeValue {
  return (
    value === null ||
    typeof value === "string" ||
    (typeof value === "number" && Number.isFinite(value)) ||
    typeof value === "boolean"
  );
}

function uniqueTargets(targets: HaloEditorMetadataContribution["targets"]) {
  return uniqBy(targets, (target) => componentKey(target.kind, target.name));
}

function componentExists(schema: Schema, kind: "node" | "mark", name: string) {
  return kind === "node"
    ? Boolean(schema.nodes[name])
    : Boolean(schema.marks[name]);
}

function componentKey(kind: "node" | "mark", name: string) {
  return `${kind}:${name}`;
}

function extensionPriority(extension: AnyExtension) {
  const priority = extension.config.priority;
  return typeof priority === "number" ? priority : DEFAULT_PRIORITY;
}

function copyDefined<T extends object, U extends object>(
  target: T,
  source: U,
  keys: Array<keyof U>
) {
  for (const key of keys) {
    if (source[key] !== undefined) {
      (target as Record<keyof U, unknown>)[key] = source[key];
    }
  }
}

function jsonValue(value: unknown): unknown {
  if (
    value === null ||
    typeof value === "string" ||
    typeof value === "boolean"
  ) {
    return value;
  }
  if (typeof value === "number") {
    return Number.isFinite(value) ? value : undefined;
  }
  if (Array.isArray(value)) {
    return value.map(jsonValue).filter((item) => item !== undefined);
  }
  if (isPlainObject(value)) {
    return Object.fromEntries(
      Object.entries(value).flatMap(([key, item]) => {
        const normalized = jsonValue(item);
        return normalized === undefined ? [] : [[key, normalized]];
      })
    );
  }
  return undefined;
}

function warn(message: string, error?: unknown) {
  if (import.meta.env.DEV) {
    console.warn(`[halo-editor-metadata] ${message}`, error ?? "");
  }
}
