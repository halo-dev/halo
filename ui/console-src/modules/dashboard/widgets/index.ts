import { i18n } from "@/locales";
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import CommentStatsWidget from "./presets/comments/CommentStatsWidget.vue";
import PendingCommentsWidget from "./presets/comments/PendingCommentsWidget.vue";
import IframeWidget from "./presets/core/iframe/IframeWidget.vue";
import QuickActionWidget from "./presets/core/quick-action/QuickActionWidget.vue";
import StackWidget from "./presets/core/stack/StackWidget.vue";
import ViewsStatsWidget from "./presets/core/view-stats/ViewsStatsWidget.vue";
import PostStatsWidget from "./presets/posts/PostStatsWidget.vue";
import RecentPublishedWidget from "./presets/posts/RecentPublishedWidget.vue";
import SinglePageStatsWidget from "./presets/single-pages/SinglePageStatsWidget.vue";
import NotificationWidget from "./presets/users/NotificationWidget.vue";
import UserStatsWidget from "./presets/users/UserStatsWidget.vue";

export const internalWidgetDefinitions: DashboardWidgetDefinition[] = [
  {
    id: "core:post:stats",
    component: markRaw(PostStatsWidget),
    group: "core.dashboard.widgets.groups.post",
    configFormKitSchema: () => [
      {
        $formkit: "checkbox",
        label: i18n.global.t(
          "core.dashboard.widgets.common_form.fields.enable_animation.label"
        ),
        name: "enable_animation",
      },
    ],
    defaultConfig: {
      enable_animation: true,
    },
    defaultSize: {
      w: 3,
      h: 3,
      minH: 2,
      minW: 2,
    },
  },
  {
    id: "core:post:recent-published",
    component: markRaw(RecentPublishedWidget),
    group: "core.dashboard.widgets.groups.post",
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 12,
      minH: 6,
      minW: 3,
    },
    permissions: ["system:posts:view"],
  },
  {
    id: "core:singlepage:stats",
    component: markRaw(SinglePageStatsWidget),
    group: "core.dashboard.widgets.groups.page",
    configFormKitSchema: () => [
      {
        $formkit: "checkbox",
        label: i18n.global.t(
          "core.dashboard.widgets.common_form.fields.enable_animation.label"
        ),
        name: "enable_animation",
      },
    ],
    defaultConfig: {
      enable_animation: true,
    },
    defaultSize: {
      w: 3,
      h: 3,
      minH: 2,
      minW: 2,
    },
    permissions: ["system:singlepages:view"],
  },
  {
    id: "core:comment:stats",
    component: markRaw(CommentStatsWidget),
    group: "core.dashboard.widgets.groups.comment",
    configFormKitSchema: () => [
      {
        $formkit: "checkbox",
        label: i18n.global.t(
          "core.dashboard.widgets.common_form.fields.enable_animation.label"
        ),
        name: "enable_animation",
      },
    ],
    defaultConfig: {
      enable_animation: true,
    },
    defaultSize: {
      w: 3,
      h: 3,
      minH: 2,
      minW: 2,
    },
    permissions: [],
  },
  {
    id: "core:comment:pending",
    component: markRaw(PendingCommentsWidget),
    group: "core.dashboard.widgets.groups.comment",
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 12,
      minH: 6,
      minW: 3,
    },
    permissions: ["system:comments:view"],
  },
  {
    id: "core:user:stats",
    component: markRaw(UserStatsWidget),
    group: "core.dashboard.widgets.groups.user",
    configFormKitSchema: () => [
      {
        $formkit: "checkbox",
        label: i18n.global.t(
          "core.dashboard.widgets.common_form.fields.enable_animation.label"
        ),
        name: "enable_animation",
      },
    ],
    defaultConfig: {
      enable_animation: true,
    },
    defaultSize: {
      w: 3,
      h: 3,
      minH: 2,
      minW: 2,
    },
  },
  {
    id: "core:view:stats",
    component: markRaw(ViewsStatsWidget),
    group: "core.dashboard.widgets.groups.other",
    configFormKitSchema: () => [
      {
        $formkit: "checkbox",
        label: i18n.global.t(
          "core.dashboard.widgets.common_form.fields.enable_animation.label"
        ),
        name: "enable_animation",
      },
    ],
    defaultConfig: {
      enable_animation: true,
    },
    defaultSize: {
      w: 3,
      h: 3,
      minH: 2,
      minW: 2,
    },
  },
  {
    id: "core:quick-action",
    component: markRaw(QuickActionWidget),
    group: "core.dashboard.widgets.groups.other",
    defaultConfig: {
      enabled_items: [
        "core:user-center",
        "core:theme-preview",
        "core:new-post",
        "core:new-page",
        "core:upload-attachment",
        "core:theme-manage",
        "core:plugin-manage",
        "core:new-user",
        "core:refresh-search-engine",
      ],
    },
    defaultSize: {
      w: 6,
      h: 12,
      minH: 6,
      minW: 3,
    },
  },
  {
    id: "core:notifications",
    component: markRaw(NotificationWidget),
    group: "core.dashboard.widgets.groups.other",
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 12,
      minH: 6,
      minW: 3,
    },
  },
  {
    id: "core:stack",
    component: markRaw(StackWidget),
    group: "core.dashboard.widgets.groups.other",
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 12,
      minH: 1,
      minW: 1,
    },
  },
  {
    id: "core:iframe",
    component: markRaw(IframeWidget),
    group: "core.dashboard.widgets.groups.other",
    configFormKitSchema: () => [
      {
        $formkit: "text",
        label: i18n.global.t(
          "core.dashboard.widgets.presets.iframe.config.fields.title.label"
        ),
        name: "title",
      },
      {
        $formkit: "url",
        label: "URL",
        name: "url",
        validation: "required|url",
      },
    ],
    defaultConfig: {},
    defaultSize: {
      w: 6,
      h: 12,
      minH: 2,
      minW: 2,
    },
  },
];
