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
- Save the atomic file first and database checkpoint last. Failed saves permit unregistration without cache reuse. Root shutdown uses an explicit lifecycle phase after core controllers stop; it stops plugins (capturing their unregistered types), then captures remaining core types before bean destruction. Late writes invalidate a published checkpoint instead of globally rejecting all shutdown writes.
- The scheme manager resolves the snapshot manager lazily. A direct constructor dependency would make Spring stop the scheme initializer before the snapshot lifecycle regardless of phase. Root capture followed by scheme destruction reuses the published generation when no write intervenes; it must not replace the file and attempt a duplicate checkpoint insert.
- Plugin and shared contexts do not register independent JVM shutdown hooks. The root lifecycle stops plugin controllers/delegates before closing their contexts; the plugin manager closes the shared context after unloading plugins. Independent hooks would otherwise race the root lifecycle during SIGTERM.
- Root shutdown does not run hot-stop cleanup of plugin-bundled resources: core controllers have already stopped and cannot complete that cleanup. Normal plugin stop keeps existing cleanup behavior.
- Backup restore first disables publication globally and waits for concurrent snapshot lifecycles, then replaces the database. All checkpoint namespaces, including the legacy marker, are excluded from backups. Direct SQL/migrations that bypass the store must explicitly invalidate cache eligibility before mutation.

## Risks / Trade-offs

- Compatibility relies on the existing semantic-version contract. Forgotten version bumps cannot be detected automatically.
- Snapshots trade shutdown I/O and approximately index-sized temporary allocations for startup speed.
- Known rollback/index uncertainty remains isolated to the type until a full rebuild. Unknown/direct database changes outside the supported write paths are not covered.
- Upgrading from the earlier global checkpoint requires one full rebuild. No user data migration is needed.
