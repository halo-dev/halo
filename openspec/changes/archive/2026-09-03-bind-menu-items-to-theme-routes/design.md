## Context

See `proposal.md` for motivation. `MenuItemSpec` currently represents either a direct `href` or an Extension `targetRef`; `MenuItemReconciler` resolves either source into `MenuItemStatus`, which themes consume. Theme route rules are mutable, and `SystemConfigChangedEvent` already drives route and permalink refresh work.

The new source must represent a stable semantic binding rather than a copied path. It must also preserve existing custom links, Extension references, and the current eventual-consistency model.

## Goals / Non-Goals

**Goals:**

- Put built-in route identity in the public menu-item model and keep rendered paths derived.
- Reuse the existing reconciliation and system-configuration event seams.
- Make binding, switching, and unbinding explicit and reversible in Console.
- Keep malformed externally written data from silently resolving through the wrong source.

**Non-Goals:**

- Automatically migrate custom links that happen to match a built-in route.
- Let plugins register route-reference values.
- Make post, page, category, and tag resource references freely interchangeable while editing.
- Add an endpoint, permission, dependency, or database migration.
- Guarantee synchronous menu refresh before the route-settings update returns.

## Decisions

### Add an explicit scalar route reference

Add an optional `routeRef` to `MenuItemSpec`, backed by an enum whose stable JSON values are `archives`, `categories`, and `tags`. The field is distinct from `targetRef`, because `targetRef` identifies a real Extension, and distinct from `href`, because a route binding is not a URL snapshot.

An object wrapper and an extensible string were rejected because the first version has only three closed values and no additional per-binding data. Annotation-based metadata and virtual Extension references were rejected as hidden or misleading contracts.

### Keep one authoritative link source

For valid route-bound items, `routeRef` determines `status.href` and `spec.displayName` determines `status.displayName`; `spec.href` is not maintained as a fallback. Existing `targetRef` items continue to resolve from their referenced Extension, and existing custom items continue to copy `spec.href` into status.

`routeRef` and `targetRef` together are invalid. A route binding without a display name is also invalid. Console prevents those combinations. Because the generic Extension write path has no MenuItem-specific conditional-validation seam, the OpenAPI schema validates the closed enum while the reconciler defensively clears derived status and logs malformed cross-field combinations. Introducing a generic validator or restructuring `MenuItemSpec` as an OpenAPI `oneOf` is outside this change.

### Resolve through the existing theme-route configuration

Read the default and current system ConfigMaps through the reconciler's existing synchronous Extension client, merge them with the existing configuration utility, and use the same route defaults and normalization rules as the theme router. This keeps route matching and generated menu links consistent without adding a Reactor blocking call.

If current rules cannot be normalized, reconciliation must not overwrite a previously valid status with an invalid path. A newly created malformed binding may remain without derived status until configuration is corrected.

### Reconcile bindings after relevant configuration changes

Handle `SystemConfigChangedEvent`, compare the old and new archives, categories, and tags rules, and request reconciliation only for menu items bound to a changed route. Route changes are rare and menu collections are small, so reuse the existing full-list-and-filter pattern instead of adding an index. If scale makes this measurable, `spec.routeRef` can be indexed later without changing the public behavior.

Process update requests independently so one failed menu update is logged and skipped without cancelling the remaining requests. Normal controller synchronization, later menu updates, or restart-time synchronization can recover the failed item.

### Treat the Console selector as a link-source selector

Add the three system routes to the existing source-type selection model. New items may choose any current source. While editing, custom links and built-in route bindings may switch among those four choices; existing post, page, category, and tag references remain locked as today.

New route bindings receive localized default names, but names remain editable and are not changed when switching or converting. Route-bound items do not expose the derived path in the editor. Unbinding copies the current `status.href` into `spec.href`, preserving a working custom link.

Keep this behavior inside the existing menu-item editing module; it does not justify a new shared module or composable.

### Change initialization without migrating existing records

Replace the new-installation default article menu item's literal archives URL with `routeRef: archives`. Do not scan or rewrite existing custom links during upgrade because matching a path cannot prove the user's intent to remain bound to that system route.

### Regenerate contracts through official generators

The public model addition requires regenerated OpenAPI documentation and UI API-client types. Run the repository's generators and accept only their necessary output; never edit those generated files manually. Core and Console ship together, so no duplicate annotation or `href` compatibility marker is added for older full-PUT clients.

## Risks / Trade-offs

- [Route changes are eventually consistent] → Reuse the existing controller model and verify event-triggered reconciliation; do not couple bulk work to the settings request.
- [Old full-PUT clients can drop an unknown `routeRef`] → Treat this as the compatibility boundary of the `v1alpha1` model and document that writers need a matching model or Patch.
- [Downgrading Core loses route-reference behavior] → Convert bound items to custom links before downgrade if their links must remain editable and stable on the older version.
- [Invalid external data cannot be rejected conditionally at the generic write seam] → Validate the enum in the schema, prevent invalid combinations in Console, and fail closed in reconciliation.
- [A full menu scan runs after route-rule changes] → Accept the bounded cost for rare changes and add an index only if profiling demonstrates a need.

## Migration Plan

1. Add the public model and reconciliation behavior with focused backend tests.
2. Regenerate OpenAPI documentation and the UI API client using repository commands.
3. Add Console authoring, conversion, unbinding, preview, and localization behavior with focused tests.
4. Update new-installation initial data; do not mutate existing MenuItems.
5. Deploy Core and Console together. Verify existing custom and resource-linked menu items before testing the new bindings.

Rollback requires converting route-bound menu items to custom links while the new Console is available. No database rollback is needed because MenuItems remain Extension data and no storage schema changes.
