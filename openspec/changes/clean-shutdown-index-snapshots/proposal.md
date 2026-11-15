## Why

Accelerate both Halo and plugin startup without changing the existing index validation transaction placement. Unrelated application or plugin upgrades should not force rebuilding large resource types.

## What Changes

- Share per-storage-prefix checkpoint consumption, write admission and snapshot publication between application shutdown and scheme unregistration/re-registration.
- Validate complete index definitions and existing explicit semantic versions, plus a shared engine compatibility version. Remove the whole-backend build fingerprint.
- Invalidate dormant snapshots before store writes, including unregistered types and slash-containing resource names. Track transactions through completion and contain uncertainty to affected types.
- Save after controllers stop on root shutdown; save before removing indices on plugin unregistration. Skip bundled plugin resource cleanup during root shutdown.
- Keep snapshots under `indices/snapshots/`, with atomic file replacement and database publication last. Rebuild whole affected types on any mismatch.

## Capabilities

### New Capabilities

- `extension-index-snapshots`: Type-scoped startup acceleration across compatible Halo and plugin releases.

### Modified Capabilities

None.

## Impact

Backend index lifecycle, store writes, plugin shutdown, backup restore and regression tests. No database schema migration, dependency or new plugin-facing interface. Reserved checkpoint rows are excluded from backups. Assumes a single Halo process owns database writes; semantic index function changes must increment the existing index-spec version.
