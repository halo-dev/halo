import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { VPagination } from "../index";

describe("Pagination", () => {
  it("should be true", () => {
    expect(mount(VPagination)).toBeDefined();
  });
});
