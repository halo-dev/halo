/* eslint-disable vue/one-component-per-file -- Test-local stubs keep this regression test isolated. */
import type { Post } from "@halo-dev/api-client";
import { utils } from "@halo-dev/ui-shared";
import messages from "@intlify/unplugin-vue-i18n/messages";
import { VueQueryPlugin } from "@tanstack/vue-query";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h } from "vue";
import { createI18n } from "vue-i18n";
import PostSettingModal from "../PostSettingModal.vue";

const plugins = [
  VueQueryPlugin,
  createI18n({
    legacy: false,
    locale: "en",
    messages,
  }),
];

function createPost(): Post {
  return {
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Post",
    metadata: {
      name: "test-post",
      annotations: {
        "content.halo.run/existing": "existing",
      },
    },
    spec: {
      title: "Test post",
      slug: "test-post",
      template: "",
      cover: "",
      deleted: false,
      publish: false,
      publishTime: undefined,
      pinned: false,
      allowComment: true,
      visible: "PUBLIC",
      priority: 0,
      excerpt: {
        autoGenerate: true,
        raw: "",
      },
      categories: [],
      tags: [],
      htmlMetas: [],
    },
  };
}

function createAnnotationsFormStub(handleSubmit: () => void) {
  return defineComponent({
    name: "AnnotationsForm",
    setup(_, { expose }) {
      expose({
        handleSubmit,
        specFormInvalid: false,
        customFormInvalid: false,
        annotations: {
          "content.halo.run/description": "from-setting-form",
        },
        customAnnotations: {
          "custom.halo.run/field": "custom-value",
        },
      });

      return () => h("div");
    },
  });
}

const FormKitStub = defineComponent({
  name: "FormKit",
  setup(_, { slots }) {
    return () => h("div", slots.default?.());
  },
});

const VModalStub = defineComponent({
  name: "VModal",
  emits: ["close"],
  setup(_, { emit, expose, slots }) {
    expose({
      close: () => emit("close"),
    });

    return () => h("div", [slots.default?.(), slots.footer?.()]);
  },
});

const VButtonStub = defineComponent({
  name: "VButton",
  setup(_, { attrs, slots }) {
    return () => h("button", attrs, slots.default?.());
  },
});

describe("PostSettingModal", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("should render", () => {
    utils.permission.setUserPermissions([]);
    const wrapper = mount(
      {
        components: {
          PostSettingModal,
        },
        template: `<PostSettingModal></PostSettingModal>`,
      },
      {
        global: {
          plugins,
        },
      }
    );
    expect(wrapper).toBeDefined();
  });

  it("merges annotation form state before emitting direct publish", async () => {
    utils.permission.setUserPermissions([]);
    const handleSubmit = vi.fn();

    const wrapper = mount(PostSettingModal, {
      props: {
        post: createPost(),
        onlyEmit: true,
      },
      global: {
        plugins,
        stubs: {
          AnnotationsForm: createAnnotationsFormStub(handleSubmit),
          FormKit: FormKitStub,
          IconRefreshLine: true,
          VButton: VButtonStub,
          VModal: VModalStub,
          VSpace: {
            template: "<div><slot /></div>",
          },
        },
      },
    });

    await (wrapper.vm.$.setupState.handlePublish as () => Promise<void>)();

    expect(handleSubmit).toHaveBeenCalledOnce();

    const publishedPost = wrapper.emitted("published")?.[0]?.[0] as Post;
    expect(publishedPost.metadata.annotations).toEqual({
      "content.halo.run/description": "from-setting-form",
      "custom.halo.run/field": "custom-value",
    });
  });
});
