import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";
import type { EntityFieldItem } from "@halo-dev/ui-shared";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { createPinia } from "pinia";
import { expect, it, vi } from "vite-plus/test";
import { defineComponent, h, isRef, markRaw, ref, type Ref } from "vue";
import { createI18n } from "vue-i18n";
import HasPermission from "@/components/permission/HasPermission.vue";
import { usePluginModuleStore } from "@/stores/plugin";
import CommentListItem from "../CommentListItem.vue";
import ReplyListItem from "../ReplyListItem.vue";

vi.mock("@halo-dev/ui-shared", () => ({
  utils: {
    permission: {
      has: (permissions: string[]) => !permissions.includes("plugin:denied"),
    },
    date: { format: () => "Full date", timeAgo: () => "Just now" },
  },
}));
vi.mock("../../composables/use-subject-ref", () => ({
  useSubjectRef: () => ({ subjectRefResult: { title: "Test post" } }),
}));

const owner = {
  kind: "Email",
  name: "test@example.com",
  displayName: "Test owner",
};
const comment = {
  comment: {
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Comment",
    metadata: { name: "comment-fields" },
    spec: { owner, approved: false, hidden: true, content: "Comment body" },
  },
  owner,
} as ListedComment;
const reply = {
  reply: {
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Reply",
    metadata: { name: "reply-fields" },
    spec: {
      owner,
      approved: false,
      content: "Reply body",
      commentName: "comment-fields",
    },
  },
  owner,
} as ListedReply;

const cases = [
  { kind: "comment", component: CommentListItem, entity: comment },
  { kind: "reply", component: ReplyListItem, entity: reply },
] as const;

it.each(cases)(
  "renders $kind plugin fields with ordering, visibility and fresh entity data",
  async ({ kind, component, entity }) => {
    const pinia = createPinia();
    const client = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    const Field = markRaw(
      defineComponent({
        props: { label: { type: String, required: true } },
        setup: (props) => () =>
          h(VEntityField, {
            description: props.label,
            "data-plugin-field": "",
          }),
      })
    );
    const field = (
      label: string,
      position: "start" | "end",
      priority: number,
      extra: Partial<EntityFieldItem> = {}
    ): EntityFieldItem => ({
      component: Field,
      props: { label },
      position,
      priority,
      ...extra,
    });
    const extension = vi.fn((value: Ref<ListedComment | ListedReply>) => {
      expect(isRef(value)).toBe(true);
      const content =
        "comment" in value.value
          ? value.value.comment.spec.content
          : value.value.reply.spec.content;
      return [
        field("End later", "end", 20),
        field("Start later", "start", 20),
        field("Hidden", "start", 0, { hidden: true }),
        field("Forbidden", "end", 0, { permissions: ["plugin:denied"] }),
        field(content, "end", 10),
        field("Start first", "start", 10),
      ];
    });
    usePluginModuleStore(pinia).registerPluginModule("test-fields", {
      extensionPoints: { [`${kind}:list-item:field:create`]: extension },
    });
    const wrapper = mount(component, {
      props: kind === "comment" ? { comment } : { comment, reply },
      global: {
        plugins: [
          pinia,
          [VueQueryPlugin, { queryClient: client }],
          createI18n({
            legacy: false,
            missingWarn: false,
            fallbackWarn: false,
          }),
        ],
        provide: { hoveredReply: ref() },
        components: { HasPermission },
        mocks: { $route: {} },
        stubs: {
          RouterLink: { template: "<a><slot /></a>" },
          OwnerButton: true,
          CommentDetailModal: true,
          ReplyDetailModal: true,
          CommentEditingModal: true,
          ReplyCreationModal: true,
          CollapsibleCommentContent: {
            props: ["content"],
            template: "<div data-content>{{ content }}</div>",
          },
        },
        directives: { tooltip: () => {} },
      },
    });
    try {
      await flushPromises();
      await flushPromises();
      const labels = (position: string) =>
        wrapper
          .findAll(`.entity-${position} [data-plugin-field]`)
          .map((item) => item.text());
      expect(extension).toHaveBeenCalled();
      expect(extension.mock.calls[0][0].value).toEqual(entity);
      expect(labels("start")).toEqual(["Start first", "Start later"]);
      expect(labels("end")).toEqual([
        kind === "comment" ? "Comment body" : "Reply body",
        "End later",
      ]);
      expect(wrapper.get("[data-content]").text()).toBe(
        kind === "comment" ? "Comment body" : "Reply body"
      );
      expect(wrapper.text()).toContain(
        "core.comment.list.fields.pending_review"
      );
      expect(wrapper.text()).toContain("core.comment.list.fields.private");
      expect(wrapper.text()).toContain("Just now");
      const updated =
        kind === "comment"
          ? {
              ...comment,
              comment: {
                ...comment.comment,
                spec: {
                  ...comment.comment.spec,
                  content: "Updated body",
                  approved: true,
                },
              },
            }
          : {
              ...reply,
              reply: {
                ...reply.reply,
                spec: {
                  ...reply.reply.spec,
                  content: "Updated body",
                  approved: true,
                },
              },
            };
      await wrapper.setProps({ [kind]: updated });
      await flushPromises();
      await flushPromises();
      expect(labels("end")).toEqual(["Updated body", "End later"]);
      expect(wrapper.get("[data-content]").text()).toBe("Updated body");
      expect(wrapper.text()).not.toContain(
        "core.comment.list.fields.pending_review"
      );
      extension.mockReturnValue([]);
      await client.invalidateQueries({
        queryKey: ["core:extension-points:list-item:fields"],
      });
      await flushPromises();
      expect(wrapper.findAll("[data-plugin-field]")).toHaveLength(0);
      expect(wrapper.get("[data-content]").text()).toBe("Updated body");
      expect(wrapper.text()).toContain("Just now");
    } finally {
      wrapper.unmount();
      client.clear();
    }
  }
);
