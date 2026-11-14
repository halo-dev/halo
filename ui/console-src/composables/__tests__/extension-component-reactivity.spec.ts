import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { expect, it, vi } from "vite-plus/test";
import { computed, defineAsyncComponent, h, reactive, ref } from "vue";
import { useRouteMenuGenerator } from "@/composables/use-route-menu-generator";
import CommentEditor from "../../modules/contents/comments/components/CommentEditor.vue";
import { useContentProviderExtensionPoint } from "../../modules/contents/comments/composables/use-content-provider-extension-point";
import { useOperationItemExtensionPoint } from "../use-operation-extension-points";

const mocks = vi.hoisted(() => ({ extension: vi.fn(), routes: vi.fn() }));
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({
    pluginModules: [
      {
        extensionPoints: {
          operations: mocks.extension,
          "comment:list-item:content:replace": mocks.extension,
          "comment:editor:replace": mocks.extension,
        },
      },
    ],
  }),
}));
vi.mock("vue-router", () => ({
  useRouter: () => ({ getRoutes: mocks.routes }),
}));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => true } },
}));
vi.mock("@halo-dev/components", () => ({ VLoading: { template: "<span />" } }));
vi.mock(
  "../../modules/contents/comments/components/DefaultCommentContent.vue",
  () => ({ default: {} })
);
vi.mock(
  "../../modules/contents/comments/components/DefaultCommentEditor.vue",
  () => ({ default: {} })
);

it.each([
  "operation",
  "child operation",
  "comment content",
  "comment editor",
  "menu icon",
  "child menu icon",
])(
  "keeps %s props and local state reactive without proxying components",
  async (kind) => {
    const component = defineAsyncComponent(async () => ({
      props: { label: String, initialContent: String },
      setup() {
        return { count: ref(0) };
      },
      template:
        '<button @click="count++">{{ label || initialContent }}:{{ count }}</button>',
    }));
    const state = reactive({ label: "before", hidden: false });
    const operation = {
      priority: 1,
      component,
      props: state,
      get hidden() {
        return state.hidden;
      },
    };
    mocks.extension.mockReturnValue(
      kind.includes("operation")
        ? [{ ...operation, children: [operation] }]
        : { component, supportsEditing: true }
    );
    // Preserve the top-level getter as well as the child's getter.
    if (kind === "operation") mocks.extension.mockReturnValue([operation]);
    const child = {
      name: "child",
      path: "/test/child",
      children: [],
      meta: {
        menu: { name: "child", group: "test", icon: component, mobile: true },
      },
    };
    mocks.routes.mockReturnValue([
      {
        name: "test",
        path: "/test",
        children: [child],
        meta: {
          menu: { name: "test", group: "test", icon: component, mobile: true },
        },
      },
      child,
      {
        name: "no-icon",
        path: "/no-icon",
        children: [],
        meta: { menu: { name: "no-icon", group: "test" } },
      },
    ]);
    const warn = vi.spyOn(console, "warn").mockImplementation(() => {});
    const client = new QueryClient();
    const wrapper = mount(
      {
        setup() {
          if (kind === "comment editor")
            return () => h(CommentEditor, { initialContent: state.label });
          if (kind.includes("menu icon")) {
            const { data } = useRouteMenuGenerator([]);
            return () => {
              const menu = data.value?.minimenus[0];
              const icon =
                kind === "menu icon" ? menu?.icon : menu?.children?.[0].icon;
              return icon ? h(icon, { label: state.label }) : null;
            };
          }
          if (kind === "comment content") {
            const { data } = useContentProviderExtensionPoint();
            return () =>
              data.value
                ? h(data.value.component, { label: state.label })
                : null;
          }
          const { data } = useOperationItemExtensionPoint(
            "operations",
            ref({ name: "test" }),
            computed(() => [])
          );
          return () => {
            const item =
              kind === "operation"
                ? data.value?.[0]
                : data.value?.[0].children?.[0];
            return item?.component && !item.hidden
              ? h(item.component, item.props)
              : null;
          };
        },
      },
      { global: { plugins: [[VueQueryPlugin, { queryClient: client }]] } }
    );
    try {
      await flushPromises();
      await flushPromises();
      expect(wrapper.text()).toBe("before:0");
      state.label = "after";
      await flushPromises();
      expect(wrapper.text()).toBe("after:0");
      await wrapper.get("button").trigger("click");
      expect(wrapper.text()).toBe("after:1");
      if (kind.includes("operation")) {
        state.hidden = true;
        await flushPromises();
        expect(wrapper.find("button").exists()).toBe(false);
        state.hidden = false;
        await flushPromises();
        expect(wrapper.text()).toBe("after:0");
      }
      if (!kind.includes("menu icon")) {
        const replacement = {
          component: defineAsyncComponent(async () => ({
            template: "<span>replaced</span>",
          })),
          priority: 1,
          supportsEditing: true,
        };
        mocks.extension.mockReturnValue(
          kind.includes("operation")
            ? [{ ...replacement, children: [replacement] }]
            : replacement
        );
        await client.invalidateQueries();
        await flushPromises();
        await flushPromises();
        expect(wrapper.text()).toBe("replaced");
      }
      expect(warn.mock.calls.flat().join(" ")).not.toContain(
        "Vue received a Component that was made a reactive object"
      );
    } finally {
      wrapper.unmount();
      client.clear();
      warn.mockRestore();
    }
  }
);
