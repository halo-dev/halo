## Purpose

Accelerate Halo startup using reusable index snapshots while preserving the database as the authoritative source and falling back to full rebuilding whenever reuse cannot be established safely.

## ADDED Requirements

### Requirement: Reuse only clean shutdown snapshots

The system SHALL reuse snapshots only when they correspond to a consumed clean shutdown checkpoint in the current database and all index specifications for the type match.

#### Scenario: Normal restart
- **WHEN** Halo restarts after successfully saving a clean shutdown checkpoint
- **THEN** matching index snapshots SHALL load without scanning extension rows and return the same query results as a full rebuild

#### Scenario: Crash or uncertain snapshot
- **WHEN** no reusable checkpoint exists, a snapshot is missing or corrupted, or an index specification differs
- **THEN** the affected type SHALL be rebuilt completely from the database

### Requirement: Capture after writes are drained

The system SHALL reject new writes to the resource type being captured and wait for its admitted transactions to finish before capturing its snapshot. It SHALL NOT publish a clean checkpoint after a drain timeout, rollback-induced uncertainty, or direct untracked database restoration.

#### Scenario: In-flight write
- **WHEN** shutdown begins while a transaction is still active
- **THEN** snapshot capture SHALL wait until the transaction completes

#### Scenario: Backup restoration
- **WHEN** a backup is restored and row versions reset
- **THEN** old snapshots SHALL NOT be reused or republished and the next startup SHALL rebuild indices

### Requirement: Preserve write constraints

The system SHALL preserve the existing transaction placement of index validation and SHALL NOT defer uniqueness checks until after database commit.

#### Scenario: Duplicate unique field
- **WHEN** creating a second resource with a conflicting unique indexed value
- **THEN** the operation SHALL fail without persisting the conflicting row

### Requirement: Isolate snapshot storage

The system SHALL store snapshot cache files in `indices/snapshots/` and SHALL NOT interpret files in the parent indices directory as snapshots.

#### Scenario: Legacy cache files
- **WHEN** only files from the previous snapshot layout exist
- **THEN** startup SHALL use full rebuilding without modifying unrelated index files

### Requirement: Share type lifecycles between Halo and plugins

The system SHALL save eligible indices before unregistration and restore them on subsequent registration using the same checks as a process restart.

#### Scenario: Hot plugin restart
- **WHEN** a plugin unregisters and registers an unchanged resource type
- **THEN** a fresh matching checkpoint SHALL avoid scanning its stored resources

#### Scenario: Dormant plugin data mutation
- **WHEN** a resource is written while its plugin is disabled, including after a process restart
- **THEN** the write SHALL invalidate its persisted checkpoint before data can commit and the next registration SHALL rebuild

#### Scenario: Independent resource types
- **WHEN** one type stops or has uncertain index state
- **THEN** unrelated types SHALL continue writing and retain their own snapshot eligibility

### Requirement: Ignore safe write conflicts for snapshot eligibility

The system SHALL retain snapshot eligibility after a confirmed rollback when none of the tracked store writes in that transaction emitted a result. It SHALL invalidate eligibility when a write emitted a result before rollback, when completion is unknown, or when draining times out.

#### Scenario: Stale resource version
- **WHEN** a store update fails optimistic locking before emitting a result
- **THEN** a subsequent clean shutdown SHALL still be eligible to publish snapshots

#### Scenario: Earlier write followed by conflict
- **WHEN** a transaction emitted a successful store write before another write conflicts and rolls back the transaction
- **THEN** shutdown SHALL NOT publish a checkpoint for potentially changed indices

### Requirement: Reuse across compatible releases

The system SHALL compare complete index fingerprints, existing semantic versions and shared engine compatibility rather than the application build or plugin artifact version.

#### Scenario: Unrelated release changes
- **WHEN** Halo or a plugin upgrades without changing index definitions or semantic versions
- **THEN** valid type checkpoints SHALL be restored without a full data scan

#### Scenario: Changed registration or semantics
- **WHEN** an index is added, removed or changed, its semantic version changes, or shared engine compatibility changes
- **THEN** the affected type SHALL rebuild all indices before becoming ready

#### Scenario: Semantic function changes
- **WHEN** Halo or plugin authors change an index extraction function's semantics without structural changes
- **THEN** they SHALL increment its existing index-spec version to invalidate prior snapshots

### Requirement: Order shutdown before snapshot capture

The system SHALL stop plugins and clean their bundled resources before stopping core controllers, then capture remaining root snapshots. It SHALL preserve indices until plugin unregistration captures them and retain resources marked do-not-overwrite.

#### Scenario: Late shutdown write
- **WHEN** a supported write occurs after its type checkpoint was published
- **THEN** the checkpoint SHALL be invalidated before that data can commit

#### Scenario: Plugin fails after a normal root shutdown
- **GIVEN** a running plugin supplies an enabled form login provider
- **WHEN** Halo shuts down normally and the plugin fails before loading bundled resources on the next startup
- **THEN** its old provider SHALL have been removed and the default login page SHALL remain renderable

#### Scenario: Offline upgrade removes a bundled declaration
- **WHEN** Halo shuts down normally and the plugin package is replaced with one that omits a prior declaration
- **THEN** the old declaration SHALL NOT remain active, and compatible business-data indices SHALL remain eligible for snapshot recovery
