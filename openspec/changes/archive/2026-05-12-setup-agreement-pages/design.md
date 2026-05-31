## Context

Halo introduced the registration agreement consent feature in PR #9978: administrators can set `SystemSetting.User.requiredAgreementPages` in the admin console to specify which SinglePage pages must be agreed to during registration. Current pain points:

1. Administrators must manually create the Terms of Service and Privacy Policy SinglePages
2. Administrators must manually enter the page names into `requiredAgreementPages` in settings
3. New users deploying Halo for the first time are unaware of this workflow, resulting in low actual adoption of the registration agreement feature

Halo already has the capability to load default data during system initialization via `initial-data.yaml` (default category, tag, post, about page, menu, etc.).

## Goals / Non-Goals

**Goals:**
- Automatically create two default SinglePages during system setup: Terms of Service and Privacy Policy
- Automatically configure these two pages as required agreement pages for registration (write to `requiredAgreementPages`)
- Stay consistent with the existing initialization mechanism (`initial-data.yaml` + `system-configurable-configmap.yaml`)

**Non-Goals:**
- Do not modify the frontend registration page UI (the frontend already dynamically reads agreement pages via `fetchAgreementPages()`)
- Do not provide legally compliant agreement content (only placeholder templates, prompting users to replace them)
- Do not support multi-language agreement content (use Chinese placeholder content, consistent with existing default data)
- Do not modify the agreement consent validation logic (reuse existing `UserServiceImpl.signUp()` logic)

## Decisions

### 1. Use `initial-data.yaml` to define default agreement pages
**Rationale**: Uses the same mechanism as existing default data (post, about page, menu, etc.), requiring no new code to load them uniformly in `initializeNecessaryData()`. The placeholder replacement mechanism (`${username}`, `${timestamp}`) is reusable.

### 2. Auto-configure `requiredAgreementPages` in `system-configurable-configmap.yaml`
**Rationale**: The `system-default` ConfigMap is loaded automatically on startup and serves as the default system configuration. Setting `requiredAgreementPages` directly in this file is simpler than modifying it at runtime via Java code, and it ensures the configuration is present from the beginning without any additional setup logic.

### 3. Use readable names as `metadata.name` for agreement pages
**Rationale**: Readable names (`user-agreement`, `privacy-policy`) are more intuitive than UUIDs and make the configuration self-documenting. This also simplifies referencing the pages in `system-configurable-configmap.yaml`.

**Selected names**:
- Terms of Service: `user-agreement`
- Privacy Policy: `privacy-policy`

### 4. Pages initial state: `publish: true`, `visible: PUBLIC`
**Rationale**: Agreement pages must be accessible to unauthenticated users (during registration), so they must be publicly visible.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Users may not want mandatory registration agreements, but they are auto-enabled after setup | Administrators can clear `requiredAgreementPages` or delete the pages at any time in the admin console; this behavior is equivalent to manual configuration, just enabled by default |
| Agreement page content may not comply with local regulations | Page content includes a clear prompt "Please replace with actual content"; consistent with the placeholder nature of the existing default post "Hello Halo" |
| `requiredAgreementPages` references deleted pages | Registration skips missing pages gracefully via existing `fetchAgreementPages()` error-ignoring behavior |

## Migration Plan

- No migration needed: only affects the initialization flow of newly installed Halo instances
- Existing running instances are unaffected (pages are not auto-created nor config modified)
