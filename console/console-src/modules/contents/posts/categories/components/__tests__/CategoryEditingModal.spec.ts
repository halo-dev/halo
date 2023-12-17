import { beforeEach, describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import CategoryEditingModal from "../CategoryEditingModal.vue";
import { createPinia, setActivePinia } from "pinia";
import { createI18n } from "vue-i18n";
import messages from "@intlify/unplugin-vue-i18n/messages";

describe("CategoryEditingModal", function () {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("should render", function () {
    expect(
      mount(CategoryEditingModal, {
        global: {
          plugins: [
            createI18n({
              legacy: false,
              locale: "en",
              messages,
            }),
          ],
        },
      })
    ).toBeDefined();
  });
});
