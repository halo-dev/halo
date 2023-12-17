import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { VPagination } from "../index";

describe("Pagination", () => {
  it("should be true", () => {
    expect(mount(VPagination)).toBeDefined();
  });
});
