import { getNode } from "@formkit/core";
import { defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, expect, it, vi } from "vite-plus/test";
import { defineComponent, h } from "vue";
import { createI18n } from "vue-i18n";
import CommentDetailModal from "../CommentDetailModal.vue";
import CommentEditor from "../CommentEditor.vue";
import ReplyCreationModal from "../ReplyCreationModal.vue";
import ReplyDetailModal from "../ReplyDetailModal.vue";

const api = vi.hoisted(() => ({ createReply: vi.fn() }));
vi.mock("@halo-dev/api-client", () => ({
  consoleApiClient: {
    content: { comment: { createReply: api.createReply } },
  },
  coreApiClient: {
    content: {
      comment: { patchComment: vi.fn() },
      reply: { patchReply: vi.fn() },
    },
  },
}));
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({ pluginModules: [] }),
}));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { date: { format: () => "", timeAgo: () => "" } },
}));
vi.mock("@uc/modules/profile/tabs/composables/use-user-agent", () => ({
  useUserAgent: () => ({ os: "", browser: "" }),
}));
vi.mock("../../composables/use-content-provider-extension-point", () => ({
  useContentProviderExtensionPoint: () => ({ data: undefined }),
}));
vi.mock("../../composables/use-subject-ref", () => ({
  useSubjectRef: () => ({ subjectRefResult: {} }),
}));

const ModalStub = defineComponent({
  emits: ["close"],
  setup(_, { slots, emit, expose }) {
    expose({ close: () => emit("close") });
    return () => h("div", [slots.default?.(), slots.footer?.()]);
  },
});

it.each([
  { label: "comment detail", component: CommentDetailModal },
  { label: "reply detail", component: ReplyDetailModal },
])(
  "submits a private reply while approving in $label",
  async ({ component }) => {
    const owner = {
      kind: "Email",
      name: "test@example.com",
      displayName: "Test",
    };
    const comment = {
      comment: {
        metadata: { name: "parent" },
        spec: { owner, approved: false, content: "Comment" },
      },
      owner,
    } as ListedComment;
    const reply = {
      reply: {
        metadata: { name: "quoted" },
        spec: { owner, approved: false, content: "Reply" },
      },
      owner,
    } as ListedReply;
    const client = new QueryClient();
    const wrapper = mount(component, {
      attachTo: document.body,
      props: component === ReplyDetailModal ? { comment, reply } : { comment },
      global: {
        plugins: [
          [VueQueryPlugin, { queryClient: client }],
          [formKitPlugin, defaultConfig()],
          createI18n({
            legacy: false,
            missingWarn: false,
            fallbackWarn: false,
          }),
        ],
        mocks: { $route: {} },
        components: {
          HasPermission: { template: "<div><slot /></div>" },
        },
        stubs: {
          Modal: ModalStub,
          Dropdown: { template: "<div><slot /></div>" },
          RouterLink: true,
          OwnerButton: true,
        },
        directives: { tooltip: () => {} },
      },
    });
    try {
      wrapper.getComponent(CommentEditor).vm.$emit("update", {
        content: "Private answer",
        characterCount: 14,
      });
      const checkbox = wrapper.get('input[type="checkbox"]');
      await checkbox.setValue(true);
      await getNode(checkbox.attributes("id"))?.settled;
      await new Promise((resolve) => setTimeout(resolve, 30));
      await wrapper
        .findAll("button")
        .find((button) =>
          button
            .text()
            .includes("core.comment.operations.reply_and_approve.button")
        )!
        .trigger("click");
      await flushPromises();
      expect(api.createReply).toHaveBeenCalledWith({
        name: "parent",
        replyRequest: {
          raw: "Private answer",
          content: "Private answer",
          allowNotification: true,
          hidden: true,
          quoteReply: component === ReplyDetailModal ? "quoted" : undefined,
        },
      });
    } finally {
      wrapper.unmount();
      client.clear();
    }
  }
);

beforeEach(() => {
  vi.resetAllMocks();
  api.createReply.mockResolvedValue({});
});

it.each([false, true])(
  "submits hidden=%s for a console reply",
  async (hidden) => {
    const comment = {
      comment: { metadata: { name: "parent" } },
    } as ListedComment;
    const reply = {
      reply: { metadata: { name: "quoted" } },
    } as ListedReply;
    const client = new QueryClient();
    const wrapper = mount(ReplyCreationModal, {
      attachTo: document.body,
      props: { comment, reply },
      global: {
        plugins: [
          [VueQueryPlugin, { queryClient: client }],
          [formKitPlugin, defaultConfig()],
          createI18n({
            legacy: false,
            missingWarn: false,
            fallbackWarn: false,
          }),
        ],
        stubs: {
          Modal: ModalStub,
          Dropdown: { template: "<div><slot /></div>" },
        },
      },
    });
    try {
      wrapper.getComponent(CommentEditor).vm.$emit("update", {
        content: "Reply body",
        characterCount: 10,
      });
      if (hidden) {
        const checkbox = wrapper.get('input[type="checkbox"]');
        await checkbox.setValue(true);
        await getNode(checkbox.attributes("id"))?.settled;
        await new Promise((resolve) => setTimeout(resolve, 30));
        expect((checkbox.element as HTMLInputElement).checked).toBe(true);
      }
      await flushPromises();
      wrapper.getComponent({ name: "SubmitButton" }).vm.$emit("submit");
      await flushPromises();
      expect(api.createReply).toHaveBeenCalledWith({
        name: "parent",
        replyRequest: {
          raw: "Reply body",
          content: "Reply body",
          allowNotification: true,
          hidden,
          quoteReply: "quoted",
        },
      });
    } finally {
      wrapper.unmount();
      client.clear();
    }
  }
);
