## 1. Backend Effective Role Filtering

- [x] 1.1 Add backend role-resolution support for finding directly assignable roles whose effective dependency closure contains a requested role.
- [x] 1.2 Update the Console user listing request model to accept an `effectiveRole` query parameter while preserving existing `role` behavior.
- [x] 1.3 Apply the resolved role-name set to the user list query through the existing `User.USER_RELATED_ROLES_INDEX` index.
- [x] 1.4 Add backend tests for direct role filtering, effective dependency matches, `super-role`, publisher-only exclusion, and unknown effective-role behavior.

## 2. Frontend Author Selector Integration

- [x] 2.1 Add a post-local author option loader that queries users with `effectiveRole=role-template-post-contributor`.
- [x] 2.2 Use a FormKit `select` with filtered author options in the post settings owner selector.
- [x] 2.3 Use a FormKit `select` with filtered author options in the batch post settings owner selector.
- [x] 2.4 Keep current post owner lookup by username independent of the author filter so existing owners remain displayable.

## 3. Generated Client And OpenAPI

- [x] 3.1 Regenerate OpenAPI docs after the Console user listing parameter changes.
- [x] 3.2 Regenerate the UI API client from the updated OpenAPI docs.
- [x] 3.3 Inspect generated API client diff to confirm only the expected `effectiveRole` request parameter was added for user listing.

## 4. Verification

- [x] 4.1 Run backend formatting/checks appropriate for the touched Java code.
- [x] 4.2 Run focused backend tests for role resolution and Console user listing.
- [x] 4.3 Run frontend typecheck and lint for the UI changes.
- [x] 4.4 Run `openspec validate filter-post-author-candidates --strict`.
