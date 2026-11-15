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
