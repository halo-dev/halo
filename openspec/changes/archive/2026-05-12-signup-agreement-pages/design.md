## Context

Halo's registration page (`/signup`) is served by `PreAuthSignUpEndpoint` and rendered using the Thymeleaf template `gateway_fragments/signup.html`. User settings are stored in `SystemSetting.User` and read via `SystemConfigFetcher`. `GlobalInfo` is responsible for passing global configuration to authentication-related templates (e.g., `allowRegistration`, `mustVerifyEmailOnRegistration`).

The current registration flow lacks a compliance-required agreement acceptance mechanism. While theme authors can customize the auth page, the configuration is lost when switching themes.

## Goals / Non-Goals

**Goals:**
- Allow administrators to select one or more independent pages that users must agree to during registration.
- Dynamically display a required agreement checkbox with hyperlinks on the signup page when pages are configured.
- Reject registration on the backend if the user does not check the agreement checkbox.
- Do not modify `GlobalInfo`; keep its responsibility focused.

**Non-Goals:**
- Do not introduce new database tables or migration scripts (reuse existing ConfigMap setting storage).
- Do not modify the UC registration flow (Halo currently has only one registration entrypoint at `/signup`).
- Do not modify the theme system or plugin APIs.

## Decisions

### 1. Use `singlePageSelect` with `multiple: true`

- **Choice**: Use `$formkit: singlePageSelect` with `multiple: true` in `system-setting.yaml`.
- **Rationale**: `singlePageSelect` already exists at `ui/src/formkit/inputs/singlePage-select.ts` and supports the `multiple` prop. The returned value is a `List<String>` (SinglePage metadata.name). The backend can query the permalink via `ReactiveExtensionClient.get(SinglePage.class, name)`.
- **Alternative considered**: Two separate fields (privacy policy, terms of service). Rejected because the user explicitly requested a single form field.

### 2. Do not put data in `GlobalInfo`

- **Choice**: Pass the agreement page permalink list only as a Thymeleaf model variable in the `PreAuthSignUpEndpoint` GET handler.
- **Rationale**: `GlobalInfo` is globally shared, while agreement pages are only needed on the signup page. Avoids polluting the global context.
- **Alternative considered**: Add to `GlobalInfo`. Rejected because it adds unnecessary global state, and the user explicitly requested not to use `GlobalInfo`.

### 3. Validate in `UserServiceImpl.signUp()`

- **Choice**: Perform agreement acceptance validation inside `UserServiceImpl.signUp()`.
- **Rationale**: `UserServiceImpl` already injects `SystemConfigFetcher` and loads `SystemSetting.User`. All registration business rules are centralized here (allow registration, protected usernames, default role, etc.).
- **Alternative considered**: Validate in `PreAuthSignUpEndpoint`. Rejected because it would require an extra settings query in the HTTP layer, increasing complexity; business rules should stay in the service layer.

### 4. Reuse existing error handling pattern

- **Choice**: Define `AgreementNotAcceptedException`, catch it in `PreAuthSignUpEndpoint` via `doOnError` + `onErrorResume`, and render it as a `FieldError`.
- **Rationale**: This is consistent with the existing error handling pattern for `DuplicateNameException`, `RestrictedNameException`, etc.

## Risks / Trade-offs

- **[Risk]** Selected SinglePage may be deleted or unpublished, causing a 404 on the signup page links.
  - **Mitigation**: Filter out pages without a permalink during backend query; do not enforce page existence (administrator's responsibility).
- **[Risk]** JSON serialization/deserialization of `List<String>` in ConfigMap may be incompatible.
  - **Mitigation**: `SystemSetting.AuthProvider` already uses `List<AuthProviderState>`, proving Jackson deserialization of list types works. `List<String>` is even simpler.
- **[Risk]** Multi-language i18n maintenance cost.
  - **Mitigation**: Only one new error message key is added, minimal impact.

## Migration Plan

No migration needed. The new setting field defaults to absent (`null`), which disables agreement validation, preserving existing behavior.

## Open Questions

None.
