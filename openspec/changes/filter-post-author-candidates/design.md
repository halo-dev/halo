## Context

The post settings modal and batch settings modal currently use the shared FormKit `userSelect` input for `spec.owner`. The input calls the Console user listing API and only excludes built-in anonymous/deleted users, so ordinary registered users can appear as author candidates.

Halo already models post authoring through RBAC role templates:

- `role-template-post-contributor` grants user-center post create/update/delete and draft read/update operations.
- `role-template-post-publisher` grants publish/unpublish operations only.
- `role-template-post-author` depends on both contributor and publisher templates.
- Console post managers depend on `role-template-post-author`.

The minimum role for "can become an author" is therefore `role-template-post-contributor`, not `role-template-post-publisher`.

## Goals / Non-Goals

**Goals:**

- List only users whose effective roles include `role-template-post-contributor` in post owner selectors.
- Preserve support for custom roles and aggregate/dependency-based role composition.
- Keep the generic user selector reusable for contexts that still need all users.
- Preserve existing direct-role filtering semantics for the user management list.

**Non-Goals:**

- Do not change post owner validation or historical ownership data in this change.
- Do not redefine built-in role templates or their permissions.
- Do not add new database tables or migrations.
- Do not hide disabled users unless existing user listing behavior already does so.

## Decisions

### Use `role-template-post-contributor` as the author eligibility role

`role-template-post-contributor` is the lowest built-in role template that can create and update posts and drafts. `role-template-post-publisher` only controls publish and unpublish operations, so using it would admit users who can publish but cannot edit author-owned content.

Alternative considered: filter by `role-template-post-publisher`. Rejected because publishing is not the minimum authoring capability.

### Add an effective-role filter instead of reusing `role`

The existing `role` query parameter filters users by directly assigned role names stored in the user role index. This is useful for user management but does not account for dependencies such as `post-editor -> role-template-manage-posts -> role-template-post-author -> role-template-post-contributor`.

Add a new Console user listing query parameter, tentatively named `effectiveRole`, that means "the user's directly assigned role set effectively contains this role after dependency expansion." Keep `role` unchanged for direct role filtering.

Alternative considered: change `role` to dependency-aware filtering. Rejected because it would change existing user management semantics and could surprise administrators using direct role filters.

### Resolve eligible direct roles on the backend

The backend should resolve the set of directly assignable roles whose effective permissions contain the requested `effectiveRole`, including `super-role` and custom roles. The user list query can then continue to use the existing `User.USER_RELATED_ROLES_INDEX` index with an `in(...)` predicate over the resolved direct role names.

This keeps pagination, sorting, keyword search, and field selectors in the extension store query path.

Alternative considered: fetch broad user pages and filter `ListedUser.roles` after pagination. Rejected because it would make total counts wrong and could return sparse pages.

### Keep `userSelect` generic and use local post author options

Keep the shared FormKit `userSelect` input unchanged because this eligibility filter is currently specific to post ownership. The post settings and batch settings modals should fetch author-capable users locally with `effectiveRole=role-template-post-contributor` and pass the result to a native FormKit `select` as `options`.

The single post settings modal should additionally resolve the current owner by name when it is not present in the author-capable option list, so existing posts can still display their current owner if that owner later loses authoring permission.

Alternative considered: extend the generic `userSelect` with optional backend filters. Rejected for now because the author eligibility filter is a post-specific workflow and does not need to become part of the shared user selector contract.

## Risks / Trade-offs

- Effective-role resolution may scan role definitions per request. Mitigation: keep the resolution in an application-side role resolver near the existing role service so it can reuse the same role dependency model and be optimized or cached later if needed.
- An unknown `effectiveRole` could accidentally fall back to all users. Mitigation: return an empty eligible-role set and an empty user result when the effective role cannot be resolved.
- Existing owners may no longer appear in author options after permission changes. Mitigation: resolve the current owner by metadata name independently of the author-candidate query in the single post settings modal.
- The new API parameter changes generated UI client code. Mitigation: regenerate OpenAPI docs and the UI API client in the implementation.
