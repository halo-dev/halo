import { beforeEach, describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import PostSettingModal from "../PostSettingModal.vue";
import { createPinia, setActivePinia } from "pinia";

describe("PostSettingModal", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("should render", () => {
    const wrapper = mount({
      components: {
        PostSettingModal,
      },
      template: `<PostSettingModal></PostSettingModal>`,
    });
    expect(wrapper).toBeDefined();
  });
});
