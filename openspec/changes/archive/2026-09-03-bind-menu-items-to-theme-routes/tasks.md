## 1. Public Menu Model

- [x] 1.1 Add the optional closed `MenuItemSpec.routeRef` enum with stable `archives`, `categories`, and `tags` JSON values and OpenAPI documentation; verify focused API serialization/schema tests pass.
- [x] 1.2 Add model-level test coverage for all route-reference values and backward-compatible deserialization without `routeRef`; verify the focused `api` tests pass.

## 2. Route Resolution and Reconciliation

- [x] 2.1 Resolve valid route-bound menu items from current/default theme route rules while preserving their configured display names; verify focused `MenuItemReconcilerTest` cases cover every route, normalization, defaults, and unchanged custom/resource sources.
- [x] 2.2 Fail closed for conflicting `routeRef` and `targetRef` values or missing route-bound display names without overwriting a last valid status on route-normalization failure; verify focused reconciler tests cover each malformed case.
- [x] 2.3 React to changed archives, categories, and tags rules by requesting reconciliation only for matching route-bound menu items, isolating per-item failures; verify event tests cover affected/unaffected items and failure continuation.

## 3. Generated Contracts

- [x] 3.1 Run `./gradlew generateOpenApiDocs` and verify the generated OpenAPI schema exposes the optional closed `routeRef` values without unrelated contract changes.
- [x] 3.2 Run `pnpm -C ui api-client:gen` and verify the generated MenuItem type matches the OpenAPI contract; do not hand-edit generated documentation or API-client files.

## 4. Console Authoring

- [x] 4.1 Add localized `Article archives`, `Category list`, and `Tag list` source options and default display names for every maintained locale; verify locale keys remain complete and formatting passes.
- [x] 4.2 Support creating route-bound items, switching among built-in routes, and explicitly converting custom links while preserving display names; verify focused Console tests assert the saved `routeRef`, cleared conflicting sources, required name, and hidden link field.
- [x] 4.3 Support unbinding to a custom link by copying the current `status.href` and keep resource-reference types locked; verify focused Console tests cover unbinding, source restrictions, and the hidden derived link.

## 5. Initialization and End-to-End Verification

- [x] 5.1 Change only the new-installation default article menu item to use `routeRef: archives`, leave existing-item migration absent, and verify initialization tests or a focused installation fixture assert the bound default.
- [x] 5.2 Run focused backend tests, `./gradlew spotlessCheck`, OpenAPI consistency checks, `pnpm -C ui typecheck`, `pnpm -C ui lint`, and the focused UI unit tests; verify all checks pass and generated diffs contain only contract-derived changes.
- [x] 5.3 Manually verify create, convert, route-setting change, switch, unbind, existing custom/resource item compatibility, and narrow-width Console behavior; record the observable results in the implementation handoff.
