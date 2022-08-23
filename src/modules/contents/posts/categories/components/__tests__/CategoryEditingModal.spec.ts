import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import CategoryEditingModal from "../CategoryEditingModal.vue";

describe("CategoryEditingModal", function () {
  it("should render", function () {
    expect(mount(CategoryEditingModal)).toBeDefined();
  });
});
