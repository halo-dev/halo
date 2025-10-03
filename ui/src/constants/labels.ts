// plugin
export enum pluginLabels {
  NAME = "plugin.halo.run/plugin-name",
}

// role
export enum roleLabels {
  TEMPLATE = "halo.run/role-template",
  HIDDEN = "halo.run/hidden",
  SYSTEM_RESERVED = "rbac.authorization.halo.run/system-reserved",
}

// post
export enum postLabels {
  DELETED = "content.halo.run/deleted",
  PUBLISHED = "content.halo.run/published",
  OWNER = "content.halo.run/owner",
  VISIBLE = "content.halo.run/visible",
  PHASE = "content.halo.run/phase",
  SCHEDULING_PUBLISH = "content.halo.run/scheduling-publish",
}

// singlePage
export enum singlePageLabels {
  DELETED = "content.halo.run/deleted",
  PUBLISHED = "content.halo.run/published",
  OWNER = "content.halo.run/owner",
  VISIBLE = "content.halo.run/visible",
  PHASE = "content.halo.run/phase",
}

// attachment
export enum attachmentPolicyLabels {
  // Used for ui display only
  HIDDEN = "storage.halo.run/policy-hidden-in-upload-ui",
  HIDDEN_WITH_JSON_PATCH = "storage.halo.run~1policy-hidden-in-upload-ui",
}
