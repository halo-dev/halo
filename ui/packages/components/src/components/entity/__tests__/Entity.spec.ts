import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { VEntity } from "..";

describe("Entity", () => {
  it("should render", () => {
    expect(mount(VEntity)).toBeDefined();
  });
});
