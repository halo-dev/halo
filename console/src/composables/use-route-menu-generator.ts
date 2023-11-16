import { useRoleStore } from "@/stores/role";
import type { MenuGroupType, MenuItemType } from "@halo-dev/console-shared";
import { onMounted, ref, type Ref } from "vue";
import sortBy from "lodash.sortby";
import { hasPermission } from "@/utils/permission";
import {
  useRouter,
  type RouteRecordRaw,
  type RouteRecordNormalized,
} from "vue-router";

interface useRouteMenuGeneratorReturn {
  menus: Ref<MenuGroupType[]>;
  minimenus: Ref<MenuItemType[]>;
}

export function useRouteMenuGenerator(
  menuGroups: MenuGroupType[]
): useRouteMenuGeneratorReturn {
  const router = useRouter();

  const menus = ref<MenuGroupType[]>([] as MenuGroupType[]);
  const minimenus = ref<MenuItemType[]>([] as MenuItemType[]);

  const roleStore = useRoleStore();
  const { uiPermissions } = roleStore.permissions;

  const generateMenus = () => {
    // sort by menu.priority and meta.core
    const currentRoutes = sortBy<RouteRecordNormalized>(
      router.getRoutes().filter((route) => {
        const { meta } = route;
        if (!meta?.menu) {
          return false;
        }
        if (meta.permissions) {
          return hasPermission(
            uiPermissions,
            meta.permissions as string[],
            true
          );
        }
        return true;
      }),
      [
        (route: RouteRecordRaw) => !route.meta?.core,
        (route: RouteRecordRaw) => route.meta?.menu?.priority || 0,
      ]
    );

    // group by menu.group
    menus.value = currentRoutes.reduce((acc, route) => {
      const { menu } = route.meta;
      if (!menu) {
        return acc;
      }
      const group = acc.find((item) => item.id === menu.group);
      const childRoute = route.children[0];
      const childMetaMenu = childRoute?.meta?.menu;

      // only support one level
      const menuChildren = childMetaMenu
        ? [
            {
              name: childMetaMenu.name,
              path: childRoute.path,
              icon: childMetaMenu.icon,
            },
          ]
        : undefined;
      if (group) {
        group.items?.push({
          name: menu.name,
          path: route.path,
          icon: menu.icon,
          mobile: menu.mobile,
          children: menuChildren,
        });
      } else {
        const menuGroup = menuGroups.find((item) => item.id === menu.group);
        let name = "";
        if (!menuGroup) {
          name = menu.group || "";
        } else if (menuGroup.name) {
          name = menuGroup.name;
        }
        acc.push({
          id: menuGroup?.id || menu.group || "",
          name: name,
          priority: menuGroup?.priority || 0,
          items: [
            {
              name: menu.name,
              path: route.path,
              icon: menu.icon,
              mobile: menu.mobile,
              children: menuChildren,
            },
          ],
        });
      }
      return acc;
    }, [] as MenuGroupType[]);

    // sort by menu.priority
    menus.value = sortBy(menus.value, [
      (menu: MenuGroupType) => {
        return menuGroups.findIndex((item) => item.id === menu.id) < 0;
      },
      (menu: MenuGroupType) => menu.priority || 0,
    ]);

    minimenus.value = menus.value
      .reduce((acc, group) => {
        if (group?.items) {
          acc.push(...group.items);
        }
        return acc;
      }, [] as MenuItemType[])
      .filter((item) => item.mobile);
  };

  onMounted(generateMenus);

  return {
    menus,
    minimenus,
  };
}
