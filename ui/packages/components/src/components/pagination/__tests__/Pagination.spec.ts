import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VPagination } from "../index";

describe("Pagination", () => {
  it("should be true", () => {
    expect(mount(VPagination)).toBeDefined();
  });

  it("provides accessible names for its controls", () => {
    const wrapper = mount(VPagination, {
      props: { page: 2, total: 100 },
    });

    expect(wrapper.get(".pagination__btn--prev").attributes("aria-label")).toBe(
      "Previous page"
    );
    expect(wrapper.get(".pagination__btn--next").attributes("aria-label")).toBe(
      "Next page"
    );

    const selects = wrapper.findAll("select");
    expect(selects[0].attributes("aria-label")).toBe("页");
    expect(selects[1].attributes("aria-label")).toBe("条 / 页");
  });
});
