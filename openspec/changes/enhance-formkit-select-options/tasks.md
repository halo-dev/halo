## 1. Select Option Types

- [x] 1.1 Add an internal select option type covering `label`, `value`, optional `icon`, optional `description`, and `attrs.disabled`.
- [x] 1.2 Replace repeated local `{ label, value }` option shapes in the custom select implementation with the internal type where practical.
- [x] 1.3 Extend select response and remote option provider types so `search` and `findOptionsByValues` can return metadata-bearing options.

## 2. Option Parsing and Value Handling

- [x] 2.1 Extend `requestOption` with optional `iconField` and `descriptionField` fields.
- [x] 2.2 Update the default `action + requestOption` parser to map configured metadata fields into option `icon` and `description`.
- [x] 2.3 Preserve metadata returned by `requestOption.parseData`, `remoteOption.search`, and `remoteOption.findOptionsByValues`.
- [x] 2.4 Keep FormKit node values unchanged for single select, multiple select, and clearing behavior.
- [x] 2.5 Update the select change callback path to pass full selected option objects instead of trimming them to `label` and `value`.

## 3. Dropdown Rendering and Search

- [x] 3.1 Render option `icon` as an image source in dropdown rows without using Iconify or component rendering.
- [x] 3.2 Hide or neutralize failed option icon images while keeping the option selectable.
- [x] 3.3 Render option `description` as secondary text below the label and keep label/description text truncated.
- [x] 3.4 Keep closed single-select display and multiple-select chips label-only.
- [x] 3.5 Update local option filtering to match both `label` and `description`, while excluding `icon` from search matching.
- [x] 3.6 Preserve provider-driven remote search behavior without applying local description filtering to remote results.

## 4. Tests and Documentation

- [x] 4.1 Add focused frontend tests for metadata parsing from `action + requestOption`.
- [x] 4.2 Add focused frontend tests for local search matching `description` and not matching `icon`.
- [x] 4.3 Add focused frontend tests or component coverage for dropdown metadata rendering and label-only selected display.
- [x] 4.4 Update custom FormKit input documentation with static `icon` / `description` examples.
- [x] 4.5 Update custom FormKit input documentation with `iconField` / `descriptionField` action mapping examples.

## 5. Validation

- [x] 5.1 Run the relevant frontend unit tests.
- [ ] 5.2 Run `pnpm -C ui typecheck`.
  - Attempted with `npx --yes pnpm@10.30.3 -C ui typecheck`; it currently fails on the pre-existing `ui/src/components/input/SearchInput.vue:52` callback type mismatch.
- [x] 5.3 Run `pnpm -C ui lint`.
- [x] 5.4 Run `openspec validate enhance-formkit-select-options --strict`.
