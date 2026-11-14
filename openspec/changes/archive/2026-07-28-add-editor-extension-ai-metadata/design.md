## Context

Halo assembles its editor from native Tiptap `Node`, `Mark`, and `Extension` instances, including extensions returned by third-party plugins through `default:editor:extension:create`. The final ProseMirror schema exposes component syntax, but it loses author-supplied semantics such as when to use a code block, how to fill a language attribute, or whether creating an image requires an attachment capability.

`plugin-ai-assistant` currently reconstructs a compact schema description from the live `Editor` and hard-codes exceptional guidance such as `figureCaption` placement. This change moves the reusable declaration and Manifest generation contract into `@halo-dev/richtext-editor`; migrating that plugin remains a separate change.

The metadata is untrusted, advisory input for AI consumers. It is not a runtime component protocol and must never alter Editor initialization, ProseMirror transactions, or document validation.

## Goals / Non-Goals

**Goals:**

- Preserve native Tiptap extension creation, configuration, inheritance, and plugin registration.
- Let every Node or Mark describe its own AI usage and exceptional placement.
- Let any final extension contribute metadata patches to explicitly named final Node or Mark components.
- Resolve a deterministic, versioned Manifest from the final `Editor`, final extensions, and final schema.
- Give every Halo default Node and Mark an explicit AI metadata or `ai: false` decision.
- Keep built-in descriptions and examples aligned with the final runtime behavior, including editor-managed fields and normalization.
- Provide a stable public contract and practical third-party plugin documentation.

**Non-Goals:**

- Enforce AI guidance or structure declarations against editor documents.
- Add an AI write gateway, transaction interceptor, component-role protocol, or separate validation DSL.
- Expose arbitrary prompt fields or allow extensions to inject system prompts.
- Change the existing `AnyExtension[]` editor extension point.
- Modify `plugin-ai-assistant` in this change.
- Add i18n variants for metadata text.
- Introduce general document enforcement or broad editor behavior changes; the only runtime adjustment is parsing Halo's existing serialized `columns` container.

## Decisions

### Public Tiptap hook and types

TypeScript module augmentation will add `addHaloEditorMetadata()` to `NodeConfig`, `MarkConfig`, and `ExtensionConfig` in `@tiptap/core`. Public types and `createHaloEditorManifest(editor)` will be exported from `@halo-dev/richtext-editor`.

The hook returns a local declaration patch. Native `Node.create()`, `Mark.create()`, and `.extend()` remain the only component construction APIs; no Halo-specific Node/Mark factory or extension wrapper will be added.

Node and Mark hooks may return self metadata plus directed contributions. Plain Extension hooks return directed contributions because plain extensions are not Manifest components. A contribution explicitly names one or more `{ kind, name }` targets; wildcard targets are not supported.

### Automatic parent-chain composition

The resolver will walk each final extension's `.extend()` ancestry from parent to child and invoke each locally declared metadata hook using the final extension context. Developers do not call `this.parent` or a merge helper.

Patches compose as follows:

- object fields merge recursively;
- arrays replace inherited arrays;
- `undefined` inherits;
- a higher-precedence `ai: false` disables AI metadata;
- directed contributions accumulate across the parent chain.

After self metadata is composed, directed contributions are applied from lower to higher Tiptap priority. At equal priority, later registration wins for fields it explicitly declares. The resolver emits a development warning when multiple sources overwrite the same final field.

Schema-derived information remains authoritative. Metadata can describe semantics but cannot widen the final schema.

### AI metadata model

A final AI object has a required non-empty English `description` for Halo built-ins and supports:

- `aliases`;
- `exposure`, normalized to `recommended` or the default `available`;
- `useWhen`;
- `avoidWhen`;
- `contentGuidelines`;
- `attributeGuidance`;
- `generation`;
- `examples`.

`attributeGuidance` keys must be attributes present on the final component. Declarations accept a string shorthand or an object with `description`, `format`, `allowedValues`, `examples`, `useWhen`, `omitWhen`, and `guidelines`. Manifest output always normalizes guidance to object form.

`generation.mode` is `direct-html`, `requires-capability`, or `read-only`. Only `requires-capability` accepts a non-empty `requiredCapabilities` array. Capability identifiers are open, non-empty strings; Halo does not register, resolve, or execute them.

Examples are HTML strings under `ai.examples`. When a final component has an AI object but no declared examples, the resolver may generate one from the final schema. No example is generated for `ai: false` or an extension without AI metadata. Declared examples need only parse with the final schema; validation does not require the parsed result to contain the declaring component.

### Self-owned structure declaration

`structure` describes only the current component:

```ts
interface HaloEditorStructureMetadata {
  allowedParents: string[];
  minPerParent?: number;
  maxPerParent?: number;
  description?: string;
}
```

For example, `figureCaption` declares that it belongs under `figure`, is optional, and may occur once. `figure` does not declare or depend on `figureCaption` metadata. No `rules` array, `position`, `allowedChildren`, role system, or executable structure language is introduced.

This information is advisory. Consumers may choose whether or how to use it.

### Final Manifest

`createHaloEditorManifest(editor)` is synchronous and returns a snapshot:

```ts
interface HaloEditorManifest {
  version: 1;
  signature: string;
  components: HaloEditorComponent[];
}
```

Components are unique by `kind + name` and include every final schema Node and Mark, even when no metadata was declared. Component fields are flat.

The Node schema whitelist contains `content`, `group`, `inline`, `atom`, `leaf`, `code`, `whitespace`, `selectable`, `draggable`, `defining`, `isolating`, and attributes. The Mark whitelist contains `excludes`, `inclusive`, `spanning`, and attributes. Both may include serializable static HTML parse rule fields such as `tag`, `style`, and `priority`. Functions and custom NodeSpec fields such as `fakeSelection` and `allowGapCursor` are not emitted.

The Manifest uses stable normalization:

- components sort by kind and name;
- attributes sort by name;
- object keys serialize deterministically;
- author-ordered guidance, example, and HTML parsing arrays preserve order.

The synchronous signature uses Halo UI's existing `object-hash` dependency and covers the normalized complete Manifest except the signature field itself. It is an identity/change detector, not a security primitive.

### Advisory validation and limits

Metadata failures never affect Editor initialization. The resolver keeps valid fields, drops invalid fields or sections, and warns only in development. Unknown runtime keys are discarded through an output whitelist.

Consistency checks include:

- final AI objects require `description`;
- attribute guidance names must exist on the final component;
- contribution targets and structure parents must exist in the final schema;
- HTML examples must parse with the final schema;
- `generation` mode and capability fields must agree;
- structure counts must be non-negative integers with `minPerParent <= maxPerParent`.

Limits are:

- 1,000 characters per guidance text;
- 10 entries per guidance array;
- 10 aliases of at most 100 characters;
- 32 entries for allowed values or attribute-value examples;
- three HTML examples of at most 4 KiB each;
- 16 KiB of explicit AI metadata per component;
- 128 KiB of explicit AI metadata per Manifest.

Schema-derived data does not count against explicit AI metadata limits.

### Built-in coverage

Every Node and Mark in Halo's default `ExtensionsKit` must explicitly resolve to an AI object or `ai: false`; a test prevents accidental `undefined` coverage. Content-facing components receive English descriptions, usage guidance, relevant attribute guidance, generation guidance, and useful examples. Internal-only components may use `ai: false`.

Built-in metadata is audited against the final schema rather than only against the most common toolbar path. When a component has materially different valid forms, its guidance or examples identify those alternatives without attempting a combinatorial example for every attribute. For example, `figure` covers image, video, and audio children; code blocks cover plain and configured forms; and galleries cover every editor-supported layout. Every final schema attribute is either explained for AI use or explicitly identified as editor-managed state that generated content should omit.

Plain extensions that add global attributes, such as indentation or block position, contribute guidance to their explicitly configured target components. Nested `addExtensions()` results and duplicate component names resolve against the final schema identity.

The final semantic audit also checks behavior that is not fully expressed by the schema:

- descriptions distinguish inline nodes from block nodes and primary forms from legacy-normalized forms;
- editor-maintained values such as heading anchors and figure-caption width are identified as values generated content should omit;
- contributed attribute guidance describes the actual rendered CSS effect;
- every built-in direct-HTML example parses to the intended component, and representative examples preserve the attributes that materially distinguish their forms.

The `columns` node already serializes a `div.columns` container with `cols` and `style`, but previously declared no matching container parse rule. It will recognize that existing HTML shape so three-column content remains a three-column layout after import. This is a compatibility correction for an existing format, not a new metadata enforcement mechanism.

### Documentation and downstream use

Public documentation will show:

- a third-party math Node or Mark declaring its own metadata;
- a plugin extending `codeBlock` and returning only its local patch;
- a plain Extension contributing guidance for global attributes;
- `ai: false`, schema-only fallback, limits, and fail-soft behavior;
- synchronous Manifest generation from a live Editor.

Downstream consumers decide how to place the Manifest into model context and whether to enforce any declaration. `plugin-ai-assistant` can later replace its local schema description and hard-coded figure policy with this API.

## Risks / Trade-offs

- **Metadata can still contain misleading prose** → Treat all metadata as untrusted data, whitelist fields, bound sizes, and prohibit prompt-injection fields.
- **Automatic parent traversal may diverge from Tiptap inheritance semantics** → Test `.extend()`, `.configure()`, dynamic options, and nested extension cases against the installed Tiptap version.
- **A complete built-in Manifest consumes model context** → Keep schema fields compact and metadata bounded; catalog/detail splitting remains a future optimization.
- **A deterministic signature can theoretically collide** → Use the existing full-width object hash and document that it is only a cache/change identity.
- **Schema-valid HTML may normalize unexpectedly** → Validation is advisory and fail-soft; consumers still parse edits through the active Editor schema.
- **A container parse rule may match unrelated HTML** → Match the existing `div.columns` class used by Halo serialization rather than arbitrary `div` elements.
- **Public metadata types become plugin-facing API** → Export a versioned Manifest, preserve native Tiptap compatibility, and include third-party examples and regression tests.

## Migration Plan

1. Add public declarations, resolver, Manifest types, and tests without changing existing extension registration.
2. Add explicit metadata decisions to all Halo default Nodes and Marks plus directed contributions from relevant plain extensions.
3. Audit descriptions and examples against runtime parsing, serialization, normalization, and editor-managed fields.
4. Publish plugin author documentation.
5. Let downstream AI plugins adopt `createHaloEditorManifest(editor)` independently while retaining their current fallback on older Halo versions.

Rollback removes the new public exports and declarations; editor content and runtime schemas are unaffected because metadata is advisory.

## Open Questions

None. The public contract and first-version scope were confirmed before implementation.
