import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { mount } from "@vue/test-utils";
import { expect, it, vi } from "vite-plus/test";
import { createI18n } from "vue-i18n";
import HasPermission from "@/components/permission/HasPermission.vue";
import CommentDetailModal from "../CommentDetailModal.vue";
import ReplyDetailModal from "../ReplyDetailModal.vue";

const permissions = vi.hoisted(() => ({ allowed: true }));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: {
    permission: { has: () => permissions.allowed },
    date: { format: () => "", timeAgo: () => "" },
  },
}));
vi.mock("@tanstack/vue-query", () => ({
  useQueryClient: () => ({ invalidateQueries: vi.fn() }),
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

it.each([
  {
    allowed: false,
    commentDeleted: false,
    replyDeleted: false,
    commentActions: 0,
    replyActions: 0,
  },
  {
    allowed: true,
    commentDeleted: false,
    replyDeleted: false,
    commentActions: 1,
    replyActions: 2,
  },
  {
    allowed: true,
    commentDeleted: true,
    replyDeleted: false,
    commentActions: 0,
    replyActions: 1,
  },
  {
    allowed: true,
    commentDeleted: false,
    replyDeleted: true,
    commentActions: 1,
    replyActions: 1,
  },
  {
    allowed: true,
    commentDeleted: true,
    replyDeleted: true,
    commentActions: 0,
    replyActions: 0,
  },
])(
  "gates detail edit actions: %j",
  ({ allowed, commentDeleted, replyDeleted, commentActions, replyActions }) => {
    permissions.allowed = allowed;
    const owner = {
      kind: "Email",
      name: "test@example.com",
      displayName: "Test",
    };
    const comment = {
      comment: {
        metadata: {
          name: "c",
          deletionTimestamp: commentDeleted
            ? "2026-09-09T00:00:00Z"
            : undefined,
        },
        spec: { owner, approved: true, content: "Body" },
      },
      owner,
    } as ListedComment;
    const reply = {
      reply: {
        metadata: {
          name: "r",
          deletionTimestamp: replyDeleted ? "2026-09-09T00:00:00Z" : undefined,
        },
        spec: { owner, approved: true, content: "Reply", commentName: "c" },
      },
      owner,
    } as ListedReply;
    for (const [component, expected] of [
      [CommentDetailModal, commentActions],
      [ReplyDetailModal, replyActions],
    ] as const) {
      const wrapper = mount(component, {
        shallow: true,
        props:
          component === ReplyDetailModal ? { comment, reply } : { comment },
        global: {
          plugins: [
            createI18n({
              legacy: false,
              missingWarn: false,
              fallbackWarn: false,
            }),
          ],
          components: { HasPermission },
          mocks: { $route: {} },
          stubs: { HasPermission: false, RouterLink: true },
          renderStubDefaultSlot: true,
          directives: { tooltip: () => {} },
        },
      });
      try {
        expect(
          wrapper
            .findAll("button-stub")
            .filter((button) => button.text() === "core.common.buttons.edit")
        ).toHaveLength(expected);
      } finally {
        wrapper.unmount();
      }
    }
  }
);
