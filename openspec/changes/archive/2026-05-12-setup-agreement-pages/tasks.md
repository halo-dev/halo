## 1. Initial Data Definition

- [x] 1.1 Add Terms of Service Snapshot in `initial-data.yaml` (with placeholder HTML content)
- [x] 1.2 Add Terms of Service SinglePage in `initial-data.yaml` (slug: `user-agreement`, publish: true, visible: PUBLIC)
- [x] 1.3 Add Privacy Policy Snapshot in `initial-data.yaml` (with placeholder HTML content)
- [x] 1.4 Add Privacy Policy SinglePage in `initial-data.yaml` (slug: `privacy-policy`, publish: true, visible: PUBLIC)

## 2. Backend Initialization Logic

- [x] 2.1 Add `configureAgreementPages` method in `SystemSetupEndpoint` to read the `user` group from `system` ConfigMap and write the two agreement page names into `requiredAgreementPages`
- [x] 2.2 Add `configureAgreementPages` into the `Mono.when` parallel task list in `doInitialization()`
- [x] 2.3 Add the same optimistic locking retry strategy for `configureAgreementPages` as used by `mergeToBasicConfig`

## 3. Verification and Testing

- [x] 3.1 Run `./gradlew spotlessApply` to format code
- [x] 3.2 Run `./gradlew :application:test` to ensure existing tests pass
- [x] 3.3 Add test in `SystemSetupEndpointTest` to verify `requiredAgreementPages` contains both agreement page names after initialization
- [x] 3.4 Manual verification: fresh install Halo, complete setup, then check that the registration page displays agreement links and requires consent to register
