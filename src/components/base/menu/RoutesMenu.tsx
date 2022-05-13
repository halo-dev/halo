import { computed, defineComponent } from "vue";
import type { PropType, Component } from "vue";
import type { MenuGroupType, MenuItemType } from "@/router/menus.config";
import { VMenu, VMenuItem, VMenuLabel } from "@/components/base/menu/index";
import { useRoute, useRouter } from "vue-router";
import type { RouteLocationMatched } from "vue-router";

const VRoutesMenu = defineComponent({
  name: "VRoutesMenu",
  props: {
    menus: {
      type: Object as PropType<MenuGroupType[]>,
    },
  },
  setup(props) {
    const route = useRoute();
    const { push } = useRouter();

    const openIds = computed(() => {
      return route.matched.map((item: RouteLocationMatched) => item.path);
    });

    async function handleSelect(id: string) {
      await push(id);
    }

    function renderIcon(icon: Component | undefined) {
      if (!icon) return undefined;

      return <icon height="20px" width="20px" />;
    }

    function renderItems(items: MenuItemType[] | undefined) {
      return items?.map((item) => {
        return (
          <>
            {item.children?.length ? (
              <VMenuItem
                key={item.path}
                id={item.path}
                title={item.name}
                v-slots={{
                  icon: () => renderIcon(item.icon),
                }}
              >
                {renderItems(item.children)}
              </VMenuItem>
            ) : (
              <VMenuItem
                key={item.path}
                id={item.path}
                title={item.name}
                v-slots={{
                  icon: () => renderIcon(item.icon),
                }}
                onSelect={handleSelect}
                active={openIds.value.includes(item.path)}
              />
            )}
          </>
        );
      });
    }

    return () => (
      <VMenu openIds={openIds.value}>
        {props.menus?.map((menu: MenuGroupType) => {
          return (
            <>
              {menu.name && <VMenuLabel>{menu.name}</VMenuLabel>}
              {menu.items?.length && renderItems(menu.items)}
            </>
          );
        })}
      </VMenu>
    );
  },
});

export { VRoutesMenu };
