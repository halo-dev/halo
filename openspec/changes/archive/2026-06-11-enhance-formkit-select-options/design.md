## Context

Halo registers a custom FormKit `select` input under `ui/src/formkit/inputs/select/`. The current implementation treats options as `{ label, value }` objects for the interactive dropdown, while keeping the FormKit node value as a string for single select and a string array for multiple select.

Option data can currently enter the component through static `options`, a custom `remoteOption` object, or an `action + requestOption` parser. The dropdown row only renders `option.label`, and the `action` parser reconstructs option objects from `labelField` and `valueField`, so richer metadata is not consistently supported.

## Goals / Non-Goals

**Goals:**

- Let Halo custom `select` options include optional image icons and secondary descriptions.
- Keep existing select value behavior unchanged for form submission and validation.
- Support the new metadata across static options, `remoteOption` results, `findOptionsByValues` results, and `action + requestOption` parsing.
- Keep selected single-select text and multiple-select chips compact by showing labels only.
- Improve local search so option descriptions are searchable.
- Keep the implementation internal to `ui/src/formkit/inputs/select/` and document the schema contract.

**Non-Goals:**

- Do not support Iconify, Vue component icons, inline SVG component rendering, or data model variants beyond image `src` strings.
- Do not add URL allow-listing or resource validation inside the select component.
- Do not change backend APIs, OpenAPI contracts, generated API clients, or database schema.
- Do not change the submitted value shape from strings to option objects.
- Do not redesign all FormKit option formats or make Halo's custom select fully match the native FormKit select option normalizer.

## Decisions

### Option metadata shape

Use optional `icon` and `description` fields on the existing option object:

```ts
type SelectOption = {
  label: string;
  value: string;
  icon?: string;
  description?: string;
  attrs?: {
    disabled?: boolean;
  };
};
```

`description` is the only secondary-text field. The custom select will not treat FormKit's documented `help` field as an alias because that would blur Halo's custom input contract and make the schema less explicit.

### Image icon rendering

Render `icon` with an `<img>` element and pass the value as `src`. The component will not parse icons as Iconify names, components, HTML, or SVG markup. Broken images should not block option rendering or selection; hiding the failed image is sufficient.

The select component should treat icon URLs like ordinary image sources. URL policy belongs at plugin/theme resource boundaries or upstream schema providers, not in this low-level option renderer.

### Selected value and selected display

Keep the FormKit node value unchanged:

- Single select stores the selected option's `value` string.
- Multiple select stores an array of selected option `value` strings.
- Clearing a single select keeps the current empty-string behavior.

Selected display remains label-only. Rendering descriptions or icons in the closed selector and multiple-select chips would make the input height less predictable and would affect existing compact forms.

`onChange` should receive the full selected option objects, including `icon` and `description`, because it is the only callback that exposes selected option context beyond the stored value.

### Remote and action option parsing

Static options and `remoteOption.search` / `remoteOption.findOptionsByValues` results can already carry arbitrary object fields, so they should preserve `icon` and `description` when present.

For `action + requestOption`, add optional `iconField` and `descriptionField` mappings to the existing parser. These fields are only used by the default parser. If `parseData` is provided, the custom parser can return complete option objects directly.

Default-value lookup through `findOptionsByValues` or the `fieldSelector` action request should preserve the same metadata so selected options are represented consistently after initial load.

### Local search

Local search should match both `label` and `description`. It should not match `icon`, because icon is a resource address rather than user-facing searchable text.

Remote search remains backend-driven: the component continues to send `keyword`, `page`, and `size` without client-side filtering of remote results.

### Internal types

Create a small internal type module near the select implementation so `SelectMain.vue`, `SelectContainer.vue`, `SelectOption.vue`, `SelectDropdownContainer.vue`, and array renderers do not duplicate `{ label, value }` shapes. This should not become a public workspace package export.

## Risks / Trade-offs

- Larger option rows can make long dropdowns feel denser → Mitigation: only show secondary text when `description` exists, keep label and description single-line truncated, and keep the selected control label-only.
- External image URLs can leak image requests → Mitigation: render only as an image source, avoid interpreting icon as HTML, and use browser image behavior; broader URL policy remains an upstream concern.
- `action` parsing may silently omit metadata when field names are wrong → Mitigation: follow the existing field parsing pattern and only add metadata when the configured field exists.
- Reusing full option objects in `onChange` changes callback payload richness → Mitigation: the existing `label/value` fields remain present, and FormKit node values remain unchanged.
- Adding internal types can look like a public API → Mitigation: keep types under the select input directory and avoid package-level exports.

## Migration Plan

No data migration is required. Existing static and remote select options continue to work because the new metadata fields are optional.

Rollback is straightforward: remove the metadata rendering and parser fields. Existing forms using only `label` and `value` remain unaffected.

## Open Questions

None.
