import { createNode } from "@formkit/core";
import { bindings } from "@formkit/vue";
import { flushPromises } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { userSelect } from "../user-select";

const { listUsersMock, paginateMock } = vi.hoisted(() => ({
  listUsersMock: vi.fn(),
  paginateMock: vi.fn(),
}));

vi.mock("@halo-dev/api-client", () => ({
  consoleApiClient: { user: { listUsers: listUsersMock } },
  paginate: paginateMock,
}));

function createUserSelectNode(effectiveRole?: string) {
  return createNode({
    props: { definition: userSelect, effectiveRole, attrs: {} },
    plugins: [bindings],
  });
}

describe("userSelect", () => {
  it("disables remoteOptimize so selected values outside the filtered set still resolve", async () => {
    const node = createUserSelectNode("role-template-post-contributor");
    await flushPromises();

    expect(node.props.remoteOptimize).toBe(false);
    node.destroy();
  });

  it("passes effectiveRole to user search", async () => {
    listUsersMock.mockResolvedValue({
      data: { items: [], total: 0, size: 10, page: 1 },
    });
    const node = createUserSelectNode("role-template-post-contributor");
    await flushPromises();

    const remoteOption = node.props.remoteOption as {
      search: (params: {
        keyword: string;
        page: number;
        size: number;
      }) => Promise<unknown>;
    };
    await remoteOption.search({ keyword: "", page: 1, size: 10 });

    expect(listUsersMock).toHaveBeenCalledWith(
      expect.objectContaining({
        effectiveRole: "role-template-post-contributor",
      })
    );
    node.destroy();
  });

  it("resolves selected values without the effectiveRole filter", async () => {
    paginateMock.mockResolvedValue([]);
    const node = createUserSelectNode("role-template-post-contributor");
    await flushPromises();

    const remoteOption = node.props.remoteOption as {
      findOptionsByValues: (values: string[]) => Promise<unknown>;
    };
    await remoteOption.findOptionsByValues(["alice"]);

    expect(paginateMock).toHaveBeenCalledTimes(1);
    const [, params] = paginateMock.mock.calls[0];
    expect(params).not.toHaveProperty("effectiveRole");
    node.destroy();
  });
});
