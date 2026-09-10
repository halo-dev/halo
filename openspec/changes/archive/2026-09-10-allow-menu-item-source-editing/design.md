## Context

See `proposal.md` for motivation. The current modal stores an editable MenuItem alongside a selected source and resource name. Both source changes and submission mutate its source fields. The caller fetches the latest item before opening and mounts the modal with `v-if`, so drafts only need to live for one modal session.

Relevant evidence:

- `MenuItemEditingModal.vue`: resource sources are disabled during editing; non-resource items receive only four options. Source initialization matches `targetRef.kind` without checking its group or version.
- `MenuItemReconciler.java`: each reconciliation resolves the current route, resource, or custom source into status; there is no original-source comparison. Resource resolution matches group and kind, and event handling queries the current reference.
- `AnnotationsForm.vue`: annotation schemas receive `formData`, and changes to its separate `value` prop reset annotation inputs.
- `SelectMain.vue`: Halo's custom select updates its node, waits for settlement, then calls the supplied change handler. Existing modal tests stub FormKit and call setup methods directly, so they do not verify this interaction.
- Existing `menu-route-bindings` design deliberately retained resource locks. This change supersedes that Console restriction without changing backend route resolution.

## Goals / Non-Goals

**Goals:**

- Keep a small local draft model and one source-normalization path shared by submission and annotation context.
- Preserve typed text across temporary switches while preventing a resource name from being reused under another resource type.
- Keep conversion on the existing MenuItem so children and external references to its name remain attached.

**Non-Goals:**

- A new source-provider extension point, shared state machine, new dependency, or broad component decomposition.
- Per-resource-type selection history, automatic fetching of previews for unsaved source selections, or changes to resource selector visibility rules.
- Redesigning the existing two-request content/parent update flow, optimistic concurrency recovery, or backend reconciliation timing.

## Decisions

### 1. Open supported sources and preserve opaque references

For supported items, show the same eight options during creation and editing. Match an existing resource against the descriptor's group, version, and kind, excluding its instance name. The built-in descriptors are `content.halo.run/v1alpha1` with Post, SinglePage, Category, or Tag.

If no descriptor matches, retain a locked representation of the existing reference, show its identity as the selected option, and preserve all original source fields on unrelated saves. This includes foreign-group `Post` references and unsupported versions. Do not send these through a built-in resource picker or normalize them as custom links.

Alternative: match only kind or expose conversions for arbitrary references. Rejected because the modal cannot interpret their semantics and kind collisions can silently rewrite plugin data. Matching the supported version is deliberately more conservative than backend group/kind resolution.

### 2. Retain text drafts; reset resource selection on actual type changes

Keep one editable display-name draft shared by custom and route modes, one custom-URL draft, the selected source, and one selected resource name. Hidden text drafts remain local and do not become active payload fields.

For an existing custom or route item, initialize the editable name from `spec.displayName`. For an existing supported resource, seed it from the fetched item's `status.displayName` when available. Initialize a custom URL from `spec.href` for custom items, or from the fetched item's `status.href` for supported bindings. These are modal-open snapshots, not live previews of later unsaved source selections.

When entering a route without an initialized name draft, use its existing localized default name once. Preserve subsequent user edits, including deliberate clearing; do not repeatedly replace text with defaults. A custom URL deliberately cleared by the user must also remain cleared after a round trip.

Switching to a different source clears the resource selection, including returning to a previously visited resource type. Reopening an existing resource item without switching retains its original resource name. There is no cache per resource type.

If a referenced item has no resolved name or URL, leave the missing field for the author to supply and use existing required-field validation. Do not invent URLs or fetch route settings to resolve an unsaved selection.

Alternative: mutate persisted source fields on each switch and reconstruct drafts from props. Rejected because it loses new input and reads stale original values. A per-source draft store is unnecessary for eight choices sharing two text fields.

### 3. Normalize a separate MenuItem view

Derive a MenuItem view from the draft and current selection, then clone it for submission. Keep normalization local to this feature; a small pure helper is acceptable if it makes direct tests simpler.

| Selected source | Active fields | Fields absent from the submitted spec |
| --- | --- | --- |
| Custom | displayName, href | targetRef, routeRef |
| Built-in route | displayName, routeRef | targetRef, href |
| Supported resource | targetRef with the selected name | routeRef, displayName, href |
| Unsupported existing reference | Original source fields | No automatic cleanup |

Retain metadata, target, and hierarchy fields except for explicit existing form edits. Continue merging validated annotations. Do not synthesize status for the new source: it remains server-derived and can temporarily describe the old source until reconciliation.

Pass the normalized view as annotation `formData` so source-dependent annotation schemas see the selected source before submission. Keep annotation `value` and the component key stable while switching so draft annotations are not reset. Validation still blocks submission while a required resource is unselected or a visible text field is invalid.

Alternative: normalize only at submit time. Rejected because annotation schemas would keep seeing the old source or inactive text fields. Mutating the form in place is also rejected because a failed request would destroy hidden drafts.

### 4. Preserve existing persistence and verify the real form

Use the existing update API, save events, tree refresh, and parent-position API. A source-only edit must not request a parent move. A simultaneous parent edit must still use the dedicated position endpoint. Existing partial-save handling remains in scope for regression checks, but this change does not make those requests atomic.

Use focused modal tests for conversion payloads, failures, annotations, and opaque references. Add a representative integration test using the real FormKit runtime and Halo select, with API responses mocked, to exercise source-change ordering, resource-picker remounting, and validation. Do not replace it with direct calls to setup methods. Finish with a local Console check of an existing item with children, saving and reopening after reconciliation.

## Risks / Trade-offs

- [A modal-open status snapshot can be missing or stale] → Only use it as an editable seed; preserve typed values and require missing values. Switching among unsaved bindings does not promise a freshly resolved preview.
- [Conditional inputs can retain obsolete values or validation state] → Normalize explicitly, reset resource names on actual source changes, retain keyed resource inputs, and exercise the real FormKit lifecycle.
- [Annotation schema context changes can reset user input] → Change only `formData` during source switching; regression-test annotation draft preservation and source-specific context.
- [Unsupported reference versions remain locked] → Preserve compatibility without guessing; wider reference support belongs to a separate change with an explicit selector contract.
- [Content save succeeds while parent movement fails] → Preserve existing partial-save reporting and refresh behavior; do not claim rollback or atomic conversion plus movement.

## Migration Plan

Ship as a Console behavior change with existing APIs and resource schema. No data migration, generated-file change, or plugin/theme upgrade coordination is required. Only explicit saves convert sources. Rolling back the Console restores the old editing restriction; converted items remain ordinary supported MenuItems and continue to render through the existing reconciler.
