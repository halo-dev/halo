# console-global-search Specification

## Purpose

Define the required behavior for fast, stable, and permission-aware global search in the Halo console.

## Requirements

### Requirement: Hybrid search sources
The console global search SHALL combine local, cached, and remote sources without loading unbounded resource collections into the browser.

#### Scenario: Build the local source
- **WHEN** the global-search modal is opened
- **THEN** the system builds a local searchable source from accessible console routes

#### Scenario: Use cached bounded sources
- **WHEN** the current user can access plugins, categories, system settings, or theme settings
- **THEN** the system searches those bounded collections through cached local data

#### Scenario: Query unbounded sources remotely
- **WHEN** the user enters a non-empty keyword
- **THEN** the system queries posts, single pages, attachments, users, and tags through their existing Console API keyword endpoints instead of fetching their complete collections

### Requirement: Keyword-triggered remote search
The system SHALL normalize and debounce keywords before starting remote searches.

#### Scenario: Empty keyword
- **WHEN** the keyword is empty after trimming whitespace
- **THEN** the system does not issue remote search requests

#### Scenario: Non-empty keyword
- **WHEN** the user enters a keyword that remains unchanged for 300 milliseconds
- **THEN** the system starts the permitted remote providers in parallel

#### Scenario: Single-character keyword
- **WHEN** the normalized keyword contains one character
- **THEN** the system treats it as a valid remote-search keyword

### Requirement: Search caching
The system SHALL use bounded caches to avoid repeating equivalent searches while limiting stale results and memory retention.

#### Scenario: Reopen with fresh bounded-source data
- **WHEN** the modal is reopened within 60 seconds of loading a bounded source
- **THEN** the system reuses the cached source data without issuing another request

#### Scenario: Reopen with stale bounded-source data
- **WHEN** cached bounded-source data is older than 60 seconds
- **THEN** the system may display the cached data while refreshing it in the background

#### Scenario: Repeat a recent keyword
- **WHEN** the same normalized keyword is searched again within 30 seconds
- **THEN** the system may reuse the cached remote results

#### Scenario: Retire inactive keyword results
- **WHEN** a remote keyword result has had no active consumer for 5 minutes
- **THEN** the system removes it from the query cache

### Requirement: Deterministic result composition
The system SHALL compose a stable result list with explicit source quotas instead of comparing unrelated server and client relevance scores.

#### Scenario: Enforce result limits
- **WHEN** local, cached, and remote providers return more than 20 matching items
- **THEN** the system returns at most 20 items, including at most 8 local or cached items and at most 4 items from each remote provider

#### Scenario: Share remote capacity
- **WHEN** multiple remote providers have results
- **THEN** the system fills the remaining result capacity through a stable provider rotation so that one provider cannot consume every remote position

#### Scenario: Order local results
- **WHEN** local or cached items match the keyword
- **THEN** the system orders them using their Fuse relevance

#### Scenario: Order remote results
- **WHEN** remote providers return results
- **THEN** posts, single pages, and attachments are ordered with newer items first, while users and tags are ordered by display name

### Requirement: Stable asynchronous interaction
The system SHALL prevent asynchronous provider updates from changing the meaning of the user's current selection.

#### Scenario: Keyword changes
- **WHEN** the normalized keyword changes
- **THEN** the system resets selection to the first available result

#### Scenario: Results arrive for the current keyword
- **WHEN** additional provider results are merged for the unchanged keyword
- **THEN** the system preserves the selected item by its stable source-and-resource identifier when that item remains available

#### Scenario: A stale request completes
- **WHEN** a request for an older keyword completes after the current keyword has changed
- **THEN** the system ignores that response and does not replace current results

### Requirement: Isolated provider failures
The system SHALL isolate remote-provider failures from successful local and remote results.

#### Scenario: One provider fails
- **WHEN** one remote provider fails and another source has results
- **THEN** the system keeps the successful results visible and reports that some search results are temporarily unavailable

#### Scenario: Failed keyword remains unchanged
- **WHEN** a provider fails for the current keyword
- **THEN** the system does not automatically retry that provider until the keyword changes or the modal is reopened

### Requirement: Permission-aware results
The system SHALL only display results whose destination the current user is permitted to open.

#### Scenario: Inaccessible route
- **WHEN** a searchable console route requires permissions the current user does not have
- **THEN** the system excludes that route from local results

#### Scenario: Inaccessible resource destination
- **WHEN** a remote provider's destination route requires permissions the current user does not have
- **THEN** the system does not query or display that provider's results

#### Scenario: Editor destination
- **WHEN** a post or single-page result navigates to an editor route
- **THEN** the system requires the corresponding manage permission instead of only the view permission

### Requirement: Search result context
The system SHALL provide concise context for remote matches while preserving the existing title, icon, and source label.

#### Scenario: Content match context
- **WHEN** a post or single page is displayed
- **THEN** the result shows its slug, or a truncated excerpt when the keyword matches only the excerpt

#### Scenario: Resource match context
- **WHEN** a user, tag, or attachment is displayed
- **THEN** the result shows the username, tag slug, or attachment media type respectively

### Requirement: Search states
The system SHALL distinguish the initial, searching, partial-failure, and final-empty states.

#### Scenario: Initial state
- **WHEN** the normalized keyword is empty
- **THEN** the modal prompts the user to enter a keyword instead of reporting that no results exist

#### Scenario: Remote search in progress
- **WHEN** local results are available and at least one remote provider is pending
- **THEN** the modal displays the local results immediately together with a non-blocking search-progress indicator

#### Scenario: Final empty state
- **WHEN** every permitted provider has completed successfully and no source has a result
- **THEN** the modal reports that no search results were found

### Requirement: Keyboard and navigation compatibility
The system SHALL preserve the existing global-search keyboard and navigation behavior while making empty-result actions safe.

#### Scenario: Navigate results
- **WHEN** the user presses the existing up or down shortcuts
- **THEN** the selection moves within the available result bounds

#### Scenario: Confirm a result
- **WHEN** the user presses Enter with a selected result
- **THEN** the system uses the result's existing route behavior, including the existing same-route refresh behavior

#### Scenario: Confirm with no result
- **WHEN** the user presses Enter while no result is selected
- **THEN** the system performs no navigation and does not raise an error

### Requirement: Localized search feedback
The system SHALL provide localized text for every newly introduced global-search state.

#### Scenario: Render a new status message
- **WHEN** the modal displays an initial, loading, or partial-failure message
- **THEN** the message is resolved from each supported UI locale rather than being hard-coded in the component
