## 1. Backend Layout Contract

- [x] 1.1 Add the system fallback `application/src/main/resources/templates/layout.html` with `html(head, content)`.
- [x] 1.2 Add focused tests for the system fallback layout rendering with both `head` and `content` fragments.
- [x] 1.3 Implement special `layout` resolution for plugin-owned templates so theme/core layout is preferred over plugin-local `templates/layout.html`.
- [x] 1.4 Add resolver tests covering compatible theme layout, missing theme layout fallback, and plugin-local layout not being selected.
- [x] 1.5 Verify existing plugin-relative template resolution is unchanged for non-contract template names.

## 2. Theme Compatibility Status

- [x] 2.1 Extend `Theme.status` with a layout compatibility state for `SUPPORTED`, `MISSING`, and `INVALID`.
- [x] 2.2 Implement theme layout compatibility detection for `templates/layout.html` during Theme reconciliation.
- [x] 2.3 Validate the v1 `html(head, content)` fragment signature without pre-rendering theme layouts.
- [x] 2.4 Record short diagnostic reasons for missing and invalid layout states.
- [x] 2.5 Add reconciler tests for supported, missing, invalid, upgrade, and reload scenarios.

## 3. Console Integration

- [x] 3.1 Regenerate OpenAPI docs and the UI API client after Theme status changes.
- [x] 3.2 Display page layout compatibility state in Console theme management.
- [x] 3.3 Add localized copy for supported, missing, and invalid layout states.
- [x] 3.4 Add or update Console tests for rendering the compatibility indicators and warning text.

## 4. Documentation And Examples

- [x] 4.1 Document the `templates/layout.html` v1 contract for theme authors.
- [x] 4.2 Document the plugin authoring pattern using `layout :: html(head = ~{::head}, content = ~{::content})`.
- [x] 4.3 Update a starter or official theme example with a valid contract layout.
- [x] 4.4 Document backward compatibility and fallback behavior for existing themes.

## 5. Validation

- [x] 5.1 Run backend formatting and relevant backend tests.
- [x] 5.2 Run OpenAPI generation and UI API client generation.
- [ ] 5.3 Run frontend typecheck, lint, and relevant unit tests.
  - Passed: `pnpm -C ui exec vue-tsc --noEmit -p tsconfig.app.json --composite false`, `pnpm -C ui lint`, and `pnpm -C ui test:unit -- console-src/modules/interface/themes/utils/__tests__/page-layout.spec.ts`.
  - Full `pnpm -C ui typecheck` remains blocked by existing editor package `prosemirror-*` resolution errors.
- [ ] 5.4 Manually verify one compatible theme and one missing-layout theme show the expected Console status.
  - Covered by reconciler tests for supported and missing status; no live Console server verification was run.
