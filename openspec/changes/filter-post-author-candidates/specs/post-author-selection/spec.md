## ADDED Requirements

### Requirement: Post author selectors list only author-capable users
Post owner selectors in Console post settings SHALL list only users whose effective roles include `role-template-post-contributor`.

#### Scenario: Ordinary registered user is excluded
- **WHEN** an administrator opens the post settings owner selector
- **AND** a registered user has only the default guest role
- **THEN** that user SHALL NOT appear in the owner selector results

#### Scenario: Contributor user is included
- **WHEN** an administrator opens the post settings owner selector
- **AND** a user has a directly assigned role whose effective permissions include `role-template-post-contributor`
- **THEN** that user SHALL appear in the owner selector results

#### Scenario: Batch owner selector uses the same eligibility
- **WHEN** an administrator opens the batch post settings owner selector
- **THEN** the selector SHALL use the same author-capable user eligibility as the single post settings owner selector

### Requirement: Author eligibility uses post contributor capability
The system SHALL use `role-template-post-contributor` as the minimum role template for post author eligibility.

#### Scenario: Publisher-only user is excluded
- **WHEN** a user has an effective role set that includes `role-template-post-publisher`
- **AND** the effective role set does not include `role-template-post-contributor`
- **THEN** that user SHALL NOT be considered an author-capable user

#### Scenario: Post author role is included through dependencies
- **WHEN** a user has `post-author` or another role that depends on `role-template-post-author`
- **THEN** the user SHALL be considered author-capable because the dependency chain includes `role-template-post-contributor`

### Requirement: Console user listing supports effective role filtering
The Console user listing API SHALL provide a non-breaking query parameter for filtering users whose effective role set contains a requested role.

#### Scenario: Direct role filter remains unchanged
- **WHEN** a request uses the existing `role` query parameter
- **THEN** the API SHALL continue filtering by directly assigned role names

#### Scenario: Effective role filter includes dependency matches
- **WHEN** a request uses the effective role query parameter with `role-template-post-contributor`
- **AND** a user has a directly assigned custom role that depends on `role-template-post-contributor`
- **THEN** the API SHALL include that user in the response

#### Scenario: Super user is included
- **WHEN** a request uses the effective role query parameter with `role-template-post-contributor`
- **AND** a user has the `super-role`
- **THEN** the API SHALL include that user in the response

#### Scenario: Unknown effective role returns no users
- **WHEN** a request uses the effective role query parameter with a role name that does not exist
- **THEN** the API SHALL return no matching users

### Requirement: Existing owners remain displayable
Post owner selectors SHALL continue resolving already-selected owner values by username even when those users are not eligible for new author selection.

#### Scenario: Existing owner loses authoring permission
- **WHEN** a post already has `spec.owner` set to a user
- **AND** that user no longer has effective `role-template-post-contributor`
- **THEN** the post settings modal SHALL still display the current owner value
- **AND** the user SHALL NOT appear in new owner search results
