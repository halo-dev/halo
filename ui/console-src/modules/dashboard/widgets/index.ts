import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";

export const internalWidgetDefinitions: DashboardWidgetDefinition[] = [
  {
    name: "core:post:stats",
    componentName: "PostStatsWidget",
    group: "core.dashboard.widgets.groups.post",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 3,
      h: 3,
    },
  },
  {
    name: "core:post:recent-published",
    componentName: "RecentPublishedWidget",
    group: "core.dashboard.widgets.groups.post",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 12,
    },
    permissions: ["system:posts:view"],
  },
  {
    name: "core:singlepage:stats",
    componentName: "SinglePageStatsWidget",
    group: "core.dashboard.widgets.groups.page",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 3,
      h: 3,
    },
    permissions: ["system:singlepages:view"],
  },
  {
    name: "core:comment:stats",
    componentName: "CommentStatsWidget",
    group: "core.dashboard.widgets.groups.comment",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 3,
      h: 3,
    },
    permissions: ["system:comments:view"],
  },
  {
    name: "core:user:stats",
    componentName: "UserStatsWidget",
    group: "core.dashboard.widgets.groups.user",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 3,
      h: 3,
    },
  },
  {
    name: "core:view:stats",
    componentName: "ViewsStatsWidget",
    group: "core.dashboard.widgets.groups.other",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 3,
      h: 3,
    },
  },
  {
    name: "core:quick-link",
    componentName: "QuickLinkWidget",
    group: "core.dashboard.widgets.groups.other",
    // TODO: remove this, just for testing
    configFormKitSchema: [
      {
        $formkit: "text",
        name: "title",
        label: "Title",
        placeholder: "Title",
      },
    ],
    defaultConfig: {
      title: "Quick Link",
    },
    defaultSize: {
      w: 6,
      h: 12,
    },
  },
  {
    name: "core:notifications",
    componentName: "NotificationWidget",
    group: "core.dashboard.widgets.groups.other",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 12,
    },
  },
];
