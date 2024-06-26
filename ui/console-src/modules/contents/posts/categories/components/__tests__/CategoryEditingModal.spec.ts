import messages from "@intlify/unplugin-vue-i18n/messages";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import CategoryEditingModal from "../CategoryEditingModal.vue";

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
