## Why

Halo PR #9978 already supports requiring users to agree to terms of service or privacy policy during registration, but the current workflow requires manual enablement in settings and manual creation of corresponding SinglePage pages by the administrator. This is cumbersome and unintuitive. To lower the configuration barrier and improve out-of-the-box experience, the system should automatically create two default SinglePage pages (Terms of Service and Privacy Policy) during the setup phase, and automatically configure them as required agreement pages for registration.

## What Changes

- **Extend `initial-data.yaml`**: Add two SinglePages (Terms of Service, Privacy Policy) and their associated Snapshot data as default pages during system initialization.
- **Auto-configure `requiredAgreementPages`**: In `SystemSetupEndpoint.doInitialization()`, after creating the default data, automatically write the two agreement page names into `SystemSetting.User.requiredAgreementPages` so that registration requires agreement by default.
- **Default page content**: Provide concise placeholder content prompting the administrator to replace it with actual content in the admin console.

## Capabilities

### New Capabilities
- `setup-agreement-pages`: Automatically create Terms of Service and Privacy Policy pages during system setup, and auto-configure them as required agreement pages for registration.

### Modified Capabilities
- `system-setup`: Extend the initialization flow to add automatic configuration of agreement pages after creating initial data.

## Impact

- `application/src/main/resources/initial-data.yaml` — Add two SinglePage + Snapshot definitions
- `application/src/main/java/run/halo/app/security/preauth/SystemSetupEndpoint.java` — Add logic to auto-configure `requiredAgreementPages` in `doInitialization()`
- No API changes, no database schema changes, no breaking changes
