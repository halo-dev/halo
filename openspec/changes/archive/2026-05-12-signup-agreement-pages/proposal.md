## Why

Halo's user registration page currently lacks a privacy policy / terms of service agreement checkbox, which is a common compliance requirement (e.g., GDPR). While theme authors can customize the auth page to add this, the configuration is lost when switching themes. Therefore, built-in support is needed. See [halo-dev/halo#7452](https://github.com/halo-dev/halo/issues/7452).

## What Changes

- Add a new multi-select setting in **User Settings**: **Required Agreement Pages for Signup**, allowing administrators to choose independent pages (e.g., privacy policy, terms of service) that users must agree to during registration.
- Add `agreedToTerms` field to `SignUpData` to capture whether the user checked the agreement checkbox.
- Add validation in `UserServiceImpl.signUp()`: when agreement pages are configured, require `agreedToTerms == true`.
- Dynamically render the agreement checkbox with page links at the bottom of the registration page (`signup.html`).
- Do not put agreement page information in `GlobalInfo`; pass it only as a local variable when rendering the signup page in `PreAuthSignUpEndpoint`.
- Add related i18n text (en/es/zh/zh-TW).

## Capabilities

### New Capabilities

- `signup-agreement`: Agreement acceptance on the signup page, including settings configuration, backend validation, and frontend rendering.

### Modified Capabilities

- None.

## Impact

- `api/src/.../infra/SystemSetting.java` — Add `requiredAgreementPages: List<String>` to `User` class
- `api/src/.../core/user/service/SignUpData.java` — Add `agreedToTerms: Boolean`
- `application/src/.../security/preauth/PreAuthSignUpEndpoint.java` — Query agreement page permalinks in GET handler and put into model; add error handling in POST handler
- `application/src/.../core/user/service/impl/UserServiceImpl.java` — Validate agreement acceptance in `signUp()`
- `application/src/.../resources/extensions/system-setting.yaml` — Add `singlePageSelect` multi-select field
- `application/src/.../templates/gateway_fragments/signup.html` — Render checkbox and links
- `application/src/.../templates/gateway_fragments/signup*.properties` — Add i18n keys

