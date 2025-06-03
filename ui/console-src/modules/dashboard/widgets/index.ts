import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";

export const internalWidgetDefinitions: DashboardWidgetDefinition[] = [
  {
    name: "core:post:stats",
    componentName: "PostStatsWidget",
    group: "dashboard",
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
    group: "dashboard",
    configFormKitSchema: [],
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 10,
    },
  },
  {
    name: "core:quick-link",
    componentName: "QuickLinkWidget",
    group: "dashboard",
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
      h: 10,
    },
  },
];
