import { beforeEach, describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import CategoryEditingModal from "../CategoryEditingModal.vue";
import { createPinia, setActivePinia } from "pinia";

describe("CategoryEditingModal", function () {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("should render", function () {
    expect(mount(CategoryEditingModal)).toBeDefined();
  });
});
