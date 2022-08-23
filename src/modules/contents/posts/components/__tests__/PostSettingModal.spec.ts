import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import PostSettingModal from "../PostSettingModal.vue";
import { VDialogProvider } from "@halo-dev/components";

describe("PostSettingModal", () => {
  it("should render", () => {
    const wrapper = mount({
      components: {
        VDialogProvider,
        PostSettingModal,
      },
      template: `
        <VDialogProvider>
        <PostSettingModal></PostSettingModal>
        </VDialogProvider>`,
    });
    expect(wrapper).toBeDefined();
  });
});
