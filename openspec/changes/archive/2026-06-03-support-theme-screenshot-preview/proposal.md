## Why

Theme management currently relies on the theme logo as the visual cue in theme lists and previews. Themes should be able
to provide a dedicated preview cover so users can identify installed themes more quickly, especially when many themes are
available.

## What Changes

- Detect a root-level screenshot file in installed theme directories using the supported names `screenshot.png`,
  `screenshot.jpeg`, `screenshot.jpg`, and `screenshot.webp`.
- Expose the resolved screenshot URL on `Theme.status` so console and integration clients can consume it without parsing
  theme files.
- Serve the detected screenshot through a narrow public theme resource route.
- Update Console theme list and preview surfaces to prefer `status.screenshot` while falling back to the existing
  `spec.logo` behavior.
- No breaking changes: existing theme manifests, theme logos, and installed theme APIs continue to work.

## Capabilities

### New Capabilities

- `theme-screenshot-preview`: Defines how installed themes expose and display preview cover screenshots.

### Modified Capabilities

- None.

## Impact

- API model: adds an optional `Theme.status.screenshot` field.
- Backend: updates theme reconciliation and static resource handling for root-level theme screenshot files.
- Security: adds only the screenshot route to public static resource matching; no new management permission is introduced.
- UI: updates Console theme list/preview rendering to use the screenshot when present.
- OpenAPI/UI client: requires regenerating OpenAPI docs and the generated `ui/packages/api-client` models.
- Dependencies/database: no new dependencies and no database migration.
