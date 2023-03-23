import { beforeEach, describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import PostSettingModal from "../PostSettingModal.vue";
import { createPinia, setActivePinia } from "pinia";
import { VueQueryPlugin } from "@tanstack/vue-query";
import { createI18n } from "vue-i18n";
import messages from "@intlify/unplugin-vue-i18n/messages";

describe("PostSettingModal", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("should render", () => {
    const wrapper = mount(
      {
        components: {
          PostSettingModal,
        },
        template: `<PostSettingModal></PostSettingModal>`,
      },
      {
        global: {
          plugins: [
            VueQueryPlugin,
            createI18n({
              legacy: false,
              locale: "en",
              messages,
            }),
          ],
        },
      }
    );
    expect(wrapper).toBeDefined();
  });
});
