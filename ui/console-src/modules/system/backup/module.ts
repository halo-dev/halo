import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconServerLine } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import Backups from "./Backups.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/backup",
      name: "BackupRoot",
      component: BasicLayout,
      meta: {
        title: "core.backup.title",
        searchable: true,
        permissions: ["system:migrations:manage"],
        menu: {
          name: "core.sidebar.menu.items.backup",
          group: "system",
          icon: markRaw(IconServerLine),
          priority: 4,
        },
      },
      children: [
        {
          path: "",
          name: "Backup",
          component: Backups,
        },
      ],
    },
  ],
});
