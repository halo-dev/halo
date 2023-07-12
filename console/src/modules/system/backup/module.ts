import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import BackupList from "./BackupList.vue";
import { IconPlug } from "@halo-dev/components";
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
          component: BackupList,
          meta: {
            title: "备份",
            searchable: true,
            permissions: [],
            menu: {
              name: "备份",
              group: "system",
              icon: markRaw(IconPlug),
              priority: 4,
            },
          },
        },
      ],
    },
  ],
});
