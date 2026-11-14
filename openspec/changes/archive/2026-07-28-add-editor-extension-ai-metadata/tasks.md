## 1. Public Metadata Contract

- [x] 1.1 Add public metadata declaration, normalized Manifest, contribution, and module-augmentation types to the editor package
- [x] 1.2 Export the public metadata types and synchronous `createHaloEditorManifest(editor)` API from `@halo-dev/richtext-editor`

## 2. Manifest Resolution

- [x] 2.1 Implement final Node and Mark schema extraction with the approved field and HTML parse-rule whitelist
- [x] 2.2 Implement automatic parent-chain metadata patch composition and explicit Node, Mark, and plain Extension contributions
- [x] 2.3 Implement fail-soft normalization, consistency checks, field whitelisting, size limits, development diagnostics, and automatic AI examples
- [x] 2.4 Implement deterministic component ordering, serialization, and synchronous Manifest signatures

## 3. Halo Built-in Metadata

- [x] 3.1 Add explicit English AI metadata or `ai: false` to every default `ExtensionsKit` Node and Mark
- [x] 3.2 Add self-owned `figureCaption` structure metadata and generation guidance for code, media, table, list, link, and other content-facing components
- [x] 3.3 Add directed metadata contributions for user-facing global attributes from plain Extensions
- [x] 3.4 Audit every default component against its final schema and cover materially distinct forms plus editor-managed attributes
- [x] 3.5 Correct final semantic-audit findings and preserve serialized `columns` container attributes on HTML import

## 4. Tests

- [x] 4.1 Add resolver tests for final schema coverage, nested extensions, duplicate identities, configuration, inheritance, contributions, and precedence
- [x] 4.2 Add normalization and resilience tests for invalid hooks, unknown fields, limits, examples, structure, attributes, and stable signatures
- [x] 4.3 Add a regression test requiring every default Node and Mark to explicitly resolve to AI metadata or `ai: false`
- [x] 4.4 Add a regression audit for complete built-in usage, generation, examples, attribute guidance, and representative variants
- [x] 4.5 Add regression coverage for runtime-aligned guidance and representative built-in HTML round trips

## 5. Documentation

- [x] 5.1 Document new component declarations, existing-component extensions, plain Extension contributions, fallback behavior, limits, and Manifest consumption

## 6. Validation

- [x] 6.1 Run OpenSpec validation and editor-focused unit tests
- [x] 6.2 Run UI formatting, typecheck, lint, and workspace package build
- [x] 6.3 Re-run OpenSpec and UI validation after the built-in metadata audit
- [x] 6.4 Re-run OpenSpec and UI validation after the final semantic corrections
