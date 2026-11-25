import { createNode } from "@formkit/core";
import { bindings } from "@formkit/vue";
import { paginate } from "@halo-dev/api-client";
import { utils } from "@halo-dev/ui-shared";
import { flushPromises } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { roleSelect } from "../role-select";

vi.mock("@halo-dev/api-client", () => ({
  coreApiClient: { role: { listRole: vi.fn() } },
  paginate: vi.fn(),
}));

describe("roleSelect", () => {
  beforeEach(() => {
    utils.permission.setUserPermissions(["*"]);
    vi.mocked(paginate).mockReset();
  });

  it.each([
    { excludedNames: undefined, expectedNames: ["super-role", "guest"] },
    { excludedNames: [], expectedNames: ["super-role", "guest"] },
    { excludedNames: ["super-role"], expectedNames: ["guest"] },
    { excludedNames: ["guest"], expectedNames: ["super-role"] },
    { excludedNames: ["super-role", "guest"], expectedNames: [] },
  ])(
    "filters role options with excludedNames=$excludedNames",
    async ({ excludedNames, expectedNames }) => {
      vi.mocked(paginate).mockResolvedValue([
        { metadata: { name: "super-role" } },
        { metadata: { name: "guest" } },
      ]);
      const node = createNode({
        props: { definition: roleSelect, excludedNames, attrs: {} },
        plugins: [bindings],
      });

      await flushPromises();

      expect(node.context?.attrs.options).toEqual(
        expectedNames.map((value) => ({ label: value, value }))
      );
      node.destroy();
    }
  );

  it("skips the request and marks the input when permission is missing", async () => {
    utils.permission.setUserPermissions([]);
    const node = createNode({
      props: { definition: roleSelect, attrs: {} },
      plugins: [bindings],
    });

    await flushPromises();

    expect(paginate).not.toHaveBeenCalled();
    expect(node.context?.attrs.options).toBeUndefined();
    expect(node.props.disabled).toBe(true);
    expect(node.props.help).toBeTruthy();
    node.destroy();
  });
});
