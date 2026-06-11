## Why

Halo's custom FormKit `select` input currently renders options as label-only rows, which makes richer plugin, theme, and configuration choices harder to distinguish when several values have similar names. It should support visual and explanatory option metadata while preserving the existing submitted value contract.

## What Changes

- Add optional `icon` and `description` metadata to Halo custom `select` options.
- Render `icon` as an image source and `description` as secondary text below the option label in the dropdown list.
- Keep selected display compact by showing the selected label only.
- Extend `action + requestOption` parsing with optional `iconField` and `descriptionField` mappings.
- Allow `remoteOption.search` and `remoteOption.findOptionsByValues` to return options with `icon` and `description`.
- Search local options by both `label` and `description`.
- Keep the FormKit node value unchanged: single select submits a string and multiple select submits a string array.
- Update custom FormKit input documentation and focused frontend tests.

## Capabilities

### New Capabilities

- `formkit-select-options`: Defines Halo custom `select` option metadata, rendering, remote parsing, search, and value behavior.

### Modified Capabilities

- None.

## Impact

- Affects frontend files under `ui/src/formkit/inputs/select/` and the select label renderer used by FormKit array displays.
- Affects custom FormKit input documentation under `ui/docs/custom-formkit-input/README.md`.
- No backend API, database schema, OpenAPI, generated API client, i18n keys, or npm dependency changes are expected.
- Existing static and remote select options remain compatible because `icon`, `description`, `iconField`, and `descriptionField` are optional.
- Plugin and theme schemas can opt in by adding `icon` and `description` to option objects or by configuring the new request option fields.
