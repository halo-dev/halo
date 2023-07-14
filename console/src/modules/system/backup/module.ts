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
            title: "备份",
            searchable: true,
            permissions: [],
            menu: {
              name: "备份",
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
