# category-hierarchy Specification

## Purpose
Define how Halo represents, migrates, renders, and manages post category hierarchy through parent references while preserving legacy `children` compatibility during the transition.

## Requirements
### Requirement: Categories express parent relationships

The system SHALL represent category hierarchy through `Category.spec.parent` after migration.

#### Scenario: Category is a root category

- **WHEN** a Category is a root category after migration or new Console creation
- **THEN** the Category SHALL have `spec.parent` unset or null

#### Scenario: Category is a child category

- **WHEN** a Category is nested under another Category after migration or new Console creation
- **THEN** the Category SHALL set `spec.parent` to the parent Category `metadata.name`

#### Scenario: Legacy children field remains available

- **WHEN** the API schema describes `Category.spec.children`
- **THEN** `Category.spec.children` SHALL remain present
- **AND** `Category.spec.children` SHALL be marked deprecated

#### Scenario: Parent field is indexed

- **WHEN** Category schemes are registered
- **THEN** the system SHALL provide an index for `spec.parent`
- **AND** the system SHALL retain the existing `spec.children` index during the compatibility period

### Requirement: Legacy category hierarchy data is migrated safely

The system SHALL migrate legacy hierarchy data from `Category.spec.children` to `Category.spec.parent` without rewriting legacy children values.

#### Scenario: Existing legacy category tree is migrated

- **WHEN** Halo starts with Categories whose `spec.children` values form a valid legacy tree
- **THEN** the migration SHALL assign missing child `spec.parent` values from the legacy parent categories
- **AND** the migration SHALL leave every `Category.spec.children` value unchanged

#### Scenario: Existing parent value takes precedence

- **WHEN** a Category already has `spec.parent`
- **THEN** the migration SHALL NOT overwrite that `spec.parent` value

#### Scenario: Existing parent disagrees with legacy children

- **WHEN** legacy `spec.children` implies a parent different from an existing `spec.parent`
- **THEN** the migration SHALL preserve the existing `spec.parent`
- **AND** the migration SHALL report the inconsistent legacy edge

#### Scenario: Missing child reference is encountered

- **WHEN** a legacy `spec.children` entry references a missing Category
- **THEN** the migration SHALL skip that reference
- **AND** the migration SHALL continue processing other categories

#### Scenario: Category is referenced by multiple legacy parents

- **WHEN** the same Category is referenced by multiple legacy parent categories
- **THEN** the migration SHALL choose one parent deterministically
- **AND** the migration SHALL skip the other conflicting parent edges
- **AND** the migration SHALL NOT clone the Category

#### Scenario: Legacy cycle is encountered

- **WHEN** following legacy `spec.children` edges would create a category cycle
- **THEN** the migration SHALL skip the cyclic edge
- **AND** the migration SHALL continue processing acyclic edges

#### Scenario: Migration is retried

- **WHEN** the migration runs more than once
- **THEN** the migration SHALL NOT change already-consistent `spec.parent` values
- **AND** the migration SHALL NOT duplicate or clone categories

#### Scenario: Migration label is inconsistent

- **WHEN** a Category has the migration marker label but its hierarchy fields remain incomplete
- **THEN** the migration SHALL inspect the actual fields and attempt safe migration again

### Requirement: Runtime category hierarchy uses parent references

The system SHALL build runtime category hierarchy from `Category.spec.parent` without falling back to `Category.spec.children`.

#### Scenario: Category children are listed

- **WHEN** the system lists children for a Category name
- **THEN** the result SHALL include the requested Category
- **AND** the result SHALL include descendants discovered through `spec.parent`

#### Scenario: Post cascade traversal reaches descendants

- **WHEN** post queries use category-with-children behavior
- **THEN** descendants SHALL be discovered through `spec.parent`
- **AND** traversal SHALL stop below any descendant whose `spec.preventParentPostCascadeQuery` is true

#### Scenario: Parent category is resolved

- **WHEN** the system resolves the parent of a Category
- **THEN** it SHALL use the Category's `spec.parent` value to fetch the parent Category

#### Scenario: Legacy children disagree with parent

- **WHEN** `Category.spec.children` disagrees with `Category.spec.parent`
- **THEN** runtime category hierarchy behavior SHALL use `spec.parent`
- **AND** runtime category hierarchy behavior SHALL NOT fall back to `spec.children`

#### Scenario: Parent reference is invalid

- **WHEN** a Category's `spec.parent` is missing, self-referential, points to a missing Category, or participates in a cycle
- **THEN** runtime tree rendering SHALL treat the affected Category as a root Category

### Requirement: Hidden category behavior is preserved

The system SHALL preserve existing hidden-category semantics while resolving hierarchy through `spec.parent`.

#### Scenario: Ancestor hides descendant

- **WHEN** a Category has an ancestor whose `spec.hideFromList` is true
- **THEN** `isCategoryHidden` for the descendant SHALL return true

#### Scenario: Hidden state propagates to descendants

- **WHEN** a Category hidden state is reconciled
- **THEN** the system SHALL propagate that hidden state to descendants discovered through `spec.parent`
- **AND** propagation SHALL NOT be stopped by `spec.preventParentPostCascadeQuery`

#### Scenario: Hidden root categories are excluded from public lists

- **WHEN** public category lists or full category trees are rendered without a specific hidden target
- **THEN** Categories whose root tree is hidden SHALL remain excluded from theme-side lists

#### Scenario: Hidden target subtree remains accessible

- **WHEN** a client asks for the category tree or breadcrumbs of a hidden Category by name
- **THEN** the system SHALL still be able to return that target subtree or breadcrumb path

### Requirement: Theme category output remains compatible

The system SHALL preserve existing theme-facing category value object shapes while sourcing hierarchy from `spec.parent`.

#### Scenario: Category tree is returned to themes

- **WHEN** a theme queries category trees
- **THEN** child Categories SHALL be nested under `CategoryTreeVo.children`
- **AND** each child CategoryTreeVo SHALL expose `parentName`

#### Scenario: Category spec remains included

- **WHEN** a theme receives `CategoryVo` or `CategoryTreeVo`
- **THEN** the value object SHALL include the stored Category spec
- **AND** stored legacy `spec.children` values SHALL NOT be recomputed from `spec.parent`

#### Scenario: Breadcrumbs are rendered

- **WHEN** a theme queries breadcrumbs for a Category
- **THEN** the breadcrumb path SHALL be derived from `spec.parent`
- **AND** the output SHALL remain a list of `CategoryVo` values

### Requirement: Console category management writes parent references

The Console SHALL manage category hierarchy through `Category.spec.parent` and `Category.spec.priority`.

#### Scenario: Console builds category tree

- **WHEN** Console loads Categories for category management or category selection
- **THEN** Console SHALL build the editable tree from `spec.parent`

#### Scenario: Console creates a root category

- **WHEN** an administrator creates a root Category
- **THEN** Console SHALL create the Category without `spec.parent`
- **AND** Console SHALL NOT add the Category name to another Category's `spec.children`

#### Scenario: Console creates a child category

- **WHEN** an administrator creates a Category under a parent Category
- **THEN** Console SHALL create the Category with `spec.parent` equal to the selected parent Category name
- **AND** Console SHALL NOT append the child Category name to the parent's `spec.children`

#### Scenario: Console saves drag-and-drop hierarchy

- **WHEN** an administrator saves a drag-and-drop category hierarchy change
- **THEN** Console SHALL patch affected Categories with updated `spec.parent` and `spec.priority`
- **AND** Console SHALL NOT patch `Category.spec.children`

#### Scenario: Console moves category to root

- **WHEN** an administrator moves a Category to the root level
- **THEN** Console SHALL remove `/spec/parent` for that Category instead of sending a null parent value

#### Scenario: Category select uses parent tree

- **WHEN** the shared category select input renders category options, keyboard navigation, or search result paths
- **THEN** it SHALL use the tree derived from `spec.parent`
