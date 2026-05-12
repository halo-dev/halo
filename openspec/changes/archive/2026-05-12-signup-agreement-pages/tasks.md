## 1. API Module Changes

- [x] 1.1 Add `requiredAgreementPages: List<String>` field to `SystemSetting.User`
- [x] 1.2 Add `agreedToTerms: Boolean` field to `SignUpData`

## 2. Backend Service and Endpoint

- [x] 2.1 Add agreement acceptance validation logic in `UserServiceImpl.signUp()`
- [x] 2.2 Define `AgreementNotAcceptedException` exception class
- [x] 2.3 Query `requiredAgreementPages` corresponding SinglePage permalinks in `PreAuthSignUpEndpoint` GET handler and put into Thymeleaf model
- [x] 2.4 Catch `AgreementNotAcceptedException` in `PreAuthSignUpEndpoint` POST handler and render as `FieldError`

## 3. Settings Configuration and Templates

- [x] 3.1 Add `singlePageSelect` multi-select field (`requiredAgreementPages`) to the user group in `system-setting.yaml`
- [x] 3.2 Modify `signup.html` to render the agreement acceptance checkbox and page links at the bottom
- [x] 3.3 Update `signup.properties` (Chinese) with new i18n text
- [x] 3.4 Update `signup_en.properties` (English) with new i18n text
- [x] 3.5 Update `signup_es.properties` (Spanish) with new i18n text
- [x] 3.6 Update `signup_zh_TW.properties` (Traditional Chinese) with new i18n text

## 4. Validation and Testing

- [x] 4.1 Run `./gradlew spotlessApply` to format Java code
- [x] 4.2 Run `./gradlew :application:test` to ensure existing tests pass
- [x] 4.3 Add unit tests for agreement validation logic in `UserServiceImpl.signUp()`
- [x] 4.4 Add tests for new logic in `PreAuthSignUpEndpoint`

