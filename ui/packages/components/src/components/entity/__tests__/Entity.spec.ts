import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VEntity } from "..";

describe("Entity", () => {
  it("should render", () => {
    expect(mount(VEntity)).toBeDefined();
  });
});
