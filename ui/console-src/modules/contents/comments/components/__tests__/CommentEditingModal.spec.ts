import { getNode } from "@formkit/core";
import { defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { defineComponent, h } from "vue";
import { createI18n } from "vue-i18n";
import SubmitButton from "@/components/button/SubmitButton.vue";
import CommentEditingModal from "../CommentEditingModal.vue";

const api = vi.hoisted(() => ({
  getComment: vi.fn(),
  getReply: vi.fn(),
  comment: vi.fn(),
  reply: vi.fn(),
  allowed: true,
}));
vi.mock("@halo-dev/api-client", () => ({
  coreApiClient: {
    content: {
      comment: { getComment: api.getComment },
      reply: { getReply: api.getReply },
    },
  },
  consoleApiClient: {
    content: {
      comment: { updateCommentContent: api.comment },
      reply: { updateReplyContent: api.reply },
    },
  },
}));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => api.allowed } },
}));
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({ pluginModules: [] }),
}));

const ModalStub = defineComponent({
  emits: ["close"],
  setup(_, { slots, emit, expose }) {
    expose({ close: () => emit("close") });
    return () => h("div", [slots.default?.(), slots.footer?.()]);
  },
});
const cleanups: (() => void)[] = [];
afterEach(() => cleanups.splice(0).forEach((cleanup) => cleanup()));
beforeEach(() => {
  vi.resetAllMocks();
  api.allowed = true;
  api.getComment.mockResolvedValue({ data: comment().comment });
  api.getReply.mockResolvedValue({ data: reply().reply });
  api.comment.mockResolvedValue({});
  api.reply.mockResolvedValue({});
});

function comment(): ListedComment {
  return {
    comment: {
      metadata: { name: "parent", version: 12 },
      spec: {
        raw: "Original",
        content: "<p>Original rendering</p>",
        approved: false,
      },
    },
  } as ListedComment;
}
function reply(): ListedReply {
  return {
    reply: {
      metadata: { name: "reply", version: 20 },
      spec: {
        raw: "Original reply",
        content: "<p>Original reply</p>",
        commentName: "parent",
        approved: true,
      },
    },
  } as ListedReply;
}
async function setup(editReply = false) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const invalidate = vi.spyOn(client, "invalidateQueries");
  const wrapper = mount(CommentEditingModal, {
    props: { target: editReply ? reply().reply : comment().comment },
    global: {
      plugins: [
        [VueQueryPlugin, { queryClient: client }],
        [formKitPlugin, defaultConfig()],
        createI18n({ legacy: false, missingWarn: false, fallbackWarn: false }),
      ],
      stubs: {
        Modal: ModalStub,
        Button: { template: "<button><slot /></button>" },
        Dropdown: { template: "<div><slot /></div>" },
      },
    },
  });
  cleanups.push(() => {
    wrapper.unmount();
    client.clear();
  });
  await flushPromises();
  const submit = async () => {
    // SubmitButton also emits from keyboard shortcuts, even when visually disabled.
    wrapper.getComponent(SubmitButton).vm.$emit("submit");
    await flushPromises();
  };
  const edit = async (value: string) => {
    await wrapper.get("textarea").setValue(value);
    await getNode("content-input")?.settled;
    await flushPromises();
  };
  return { wrapper, invalidate, submit, edit };
}

describe("comment editing", () => {
  it.each([false, true])(
    "loads the latest body and version on open (reply=%s)",
    async (isReply) => {
      const latest = isReply ? reply().reply : comment().comment;
      latest.metadata.version = 42;
      latest.spec.raw = "Latest body";
      const get = isReply ? api.getReply : api.getComment;
      get.mockResolvedValue({ data: latest });
      const { wrapper, edit, submit } = await setup(isReply);
      expect(get).toHaveBeenCalledExactlyOnceWith({
        name: latest.metadata.name,
      });
      expect(wrapper.get("textarea").element.value).toBe("Latest body");
      await edit("Edited latest body");
      await submit();
      expect(isReply ? api.reply : api.comment).toHaveBeenCalledWith({
        name: latest.metadata.name,
        commentContentRequest: {
          raw: "Edited latest body",
          content: "Edited latest body",
          version: 42,
        },
      });
    }
  );

  it("does not edit or submit stale data while loading or after a load failure", async () => {
    let reject!: (reason: Error) => void;
    api.getComment.mockReturnValue(
      new Promise((_, fail) => {
        reject = fail;
      })
    );
    const { wrapper, submit } = await setup();
    expect(wrapper.find("textarea").exists()).toBe(false);
    await submit();
    expect(api.comment).not.toHaveBeenCalled();
    reject(new Error("Load failed"));
    await flushPromises();
    expect(wrapper.find("textarea").exists()).toBe(false);
    expect(wrapper.get('[role="alert"]').text()).toBe(
      "core.common.status.loading_error"
    );
    await submit();
    expect(api.comment).not.toHaveBeenCalled();
  });

  it.each([false, true])(
    "saves the selected resource and refreshes its lists (reply=%s)",
    async (isReply) => {
      const { wrapper, submit, edit, invalidate } = await setup(isReply);
      expect(wrapper.get("textarea").element.value).toBe(
        isReply ? "Original reply" : "Original"
      );
      await submit();
      expect(api.comment).not.toHaveBeenCalled();
      expect(api.reply).not.toHaveBeenCalled();
      await edit("<p>Corrected <strong>body</strong></p>");
      // A query refresh must not advance the original version or erase the draft.
      const refreshed = isReply ? reply().reply : comment().comment;
      refreshed.metadata.version = 30;
      refreshed.spec.raw = "Other edit";
      await wrapper.setProps({ target: refreshed });
      await submit();
      expect(isReply ? api.reply : api.comment).toHaveBeenCalledWith({
        name: isReply ? "reply" : "parent",
        commentContentRequest: {
          raw: "<p>Corrected <strong>body</strong></p>",
          content: "<p>Corrected <strong>body</strong></p>",
          version: isReply ? 20 : 12,
        },
      });
      expect(isReply ? api.comment : api.reply).not.toHaveBeenCalled();
      expect(invalidate).toHaveBeenCalledWith({ queryKey: ["core:comments"] });
      expect(invalidate).toHaveBeenCalledWith({
        queryKey: ["core:comment-replies", "parent"],
      });
      expect(wrapper.emitted("close")).toHaveLength(1);
    }
  );

  it.each(["", "   ", "<p><br></p>", "<p>&nbsp;</p>"])(
    "does not submit empty content %s",
    async (body) => {
      const { submit, edit } = await setup();
      await edit(body);
      await submit();
      expect(api.comment).not.toHaveBeenCalled();
    }
  );

  it("allows image-only replies", async () => {
    const { submit, edit } = await setup(true);
    await edit('<img src="https://example.com/image.png">');
    await submit();
    expect(api.reply).toHaveBeenCalledOnce();
  });

  it("does not submit without management permission", async () => {
    api.allowed = false;
    const { submit, edit } = await setup();
    await edit("Changed");
    await submit();
    expect(api.comment).not.toHaveBeenCalled();
  });

  it("prevents duplicate submissions", async () => {
    let resolve!: (value: unknown) => void;
    api.comment.mockReturnValue(
      new Promise((done) => {
        resolve = done;
      })
    );
    const { submit, edit } = await setup();
    await edit("Changed");
    await submit();
    await submit();
    expect(api.comment).toHaveBeenCalledOnce();
    resolve({});
    await flushPromises();
  });

  it.each([409, 500])("retains drafts on a %s failure", async (status) => {
    api.comment.mockRejectedValue({ isAxiosError: true, response: { status } });
    const { wrapper, submit, edit, invalidate } = await setup();
    await edit("Unsaved correction");
    await submit();
    expect(wrapper.get("textarea").element.value).toBe("Unsaved correction");
    expect(wrapper.get('[role="alert"]').text()).toBe(
      status === 409
        ? "core.comment.edit_modal.conflict"
        : "core.comment.edit_modal.save_failed"
    );
    expect(wrapper.emitted("close")).toBeUndefined();
    expect(invalidate).not.toHaveBeenCalled();
  });

  it("cancels without changing the original object or saving", async () => {
    const { wrapper, edit } = await setup();
    await edit("Unsaved correction");
    const cancel = wrapper
      .findAll("button")
      .find((button) => button.text() === "core.common.buttons.cancel");
    await cancel!.trigger("click");
    expect(wrapper.props("target").spec.raw).toBe("Original");
    expect(wrapper.props("target").spec.content).toBe(
      "<p>Original rendering</p>"
    );
    expect(api.comment).not.toHaveBeenCalled();
    expect(wrapper.emitted("close")).toHaveLength(1);
  });
});
