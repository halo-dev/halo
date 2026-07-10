## Context

Halo currently lets plugins render frontend templates through the theme engine, and
`DefaultTemplateNameResolver` can resolve a theme override before falling back to a plugin classpath template. That
solves "can a page render", but it does not provide a standard way for a plugin page to reuse the active theme shell.

The existing resolver behavior also matters for the proposed authoring style. When a plugin-owned template references a
relative template such as `layout`, `PluginClassloaderTemplateResolver` treats the reference as plugin-local because the
owner template is `plugin:<pluginName>:...`. Without special handling, a plugin's own `templates/layout.html` could
shadow the intended theme/core layout contract.

The local theme inventory supports using `templates/layout.html` as a new contract name, but not by treating existing
files as automatically compatible. Among 68 inspected themes, 12 already provide `templates/layout.html`, and their
fragment signatures vary. Compatibility must therefore be detected by contract validation, not by file existence alone.

## Goals / Non-Goals

**Goals:**

- Let plugin authors use a simple `th:replace="~{layout :: html(head = ~{::head}, content = ~{::content})}"` pattern.
- Let active themes provide `templates/layout.html` to wrap contract-aware pages with theme-specific header, footer,
  containers, styles, and mode behavior.
- Provide a system fallback `layout.html` so contract-aware pages still render for themes that do not support the
  contract.
- Expose theme layout compatibility in `Theme.status` so Console can guide users and theme developers.
- Preserve existing themes and plugins by treating layout support as additive.

**Non-Goals:**

- Do not migrate existing theme-specific `modules/layout.html`, `common/layout.html`, or similar internal layout files.
- Do not require all themes to provide the layout contract before they can be installed, upgraded, activated, or used.
- Do not standardize every possible plugin-page slot in the first version; v1 only standardizes `head` and `content`.
- Do not make plugin-local `templates/layout.html` part of the integration contract.

## Decisions

### Use `templates/layout.html` with `html(head, content)` as the v1 contract

The contract name should match the plugin authoring pattern and remain easy to teach. `templates/modules/layout.html`
matches more existing themes, but those layouts are internal implementation details with incompatible signatures and
theme-specific parameters. A new contract using `templates/layout.html` avoids coupling plugins to theme internals.

Alternatives considered:

- `templates/modules/layout.html`: rejected because it implies reuse of existing internal module layouts that often
  require theme-specific parameters.
- `templates/_layout.html` or `templates/plugin-layout.html`: rejected for now because the desired authoring experience
  is the conventional `layout :: html(...)` call, and special resolver handling can protect the contract.

### Add special layout resolution for plugin-owned templates

Introduce a resolver path that only applies when the owner template is plugin-owned and the requested template is the
contract name `layout`. For that case, Halo should resolve:

1. a compatible active-theme `templates/layout.html`;
2. otherwise the system fallback layout from application resources.

The resolver must not let a plugin-local `templates/layout.html` satisfy the theme layout contract. Plugin authors may
still keep any private internal layout templates under non-contract names.

This special handling should be narrowly scoped to the contract template so existing plugin relative-template resolution
continues to behave as it does today.

### Provide a minimal system fallback layout

Add `application/src/main/resources/templates/layout.html` with a fragment equivalent to:

```html
<html th:fragment="html (head, content)">
  <head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <th:block th:if="${head != null}" th:replace="${head}" />
  </head>
  <body>
    <th:block th:replace="${content}" />
    <halo:footer />
  </body>
</html>
```

The fallback should be intentionally plain. It exists for compatibility, not for visual parity with the active theme.
The literal `<head>` element allows existing global head processors to run, and `<halo:footer />` preserves footer code
injection.

### Detect compatibility during theme reconciliation

Theme reconciliation should inspect the theme root and update layout compatibility in Theme status. Detection should:

- treat a missing `templates/layout.html` as unsupported but non-fatal;
- validate that `templates/layout.html` declares the v1 `html(head, content)` fragment signature;
- record a stable state such as `SUPPORTED`, `MISSING`, or `INVALID`, plus a short reason/message;
- keep `Theme.status.phase` independent of layout compatibility unless the theme already fails existing lifecycle checks.

This keeps installation and upgrade behavior backward compatible while giving Console a reliable signal.

### Surface compatibility in Console theme management

Console should display the layout compatibility state for installed themes. Suggested states:

- supported: the theme provides a valid page layout;
- missing: pages using the layout contract will use Halo's fallback layout and may not match the theme visually;
- invalid: the theme's layout contract failed validation and pages may use fallback or fail if forced.

Messages must be localized and should point theme authors to the contract documentation.

## Risks / Trade-offs

- Existing `templates/layout.html` files may not match the new signature -> Compatibility validation prevents file
  existence from being treated as support.
- Special-casing `layout` could surprise plugin authors -> The special case is limited to plugin-owned templates and
  documented as a reserved theme/core contract.
- Static validation can mark a layout as supported even if an unrelated runtime expression later fails -> v1 treats
  `pageLayout` as contract availability, not a full theme render health check.
- The two-slot contract may be too small for some plugins -> v1 keeps the contract minimal; scripts can be placed in
  `head` or `content`, and future versions can add slots after real usage emerges.

## Migration Plan

1. Add core fallback and resolver behavior without requiring theme changes.
2. Add Theme status compatibility reporting and Console display.
3. Document the v1 contract for theme and plugin authors.
4. Update starter or official themes to provide a valid `templates/layout.html` example.
5. Plugins can adopt the contract gradually while retaining their complete fallback pages where needed.

Rollback is low risk because the contract is additive. Reverting the resolver and status fields restores previous plugin
template behavior; themes and plugins that added `layout.html` remain ordinary templates.

## Open Questions

None for the initial proposal. Future revisions can consider adding slots such as `scripts`, `bodyClass`, or
theme-declared layout versions after v1 usage is validated.
