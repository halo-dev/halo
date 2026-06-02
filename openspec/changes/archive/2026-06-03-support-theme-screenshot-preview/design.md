## Context

Installed themes are stored under the theme root and reconciled into `Theme.status`. The Console currently uses
`Theme.spec.logo` as the visual preview in theme lists and preview selectors, which is a poor fit for themes that want to
show a page-like cover image instead of a square logo.

Theme assets are already exposed through `/themes/{themeName}/assets/**`, but that route resolves files from
`templates/assets/`. Issue #10048 asks for root-level `screenshot.*` files, so reusing the existing assets route would
either miss the requested location or require changing established theme asset semantics.

## Goals / Non-Goals

**Goals:**

- Detect `screenshot.png`, `screenshot.jpeg`, `screenshot.jpg`, or `screenshot.webp` from an installed theme root during
  theme reconciliation.
- Expose the detected screenshot as an optional `Theme.status.screenshot` URL.
- Serve only the detected root-level screenshot file through a narrow public static route.
- Update Console theme list/preview image selection to prefer the screenshot and fall back to the existing logo.

**Non-Goals:**

- Do not require or introduce a new theme manifest field.
- Do not change how `/themes/{themeName}/assets/**` resolves template assets.
- Do not add screenshot upload, editing, cropping, or validation beyond supported file-name detection.
- Do not change uninstalled theme discovery beyond keeping existing fallback rendering.

## Decisions

1. Add `screenshot` to `ThemeStatus`, not `ThemeSpec`.

   `ThemeSpec` is authored by the theme manifest. The requested screenshot is an observed runtime resource discovered
   from installed files, matching existing status fields such as `location` and `inDevelopment`. Keeping it in status
   avoids changing theme manifest compatibility and lets reconciliation clear stale values when files are removed.

   Alternative considered: add a manifest-level `spec.screenshot`. This would not satisfy automatic root-file discovery
   and would force theme authors to maintain redundant metadata.

2. Detect screenshots in `ThemeReconciler.reconcileStatus`.

   Reconciliation already owns observed theme state and has access to the theme root. Detection there keeps install,
   upgrade, reload, and local file changes represented through the same status update path.

   Alternative considered: populate the screenshot only during install/upgrade. That would miss local development
   changes and stale file removal until another lifecycle operation rewrites the status.

3. Serve screenshots through `/themes/{themeName}/screenshot.{extension}`.

   This route is intentionally separate from `/themes/{themeName}/assets/**` because screenshots live in the theme root,
   while assets resolve from `templates/assets/`. The resource resolver should only allow the four supported filenames
   and must keep the existing directory traversal protection.

   Alternative considered: expose root screenshots under the existing assets route. That would blur the meaning of
   `assets` and may surprise theme authors that already rely on `templates/assets/`.

4. Use deterministic file priority.

   If multiple supported files exist, reconciliation should choose the first readable file in this order:
   `screenshot.png`, `screenshot.jpeg`, `screenshot.jpg`, `screenshot.webp`. This makes behavior stable without requiring
   extra configuration.

5. Keep UI rendering as a fallback-only change.

   Console image components should derive one display source: `theme.status?.screenshot || theme.spec.logo`. Existing
   behavior remains intact for themes without screenshots and for uninstalled themes that have no reconciled status.

## Risks / Trade-offs

- Public route expands static theme surface area -> Limit the resolver to exact supported screenshot filenames and keep
  directory traversal checks.
- Cached screenshots may not refresh immediately in local development -> Include a cache-busting version query where
  practical and rely on the static resource handler's last-modified behavior.
- OpenAPI model change affects generated UI client -> Regenerate OpenAPI docs and the api-client instead of editing
  generated TypeScript by hand.

## Migration Plan

No database migration is required. Existing themes continue to work without screenshot files. After deployment, theme
reconciliation will populate or clear `status.screenshot` as themes are reconciled.

Rollback removes the optional status field usage and the screenshot route. Existing theme manifests and stored theme
objects remain compatible because the field is optional observed status.

## Open Questions

- None for the initial implementation.
