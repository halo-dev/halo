import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";
import type { MenuGroupType, MenuItemType } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { sortBy } from "lodash-es";
import { ref, watch } from "vue";
import {
  useRouter,
  type RouteRecordNormalized,
  type RouteRecordRaw,
} from "vue-router";

export function useRouteMenuGenerator(menuGroups: MenuGroupType[]) {
  const router = useRouter();

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

  async function isRouteValid(route?: RouteRecordNormalized) {
    if (!route) return false;
    const { meta } = route;
    if (!meta?.menu) return false;

    // If permissions doesn't exist or is empty
    if (!meta.permissions) return true;

    // Check if permissions is a function
    if (typeof meta.permissions === "function") {
      try {
        return await meta.permissions(uiPermissions);
      } catch (e) {
        console.error(
          `Error checking permissions for route ${String(route.name)}:`,
          e
        );
        return false;
      }
    }

    // Default behavior for array of permissions
    return hasPermission(uiPermissions, meta.permissions as string[], true);
  }

  const { data, isLoading: isDataLoading } = useQuery({
    queryKey: ["core:sidebar:menus"],
    queryFn: async () => {
      const allRoutes = router.getRoutes();

      // Filter routes based on permissions (async)
      const validRoutePromises = allRoutes.map(async (route) => {
        const isValid = await isRouteValid(route);
        return isValid ? route : null;
      });

      // Wait for all permission checks to complete
      const validRoutes = (await Promise.all(validRoutePromises)).filter(
        Boolean
      ) as RouteRecordNormalized[];

      // Sort the valid routes
      let currentRoutes = sortBy<RouteRecordNormalized>(validRoutes, [
        (route: RouteRecordRaw) => !route.meta?.core,
        (route: RouteRecordRaw) => route.meta?.menu?.priority || 0,
      ]);

      // Flatten and filter child routes
      for (const route of currentRoutes) {
        if (route.children.length) {
          const routesMap = new Map(
            currentRoutes.map((route) => [route.name, route])
          );

          const childRoutesPromises = route.children
            .flatMap((child) => flattenRoutes(child))
            .map(async (flattenedChild) => {
              const validRoute = routesMap.get(flattenedChild.name);
              if (validRoute && (await isRouteValid(validRoute))) {
                return validRoute;
              }
              return null;
            });

          // Wait for all child permission checks to complete
          const flattenedAndValidChildren = (
            await Promise.all(childRoutesPromises)
          ).filter(Boolean) as RouteRecordNormalized[]; // filters out falsy values

          // Sorting the routes
          // @ts-ignore children must be RouteRecordRaw[], but it is RouteRecordNormalized[]
          route.children = sortBy(flattenedAndValidChildren, [
            (route) => !route?.meta?.core,
            (route) => route?.meta?.menu?.priority || 0,
          ]);
        }
      }

      // Remove duplicate routes
      const allChildren = currentRoutes.flatMap((route) => route.children);

      currentRoutes = currentRoutes.filter(
        (route) => !allChildren.find((child) => child.name === route.name)
      );

      // group by menu.group
      const groupedMenus = currentRoutes.reduce((acc, route) => {
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
      const menus = sortBy(groupedMenus, [
        (menu: MenuGroupType) => {
          return menuGroups.findIndex((item) => item.id === menu.id) < 0;
        },
        (menu: MenuGroupType) => menu.priority || 0,
      ]);

      const minimenus = menus
        .reduce((acc, group) => {
          if (group?.items) {
            acc.push(...group.items);
          }
          return acc;
        }, [] as MenuItemType[])
        .filter((item) => item.mobile);

      return {
        menus,
        minimenus,
      };
    },
  });

  const isLoading = ref(false);

  // Make loading more user-friendly
  watch(
    () => isDataLoading.value,
    (value) => {
      let delayLoadingTimer: ReturnType<typeof setTimeout> | undefined;
      if (value) {
        delayLoadingTimer = setTimeout(() => {
          isLoading.value = isDataLoading.value;
        }, 200);
      } else {
        clearTimeout(delayLoadingTimer);
        isLoading.value = false;
      }
    },
    {
      immediate: true,
    }
  );

  return {
    data,
    isLoading,
  };
}
