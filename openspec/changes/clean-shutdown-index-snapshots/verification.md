## Filesystem and malformed-cache recovery

- Full regression: API 269 tests, application 2659 tests, zero failures/errors and 3 skipped; Java formatting, bootJar and whitespace checks passed. Evidence: `/tmp/halo-snapshot-extremes/test-summary.json`, `full-regression.log` and copied XML reports.
- A 32 MiB heap child JVM reproduced OutOfMemoryError from a truncated snapshot declaring an allowed large count. All five count fields now grow their collections with decoded payload instead of preallocating from untrusted headers; the same constrained-heap test rejects all five normally. Red evidence is retained in `red-test-results/`.
- Fixed temporary paths were independently shown to fail permanently for read-only files/directories and overwrite a symlink target. Capture now uses a fresh exclusive temporary file, cleans abandoned per-type temporary regular files/links without following links, preserves directories, and treats cleanup as best effort. Regression tests verify target contents, checkpoints and subsequent restoration.
- Real PostgreSQL 15.4 and two plugin versions with 100,000 records passed 27 HTTP validation stages, including missing/empty/truncated files, CRC failures, foreign tokens, truncated huge counts, temporary-only files, read-only temporary files, symlinks, directory obstructions at the old temporary path, absent directory-list permission, read-only cache storage and actual ENOSPC on a dedicated 32 MiB APFS volume. Each file fault rebuilt only the affected type and resumed fast restoration on the following lifecycle; all count/filter/long-key/legacy-null/sort assertions passed.
- The ENOSPC test filled only the isolated image, not the host filesystem. Plugin stop/start and database queries continued; after freeing capacity, normal plugin lifecycles resumed snapshot capture without deleting snapshots. Permission recovery was similarly tested by restoring filesystem access, not deleting cache files.
- An injected interruption while registering the second type allowed a normal plugin retry to recover queries. Interrupted cleanup invalidated the first type; the next Halo restart rebuilt that type once, and a further restart restored both types. This is conservative recovery, not an automatic retry of plugin application errors. The test harness explicitly performs bounded normal plugin retries.
- Final runtime evidence: `/tmp/halo-snapshot-extremes/e2e-accepted/results.json`, runner/source/server/path logs; JAR SHA-256 `e7f8b57949d16a063d9e159859fcb41f5d24751e4f2bebb971e255759210be24`. Earlier runs remain under `e2e/` (an overly strict immediate-restore assertion) and `e2e-final/` (an unrelated startup-task interruption before injection); neither is reported as a complete pass.
- Storage permissions/capacity must be restored externally. Tests do not establish immunity to arbitrary valid decompression bombs, exhausted heap from real index volume, hung filesystem calls, multi-process writers or real database network partitions. Existing semantic-version requirements remain unchanged.

## Readiness retry race closure

- A deterministic probe reproduced an old failed-build waiter reading the next build's partial indices. Readiness now stores an independent completion result per build; removing a failed build completes its result exceptionally. A retry cannot turn that result into success. The same probe now reports an initialization-aborted exception. Evidence: `/tmp/halo-snapshot-review-fixes/readiness-race/red.log`, `green.log`, and `ReadinessRaceProbe.java`.
- Final regression after this change: API 269 tests, application 2649 tests, zero failures/errors and 3 skipped; formatting checks passed. Evidence: `/tmp/halo-snapshot-review-fixes/accepted-test-summary.json` and `regression-readiness.log`.
- The full nine-stage, 100,000-record real-plugin probe including intentionally interrupted registration and recovery passed again. Evidence: `/tmp/halo-snapshot-review-fixes/e2e-readiness/results.json`. Runtime JAR SHA-256: `38774c5aa8a8113a09fdc274a736274bd7da9a6995a0021e6b212c142c70e58c`.

## Final follow-up after independent rereview

- A second failure test reproduced a late reservation arriving after reinitialization and preventing the next capture. On a duplicate reservation, publication now reads the existing row version and still uses an optimistic update. The test fails before the change and passes afterward (`/tmp/halo-snapshot-review-fixes/late-reservation-red.log`).
- An intermediate real plugin upgrade was cancelled during index construction. Failed registration remained present and the retry skipped rebuilding (`e2e-round2/failure.txt`). Registration now rolls back its scheme and removal listeners; initialization discards partial indices before releasing blocked queries. Dedicated tests cover interruption/retry, partial-index rejection, blocked queries and cleanup-listener errors. This resolves the previously unconfirmed registration-failure concern.
- Final full regression: API 269 tests, application 2648 tests; zero failures/errors, 3 skipped. Java formatting and OpenSpec checks passed. Evidence: `/tmp/halo-snapshot-review-fixes/final-test-summary.json` and `regression-accepted.log`.
- Final real-plugin validation repeats all nine stages with 100,000 records and actively injects an interrupted index build in version 3. It observes FAILED, retries, rebuilds correctly, verifies HTTP/index results, and completes subsequent upgrades/restarts/crash recovery. Evidence: `/tmp/halo-snapshot-review-fixes/e2e-final/results.json` and adjacent fixture sources/logs. Final runtime JAR SHA-256: `a106ca3b239f13a64feb69df532c1b069fca96c06331bffc0ca23c794d754c3b`.
- Intermediate failed runs remain preserved; they are not reported as passes. Earlier result counts/artifact hashes below identify earlier snapshots. The old-production probe `late-red-probe/run.log` also supplies the previously missing red evidence for the late-reservation-after-write case.

## Current review corrections (supersedes historical implementation details below)

- R1 uses an unusable reservation followed by a version-checked checkpoint update. Real H2 tests cover committed publication with a lost acknowledgement, publication completing after invalidating writes, and reservation completing after writes. These are deterministic fault injections, not actual network-disconnection tests.
- R2/R4 use snapshot format 3: wrapped null values remain distinct, 64 KiB-plus ASCII and multibyte keys round-trip, malformed UTF-8/lengths and older formats are rejected. The per-string limit is 64 MiB.
- R3 uses the minimum multi-value key for sorting before and after restoration. Shared engine compatibility version 2 forces one safe rebuild of older snapshots. R5 preserves custom-builder compatibility with an explicit unsupported-versioning default; an old compiled implementation was also exercised against the new API.
- Full API/application tests and Java formatting checks passed: API 269 tests, application 2643 tests, zero failures/errors and 3 skipped. The runnable JAR was built from the same production code. Evidence: `/tmp/halo-snapshot-review-fixes/test-summary.json`, `regression-final.log`, `red-test-results/`, and `binary-compat.log`.
- Five isolated Halo processes, PostgreSQL 15.4, four actual plugin versions and 100,000 plugin records passed nine HTTP validation stages. Hot restart, compatible upgrade, semantic/structural changes, root restart, crash recovery and disabled-plugin restart all selected the expected restore/rebuild paths. Every stage checked long-text indexing, legacy literal-null matching and ascending/descending multi-value sorting. Evidence: `/tmp/halo-snapshot-review-fixes/e2e/results.json` and adjacent scripts/logs. JAR SHA-256: `0f1dbdf1c53344736a17bf7666bca4f42502d2497f08bf1d57bc6c1de0b9ca27`.
- Upgrade-transition polling encountered two transient HTTP 500s before readiness; all stable-stage assertions passed. A separate run against the pre-fix JAR reproduced three upgrade-transition 500s (and 404s), so seamless availability during plugin replacement remains outside these fixes. Evidence: `/tmp/halo-snapshot-review-fixes/baseline-upgrade/results.json`. The macOS native DNS fallback log also remains. All owned services/containers were stopped; original user data was untouched.
- Earlier build-identity and global-checkpoint descriptions below document historical runs; the current design is the per-type lifecycle described in `design.md`. This round does not claim to rerun the historical huge-halo performance benchmark or real network-fault recovery.

## Historical verification

## Automated checks

- Java 21: `:api:test :application:test` passed. API: 268 tests, 0 failures; application: 2,615 tests, 0 failures, 3 skipped. Evidence: `/tmp/halo-pr10492-full-tests-final.log` and `/tmp/halo-pr10492-e2e/full-test-summary.json`.
- After the final publication guard and additional cases, 267 targeted tests passed, including application startup, real H2 uniqueness/non-null validation rollback, transaction completion/cancellation/drain timeout, checkpoint consumption, snapshot corruption, spec/build mismatch, whole-type rebuilding, and backup invalidation. Evidence: `/tmp/halo-pr10492-focused-final.log`.
- `spotlessCheck`, compilation and `openspec validate clean-shutdown-index-snapshots --strict` passed. The lifecycle flags were made volatile before the final runtime build and process tests.
- The first full run exposed an early Spring listener initialization cycle. Restricting the listener to ready/closed events fixed it; the subsequent full run passed.

## Runtime evidence

The final probe passed with five independently launched Halo processes and an isolated work directory, using real HTTP setup, basic authentication and H2 persistence. All owned processes exited. Evidence: `/tmp/halo-pr10492-e2e/attempt2/results.json`, `e2e.py`, `server-*.log`, and per-phase `manifest-*.json` in that directory.

| Check | Observed result |
| --- | --- |
| Seed and normal restart | 1,000 ConfigMaps with 16 KiB payloads; all data hashes, homepage and console user API checks passed |
| Cache layout | 38 snapshot files in `work/indices/snapshots/`; none in `work/indices/` |
| Warm startup | 38 types restored, including ConfigMap; no ConfigMap full database scan |
| Crash recovery | SIGKILL after updates, creations and physical deletions; next startup restored 0 types and all expected names/data matched |
| Backup export/import | Actual backup resource and restoration endpoint; checkpoint excluded from exported extension rows; restoration returned HTTP 200 |
| Backup recovery | Changed an indexed label, added and deleted resources after export; after import and restart, restored 0 snapshot types and matched the exported data and index query results |
| Shutdown after restore | No clean checkpoint published; the next rebuilt process could publish one again |

Local cold/warm samples with the same seeded data: HTTP/startup polling reached readiness in 5.605 s / 5.232 s (the probe includes a fixed 1 s settling delay). Spring's own startup log reported 4.193 s / 3.843 s. The cold ConfigMap full build indexed 1,008 total ConfigMaps in 138.172 ms; the warm process restored that type. These are diagnostic samples only, not a promised speedup.

## Limits

- H2 and a single Halo process were exercised. PostgreSQL/MySQL and external database writers were not exercised; multi-process writers are outside this design.
- Initial implementation: every rollback disabled publication. The local startup follow-up below narrows this to rollbacks after a store emitted a result; safe optimistic-lock failures no longer disable publication.
- Startup timing is a local sample, not a statistically controlled performance benchmark.
- Bundled plugins fail with `NoSuchMethodError` for `BasePlugin(PluginWrapper)`. Source inspection at baseline `cd17256a` confirms that constructor was already absent before this PR; a separate baseline runtime was not launched. Core HTTP and snapshot checks do not establish plugin feature compatibility.
- The initial process probe's pre-crash comparison stopped because it treated `size=0` as unlimited, while the current API caps a page at 1,000. The corrected probe paginates and waits for all deletions; initial evidence is retained separately.

## Local bootRun and optimistic-lock follow-up

- Reproduced the previous bug with a real-database test: an optimistic-lock failure before any store result disabled snapshots (`/tmp/halo-snapshot-conflict-red.log`). The revised guard passes that test and continues to reject checkpoints after a prior successful write in the same rolled-back transaction (`/tmp/halo-snapshot-conflict-green.log`).
- The real extension-client test now verifies stale updates fail, the latest indexed value remains queryable, the stale value is absent, and snapshot eligibility remains intact before deliberately exercising unsafe post-write failures.
- Snapshot eligibility records the first invalidation reason, which is included in the shutdown log. Unknown transaction completion and drain timeout still disable reuse.
- Build identity now comes from backend source/resource/version/build-configuration content. A fixture executes the actual Gradle generator: unchanged inputs keep the same ID; changes to API source, migration SQL and version each change it. Evidence: `/tmp/halo-snapshot-bootrun-q6rpv0s7/fingerprint-results.json`.
- Three actual Gradle `bootRun` launches with the `local` profile, isolated PostgreSQL 15.2 and an isolated work directory passed. Build timestamps differed on all three runs while content identity stayed equal; the second and third starts restored 38 types. Two HTTP stale-version updates returned 409, preserved the expected database and label-index values, and both following shutdowns saved snapshots. All 100 test resources matched independent expected hashes. Evidence: `/tmp/halo-snapshot-bootrun-q6rpv0s7/attempt2/results.json`, `build-identities.json`, and `bootrun-*.log` in the same directory.
- The application JVM was stopped with SIGTERM; Gradle reports child exit 143 as a failed bootRun task after the application's successful shutdown. This is expected for the test's shutdown mechanism. The temporary PostgreSQL containers were stopped/removed; the user's development database and work directory were not used.
- An initial probe used comma-separated label selectors, while this endpoint expects repeated `labelSelector` parameters. The corrected probe passed; the initial output is retained in the parent evidence directory.
- Full API/application regression and formatting checks passed (`/tmp/halo-snapshot-followup-full-tests.log`): API 268 tests; application 2620 tests, 3 skipped; zero failures/errors. The unknown-completion regression also passed. OpenSpec validation and git diff whitespace checks passed.
