import type { App } from "vue";

import PostStatsWidget from "./PostStatsWidget.vue";
import UserStatsWidget from "./UserStatsWidget.vue";
import CommentStatsWidget from "./CommentStatsWidget.vue";
import ViewsStatsWidget from "./ViewsStatsWidget.vue";
import RecentLoginWidget from "./RecentLoginWidget.vue";
import RecentPublishedWidget from "./RecentPublishedWidget.vue";
import JournalPublishWidget from "./JournalPublishWidget.vue";
import QuickLinkWidget from "./QuickLinkWidget.vue";

const install = (app: App) => {
  app.component("PostStatsWidget", PostStatsWidget);
  app.component("UserStatsWidget", UserStatsWidget);
  app.component("CommentStatsWidget", CommentStatsWidget);
  app.component("ViewsStatsWidget", ViewsStatsWidget);
  app.component("RecentLoginWidget", RecentLoginWidget);
  app.component("RecentPublishedWidget", RecentPublishedWidget);
  app.component("JournalPublishWidget", JournalPublishWidget);
  app.component("QuickLinkWidget", QuickLinkWidget);
};

export default install;
