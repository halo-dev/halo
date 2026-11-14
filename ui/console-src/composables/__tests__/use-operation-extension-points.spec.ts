import type { OperationItem } from "@halo-dev/ui-shared";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { expect, it, vi } from "vite-plus/test";
import { computed, defineComponent, h, ref } from "vue";
import { useOperationItemExtensionPoint } from "../use-operation-extension-points";

const extension = vi.hoisted(() => vi.fn());
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({
    pluginModules: [{ extensionPoints: { operations: extension } }],
  }),
}));

it("updates preset visibility without rerunning or losing plugin operations", async () => {
  extension.mockReturnValue([{ priority: 20, label: "Plugin" }]);
  const activeTheme = ref<string>();
  const client = new QueryClient();
  const wrapper = mount(
    defineComponent({
      setup() {
        const { data } = useOperationItemExtensionPoint(
          "operations",
          ref({ name: "earth" }),
          computed((): OperationItem<{ name: string }>[] => [
            {
              priority: 10,
              label: "Activate",
              hidden: !activeTheme.value || activeTheme.value === "earth",
            },
          ])
        );
        return () =>
          h(
            "div",
            data.value
              ?.filter((item) => !item.hidden)
              .map((item) => h("button", item.label))
          );
      },
    }),
    { global: { plugins: [[VueQueryPlugin, { queryClient: client }]] } }
  );
  try {
    await flushPromises();
    expect(wrapper.text()).toBe("Plugin");
    activeTheme.value = "other";
    await flushPromises();
    expect(wrapper.findAll("button").map((button) => button.text())).toEqual([
      "Activate",
      "Plugin",
    ]);
    activeTheme.value = "earth";
    await flushPromises();
    expect(wrapper.text()).toBe("Plugin");
    expect(extension).toHaveBeenCalledOnce();
  } finally {
    wrapper.unmount();
    client.clear();
  }
});
