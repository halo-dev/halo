import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import Backups from "./Backups.vue";
import { IconServerLine } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/backup",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Backup",
          component: Backups,
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
        },
      ],
    },
  ],
});
