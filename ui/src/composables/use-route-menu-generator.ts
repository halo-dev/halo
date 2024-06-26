import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";
import type { MenuGroupType, MenuItemType } from "@halo-dev/console-shared";
import { sortBy } from "lodash-es";
import { onMounted, ref, type Ref } from "vue";
import {
  useRouter,
  type RouteRecordNormalized,
  type RouteRecordRaw,
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

  function flattenRoutes(route: RouteRecordNormalized | RouteRecordRaw) {
    let routes: (RouteRecordNormalized | RouteRecordRaw)[] = [route];
    if (route.children) {
      route.children.forEach((child) => {
        routes = routes.concat(flattenRoutes(child));
      });
    }
    return routes;
  }

  function isRouteValid(route?: RouteRecordNormalized) {
    if (!route) return false;
    const { meta } = route;
    if (!meta?.menu) return false;
    return (
      !meta.permissions || hasPermission(uiPermissions, meta.permissions, true)
    );
  }

  const generateMenus = () => {
    // Filter and sort routes based on menu and permissions
    let currentRoutes = sortBy<RouteRecordNormalized>(
      router.getRoutes().filter((route) => isRouteValid(route)),
      [
        (route: RouteRecordRaw) => !route.meta?.core,
        (route: RouteRecordRaw) => route.meta?.menu?.priority || 0,
      ]
    );

    // Flatten and filter child routes
    currentRoutes.forEach((route) => {
      if (route.children.length) {
        const routesMap = new Map(
          currentRoutes.map((route) => [route.name, route])
        );

        const flattenedAndValidChildren = route.children
          .flatMap((child) => flattenRoutes(child))
          .map((flattenedChild) => {
            const validRoute = routesMap.get(flattenedChild.name);
            if (validRoute && isRouteValid(validRoute)) {
              return validRoute;
            }
          })
          .filter(Boolean); // filters out falsy values

        // Sorting the routes
        // @ts-ignore children must be RouteRecordRaw[], but it is RouteRecordNormalized[]
        route.children = sortBy(flattenedAndValidChildren, [
          (route) => !route?.meta?.core,
          (route) => route?.meta?.menu?.priority || 0,
        ]);
      }
    });

    // Remove duplicate routes
    const allChildren = currentRoutes.flatMap((route) => route.children);
    currentRoutes = currentRoutes.filter(
      (route) => !allChildren.find((child) => child.name === route.name)
    );

    // group by menu.group
    menus.value = currentRoutes.reduce((acc, route) => {
      const { menu } = route.meta;
      if (!menu) {
        return acc;
      }
      const group = acc.find((item) => item.id === menu.group);
      const childRoute = route.children;

      const menuChildren: MenuItemType[] = childRoute
        .map((child) => {
          if (!child.meta?.menu) return;
          return {
            name: child.meta.menu.name,
            path: child.path,
            icon: child.meta.menu.icon,
            mobile: child.meta.menu.mobile,
          };
        })
        .filter(Boolean) as MenuItemType[];

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
