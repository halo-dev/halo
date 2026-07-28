## Why

Halo's active Tiptap schema exposes the syntax of registered editor components but not enough semantic guidance for an AI agent to reliably choose and construct components such as code blocks, tables, media, or plugin-provided nodes. The editor needs a public, plugin-extensible metadata contract that describes how each final component should be used without changing Tiptap runtime behavior.

## What Changes

- Add a public `addHaloEditorMetadata()` hook for Tiptap Node, Mark, and Extension configurations.
- Add public metadata and Manifest types plus a synchronous `createHaloEditorManifest(editor)` API in `@halo-dev/richtext-editor`.
- Resolve metadata from the final configured extension set, automatically merge `.extend()` parent chains, and merge directed contributions from Node, Mark, and plain Extension instances.
- Combine declared AI guidance with a whitelisted description of the final ProseMirror schema, static HTML parsing rules, and stable Manifest identity.
- Keep all metadata advisory: invalid declarations never block Editor initialization, and Halo does not enforce the guidance against editor documents.
- Add explicit English AI metadata or `ai: false` decisions for every Node and Mark in Halo's default `ExtensionsKit`, including practical creation guidance and HTML examples.
- Align built-in guidance with actual editor-managed fields, nesting, normalization, and inline or block behavior.
- Preserve the existing `columns` container attributes when its serialized HTML is parsed so the built-in direct-HTML examples round-trip correctly.
- Document how third-party plugins can describe new components and extend existing components.

## Capabilities

### New Capabilities

- `editor-extension-ai-metadata`: Public editor-extension metadata declarations and deterministic AI capability Manifest generation.

### Modified Capabilities

None.

## Impact

- Affects the public TypeScript API and exports of `ui/packages/editor`.
- Adds metadata declarations to Halo's built-in Tiptap extensions without changing their runtime schema.
- Adds an HTML parse rule for the existing `columns` serialization format so `cols` and container styles are not lost on import.
- Enables downstream AI plugins to replace local schema-description and hard-coded component guidance with Halo-provided data in a separate follow-up change.
- Adds no backend API, dependency, database, security, or generated-client changes.
