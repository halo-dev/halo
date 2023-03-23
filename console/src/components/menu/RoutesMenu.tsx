import type { Component, PropType } from "vue";
import { computed, defineComponent } from "vue";
import type { MenuGroupType, MenuItemType } from "@halo-dev/console-shared";
import { VMenu, VMenuItem, VMenuLabel } from "@halo-dev/components";
import type { RouteLocationMatched } from "vue-router";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

const RoutesMenu = defineComponent({
  name: "RoutesMenu",
  props: {
    menus: {
      type: Object as PropType<MenuGroupType[]>,
      required: true,
    },
  },
  emits: ["select"],
  setup(props, { emit }) {
    const route = useRoute();
    const { push } = useRouter();

    const openIds = computed(() => {
      return route.matched.map((item: RouteLocationMatched) => item.path);
    });

    async function handleSelect(id: string) {
      emit("select", id);
      await push(id);
    }

    function renderIcon(icon: Component | undefined) {
      if (!icon) return undefined;

      return <icon height="20px" width="20px" />;
    }

    const { t } = useI18n();

    function renderItems(items: MenuItemType[] | undefined) {
      return items?.map((item) => {
        return (
          <>
            {item.children?.length ? (
              <VMenuItem
                key={item.path}
                id={item.path}
                title={t(item.name, item.name)}
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
                title={t(item.name, item.name)}
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
              {menu.name && <VMenuLabel>{t(menu.name, menu.name)}</VMenuLabel>}
              {menu.items?.length && renderItems(menu.items)}
            </>
          );
        })}
      </VMenu>
    );
  },
});

export { RoutesMenu };
