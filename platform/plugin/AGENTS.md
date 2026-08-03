# Plugin BOM Guidelines

`platform/plugin/` is a BOM constraining plugin-facing dependencies.

- Combine changes here with the matching `api/` contract changes, since plugins compile against both.
- Verify compatibility with the plugin ecosystem before upgrading or adding constraints.
- Changes affecting the plugin API require approval, as with `api/` changes.

