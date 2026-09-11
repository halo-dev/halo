import type { EntityFieldItem } from "@halo-dev/ui-shared";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { expect, it, vi } from "vite-plus/test";
import {
  computed,
  defineAsyncComponent,
  defineComponent,
  h,
  reactive,
  ref,
} from "vue";
import EntityFieldItems from "@/components/entity-fields/EntityFieldItems.vue";
import { useEntityFieldItemExtensionPoint } from "../use-entity-extension-points";

const extension = vi.hoisted(() => vi.fn());
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({
    pluginModules: [{ extensionPoints: { fields: extension } }],
  }),
}));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => true } },
}));

it("renders async plugin fields without making their components reactive", async () => {
  const state = reactive({ label: "Plugin field", hidden: false });
  extension.mockImplementation(() => [
    {
      position: "end",
      priority: 10,
      component: defineAsyncComponent(async () => ({
        props: { label: String },
        template: "<span>{{ label }}</span>",
      })),
      props: state,
      get hidden() {
        return state.hidden;
      },
    },
  ]);
  const warn = vi.spyOn(console, "warn").mockImplementation(() => {});
  const client = new QueryClient();
  const wrapper = mount(
    defineComponent({
      setup() {
        const { data } = useEntityFieldItemExtensionPoint(
          "fields",
          ref({ name: "test-plugin" }),
          computed((): EntityFieldItem[] => [])
        );
        return () => h(EntityFieldItems, { fields: data.value?.end || [] });
      },
    }),
    { global: { plugins: [[VueQueryPlugin, { queryClient: client }]] } }
  );
  try {
    await flushPromises();
    await flushPromises();
    expect(wrapper.text()).toBe("Plugin field");
    await client.invalidateQueries();
    await flushPromises();
    await flushPromises();
    expect(wrapper.text()).toBe("Plugin field");
    state.label = "Updated field";
    await flushPromises();
    expect(wrapper.text()).toBe("Updated field");
    state.hidden = true;
    await flushPromises();
    expect(wrapper.text()).toBe("");
    state.hidden = false;
    await flushPromises();
    expect(wrapper.text()).toBe("Updated field");
    expect(warn.mock.calls.flat().join(" ")).not.toContain(
      "Vue received a Component that was made a reactive object"
    );
  } finally {
    wrapper.unmount();
    client.clear();
    warn.mockRestore();
  }
});
