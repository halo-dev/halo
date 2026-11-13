# Security Policy

## Supported Versions

Halo currently supports the versions listed below, where as:

- :white_check_mark: indicates an active development roadmap, is therefore maintaining, and **will** receive Security
  Vulnerability Report.
- :x: indicates such version has already deprecated and **will not** be receiving Security Vulnerability Report.

| Version |     Supported      |
|---------|--------------------|
| 0.x     | :x:                |
| 1.x     | :x:                |
| 2.x     | :white_check_mark: |

## Reporting a Vulnerability

We first appreciate and are very thankful that you've found a vulnerability issue in Halo! By disclosing such issue to
Halo development team you are helping Halo to become a much more safer project than before! ;)

To protect the existing users of Halo, we kindly ask you to not disclose the vulnerability to anyone except the Halo
development team before a fix has been rolled out.

To report a vulnerability, please use
[GitHub private vulnerability reporting](https://github.com/halo-dev/halo/security/advisories/new). This allows you to
share the details privately with the Halo development team, and we will work with you on a fix and coordinated
disclosure. The report form will ask for the affected versions, deployment method, environment details, reproduction
steps, and impact.

If you are unable to use GitHub private vulnerability reporting, you can instead send the same information by email to
`hi@halo.run`.

## What to Expect

- We will acknowledge your report within 5 business days.
- We will investigate the issue and keep you informed of our progress.
- We aim to release a fix within 90 days. We will coordinate the disclosure date with you, and credit you in the
  security advisory and release notes unless you prefer to remain anonymous.

## Scope

This policy covers vulnerabilities in the Halo core repository. The following are generally out of scope:

- Issues that require an already-compromised administrator account, or existing access to the server or database.
- Vulnerabilities in third-party plugins, themes, or dependencies that do not have a demonstrated impact on Halo.
  Please report those to their respective maintainers.
- Reports for unsupported versions (0.x and 1.x).
- Reports from automated scanners without a working proof of concept.

