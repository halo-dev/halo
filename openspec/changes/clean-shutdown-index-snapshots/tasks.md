## 1. Scope and write contract

- [x] 1.1 Remove afterCommit mutations, version guards and delta-query APIs; verify existing extension and GC transaction tests plus a real-database uniqueness regression.
- [x] 1.2 Replace the earlier design/plan with the conservative lifecycle design; validate OpenSpec artifacts.

## 2. Clean checkpoint and restoration

- [x] 2.1 Implement transaction draining, database checkpoint consumption/publication and backup invalidation; verify rollback, cancellation, timeout, and restart cases.
- [x] 2.2 Store token-bound snapshots under indices/snapshots and rebuild whole types on mismatches; verify corruption, version/spec changes and runtime re-registration.

## 3. Validation

- [x] 3.1 Run relevant backend and API tests and formatting checks; verify the reviewed regression cases no longer occur.
- [x] 3.2 Start isolated Halo processes and compare cold/warm startup, HTTP behavior and abnormal-exit fallback; record evidence and limits.

## 4. Local startup follow-up

- [x] 4.1 Preserve snapshots after failed store writes that emit no result; retain rollback/cancellation/unknown-completion protections and record invalidation reasons.
- [x] 4.2 Generate a stable backend content fingerprint for checkpoint identity; verify repeated builds preserve it and changed content invalidates reuse.
- [x] 4.3 Verify real optimistic-lock conflicts and repeated bootRun launches against an isolated PostgreSQL database; run regression tests and record evidence.

## 5. Unified Halo and plugin lifecycle (supersedes global build identity and one-use-per-process rules)

- [x] 5.1 Replace global checkpoints/build identity with per-type compatibility and repeated capture/restore.
- [x] 5.2 Coordinate registration/unregistration, transaction draining, dormant writes and restore invalidation; preserve independent types.
- [x] 5.3 Verify structural/semantic changes, rollback, cancellation, timeout, crash, corruption and direct-store paths with real database tests.
- [x] 5.4 Verify actual plugin stop/start and compatible/incompatible upgrades, root restart and HTTP results in isolated processes.
- [x] 5.5 Repeat huge-halo startup measurement, verify shutdown behavior, run backend/API regression checks and record evidence.
