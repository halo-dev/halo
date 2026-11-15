## Context

Both Halo and plugins register resource types and index definitions. Existing index mutations precede database commit; preserve that contract, but never snapshot known rollback-induced drift. A single process owns extension writes.

## Goals / Non-Goals

**Goals:** Avoid full scans for compatible core/plugin restarts and upgrades; contain fallback to affected types; permit stopped-plugin snapshots to survive unrelated activity and process restarts.

**Non-Goals:** Automatic Java function equivalence, partial-index rebuilds, crash delta recovery, multi-process coordination or changing index transaction semantics.

## Decisions

- Key database checkpoints by a SHA-256 of the actual storage prefix. Bind each checkpoint to a new random file token, resource type/GVK and a manually maintained shared index engine compatibility version. Full index fingerprints include names, kinds, key types, uniqueness, nullability and explicit semantic versions. Neither application build identity nor plugin JAR identity participates.
- Halo and plugin authors must increment an index's existing `version` when its extraction semantics change. Shared index semantics/encoding changes require the engine/codec compatibility version to change. Structural equality alone cannot prove arbitrary Java functions equivalent.
- Hold per-prefix nonblocking writer admission during initialize/restore/rebuild and during capture plus index removal. Wait for admitted transactions through afterCompletion; timeout aborts the lifecycle rather than removing indices under an active writer. Reactive writers fail admission instead of blocking event-loop threads.
- A registration consumes its database checkpoint before restoring or rebuilding; a crash after consumption cannot reuse it. Successful rebuilding repairs local eligibility. Each clean unregistration can publish a new checkpoint, allowing repeated plugin restarts in one process.
- Writes to inactive types delete possible ancestor-prefix checkpoint rows before changing data in the same transaction. This also covers disabled plugins after process restart and names containing slashes. Do not cache this deletion until confirmed commit. Initialized active types avoid checkpoint SQL on ordinary writes. Raw store writes without the internal indexed-write context disable publication for affected registered types, even inside a transaction.
- Save the atomic file first, reserve an unusable database checkpoint row, then publish the token through a version-checked update of that row. Mark the row as potentially present before reservation, including failed or timed-out acknowledgements. Later writes delete it before mutating data; a delayed publication cannot insert it again. A late reservation can only leave an unusable marker. If that marker arrives after reinitialization, a later capture reuses its current row version on a duplicate reservation instead of permanently losing capture eligibility. Publication remains a version-checked update. Failed saves permit unregistration and disable further capture until rebuilding. Root shutdown first stops plugins while core controllers and GC can finish deleting bundled resources (capturing plugin types when unregistered), then stops core controllers and captures remaining core types before bean destruction. Late writes invalidate a published checkpoint instead of globally rejecting all shutdown writes.
- The scheme manager resolves the snapshot manager lazily. A direct constructor dependency would make Spring stop the scheme initializer before the snapshot lifecycle regardless of phase. Root capture followed by scheme destruction reuses the published generation when no write intervenes; it must not replace the file and attempt a duplicate checkpoint insert.
- Plugin and shared contexts do not register independent JVM shutdown hooks. The root lifecycle stops plugin controllers/delegates before closing their contexts; the plugin manager closes the shared context after unloading plugins. Independent hooks would otherwise race the root lifecycle during SIGTERM.
- Root shutdown retains hot-stop cleanup of plugin-bundled resources, including the do-not-overwrite exception. Plugin shutdown runs before core controllers stop; snapshot capture runs afterward. A failed plugin start or an offline package replacement must not leave a prior bundled login provider active.
- Backup restore first disables publication globally and waits for concurrent snapshot lifecycles, then replaces the database. All checkpoint namespaces, including the legacy marker, are excluded from backups. Direct SQL/migrations that bypass the store must explicitly invalidate cache eligibility before mutation.

## Risks / Trade-offs

- Compatibility relies on the existing semantic-version contract. Forgotten version bumps cannot be detected automatically.
- Snapshots trade shutdown I/O and approximately index-sized temporary allocations for startup speed.
- Known rollback/index uncertainty remains isolated to the type until a full rebuild. Unknown/direct database changes outside the supported write paths are not covered.
- Upgrading from the earlier global checkpoint requires one full rebuild. No user data migration is needed.

## Review corrections

- Snapshot format 3 uses length-prefixed UTF-8 (up to 64 MiB per string), rejects malformed text and distinguishes wrapped null keys from literal strings. Previous formats rebuild safely.
- Shared engine compatibility version 2 defines the minimum key as the sorting representative of a multi-value index for both ascending and descending sorts, independent of set iteration order or snapshot restoration.
- Custom index builders retain source compatibility through a default `version(int)` method that explicitly rejects unsupported versioning. Standard builders override it and retain semantic version support.

- Failed or interrupted initialization removes partial indices before releasing waiting queries. Scheme registration rolls back its registry entry and notifies removal listeners, allowing a subsequent plugin start to rebuild instead of skipping an incomplete registration. Cleanup preserves the original failure.

- Readiness is a per-build completion result. Removing a failed build completes that result exceptionally, so its already-waiting queries fail even if the same class starts a fresh build before those queries resume. Success, timeout and interruption retain their existing behavior.

- Cache recovery is automatic: missing, corrupt or mismatched files rebuild the affected type, and a later successful lifecycle replaces its snapshot. Filesystem failures do not require deleting snapshots; once storage access/capacity recovers, rebuilding repairs eligibility and normal capture resumes.
- Snapshot count fields never determine large initial allocations; collections grow only as payload entries are decoded. Captures use a newly created temporary file, clean abandoned per-type temporary regular files/symlinks without following them, and preserve directories. Temporary cleanup is best effort and cannot require directory-list permission for otherwise writable storage.
- Interruption while draining a type still invalidates its eligibility. If another type's startup failure interrupts plugin cleanup, already-initialized types can require one conservative rebuild at their next registration. This is distinct from cache-file corruption and does not require manual snapshot cleanup.
