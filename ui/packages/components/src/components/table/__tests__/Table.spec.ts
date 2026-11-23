import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { h, nextTick } from "vue";
import { VTable, createTableColumnHelper } from "../index";

interface Row {
  id: number;
  name: string;
  age: number | null;
}

const rows: Row[] = [
  { id: 1, name: "beta", age: 30 },
  { id: 2, name: "alpha", age: null },
  { id: 3, name: "gamma", age: 20 },
];

const rowColumn = createTableColumnHelper<Row>();

const columns = [
  rowColumn.accessor("name", {
    header: "姓名",
    enableSorting: true,
    sortFn: "text",
  }),
  rowColumn.accessor("age", {
    header: "年龄",
  }),
];

interface ExposedTable {
  getRowModel: () => { rows: unknown[] };
  getVisibleLeafColumns: () => unknown[];
  getColumn: (id: string) =>
    | {
        toggleVisibility: (visible: boolean) => void;
        pin: (position: "start" | "end" | false) => void;
      }
    | undefined;
}

function mountTable(
  props: Record<string, unknown> = {},
  options: Record<string, unknown> = {}
) {
  return mount(VTable, {
    props: {
      data: rows,
      columns,
      rowKey: "id",
      ...props,
    },
    ...options,
  });
}

async function settle() {
  await nextTick();
  await nextTick();
}

function getBodyText(wrapper: ReturnType<typeof mountTable>, index = 0) {
  return wrapper
    .findAll("tbody tr")
    [index].findAll("td")
    .map((cell) => cell.text());
}

describe("Table", () => {
  it("renders headers and rows with a fallback for empty values", () => {
    const wrapper = mountTable();

    expect(wrapper.findAll("thead th").map((cell) => cell.text())).toEqual([
      "姓名",
      "年龄",
    ]);
    expect(wrapper.findAll("tbody tr")).toHaveLength(3);
    expect(getBodyText(wrapper)).toEqual(["beta", "30"]);
    expect(getBodyText(wrapper, 1)).toEqual(["alpha", "-"]);
  });

  it("updates rows when data changes", async () => {
    const wrapper = mountTable();

    await wrapper.setProps({ data: [rows[2]] });

    expect(wrapper.findAll("tbody tr")).toHaveLength(1);
    expect(getBodyText(wrapper)).toEqual(["gamma", "20"]);
  });

  it("renders cell renderers and lets cell slots take precedence", () => {
    const customColumns = [
      rowColumn.accessor("name", {
        header: "姓名",
        cell: (info) => h("span", { class: "cell-renderer" }, info.getValue()),
      }),
      rowColumn.accessor("age", { header: "年龄" }),
    ];

    const rendererWrapper = mountTable({ columns: customColumns });

    expect(rendererWrapper.find(".cell-renderer").text()).toBe("beta");

    const slotWrapper = mountTable(
      { columns: customColumns },
      {
        slots: {
          "cell-name": ({ original }: { original: Row }) =>
            h("span", { class: "cell-slot" }, original.name),
        },
      }
    );

    expect(slotWrapper.find(".cell-renderer").exists()).toBe(false);
    expect(slotWrapper.find(".cell-slot").text()).toBe("beta");
  });

  it("renders header slots", () => {
    const wrapper = mountTable(
      {},
      {
        slots: {
          "header-name": () => "自定义标题",
        },
      }
    );

    expect(wrapper.findAll("thead th")[0].text()).toBe("自定义标题");
  });

  it("only renders sort controls for sortable columns", () => {
    const wrapper = mountTable();

    expect(wrapper.findAll("thead th button")).toHaveLength(1);
    expect(wrapper.findAll("thead th")[1].attributes("aria-sort")).toBe(
      undefined
    );
  });

  it("sorts rows locally and cycles the sort direction", async () => {
    const wrapper = mountTable();

    await wrapper.findAll("thead th button")[0].trigger("click");
    await settle();

    expect(wrapper.emitted("update:sorting")?.[0]?.[0]).toEqual([
      { id: "name", desc: false },
    ]);
    expect(
      wrapper.findAll("tbody tr").map((row) => row.findAll("td")[0].text())
    ).toEqual(["alpha", "beta", "gamma"]);
    expect(wrapper.findAll("thead th")[0].attributes("aria-sort")).toBe(
      "ascending"
    );

    await wrapper.findAll("thead th button")[0].trigger("click");
    await settle();

    expect(wrapper.findAll("thead th")[0].attributes("aria-sort")).toBe(
      "descending"
    );
    expect(
      wrapper.findAll("tbody tr").map((row) => row.findAll("td")[0].text())
    ).toEqual(["gamma", "beta", "alpha"]);

    await wrapper.findAll("thead th button")[0].trigger("click");
    await settle();

    expect(wrapper.findAll("thead th")[0].attributes("aria-sort")).toBe("none");
    expect(
      wrapper.findAll("tbody tr").map((row) => row.findAll("td")[0].text())
    ).toEqual(["beta", "alpha", "gamma"]);
  });

  it("emits sorting without reordering when manual sorting is enabled", async () => {
    const wrapper = mountTable({ manualSorting: true });

    await wrapper.findAll("thead th button")[0].trigger("click");
    await settle();

    expect(wrapper.emitted("update:sorting")?.[0]?.[0]).toEqual([
      { id: "name", desc: false },
    ]);
    expect(
      wrapper.findAll("tbody tr").map((row) => row.findAll("td")[0].text())
    ).toEqual(["beta", "alpha", "gamma"]);
  });

  it("selects rows, shows an indeterminate header, and selects all", async () => {
    const wrapper = mountTable({ selectable: true });
    const headerCheckbox = wrapper.find("thead input[type='checkbox']");
    const rowCheckboxes = wrapper.findAll("tbody input[type='checkbox']");

    expect(rowCheckboxes).toHaveLength(3);
    expect(headerCheckbox.attributes("aria-label")).toBe("Select all");
    expect(rowCheckboxes[0].attributes("aria-label")).toBe("Select 1");

    await rowCheckboxes[0].trigger("click");
    await settle();

    expect(wrapper.emitted("update:rowSelection")?.[0]?.[0]).toEqual({
      "1": true,
    });
    expect(wrapper.findAll("tbody tr")[0].classes()).toContain("bg-gray-100");
    expect(
      (wrapper.find("thead input[type='checkbox']").element as HTMLInputElement)
        .indeterminate
    ).toBe(true);

    await wrapper.find("thead input[type='checkbox']").trigger("click");
    await settle();

    expect(wrapper.emitted("update:rowSelection")?.[1]?.[0]).toEqual({
      "1": true,
      "2": true,
      "3": true,
    });
    expect(
      (wrapper.find("thead input[type='checkbox']").element as HTMLInputElement)
        .checked
    ).toBe(true);
    expect(
      (wrapper.find("thead input[type='checkbox']").element as HTMLInputElement)
        .indeterminate
    ).toBe(false);
  });

  it("uses a custom row key function for selection", async () => {
    const wrapper = mountTable({
      selectable: true,
      rowKey: (row: Row) => `row-${row.id}`,
    });

    await wrapper.findAll("tbody input[type='checkbox']")[0].trigger("click");
    await settle();

    expect(wrapper.emitted("update:rowSelection")?.[0]?.[0]).toEqual({
      "row-1": true,
    });
  });

  it("expands a row and renders the expanded slot", async () => {
    const wrapper = mountTable(
      { expandable: true },
      {
        slots: {
          expanded: ({ row }: { row: { original: Row } }) =>
            h("div", { class: "expanded-detail" }, row.original.name),
        },
      }
    );

    expect(wrapper.findAll("tbody button[aria-label='Expand']")).toHaveLength(
      3
    );

    await wrapper
      .findAll("tbody button[aria-label='Expand']")[0]
      .trigger("click");
    await settle();

    expect(wrapper.emitted("update:expanded")?.[0]?.[0]).toEqual({
      "1": true,
    });
    expect(wrapper.find(".expanded-detail").text()).toBe("beta");
    expect(wrapper.findAll("tbody tr")).toHaveLength(4);
    expect(
      wrapper.findAll("tbody tr")[1].find("td").attributes("colspan")
    ).toBe("3");
    expect(wrapper.find("tbody button[aria-label='Collapse']").exists()).toBe(
      true
    );

    await wrapper
      .findAll("tbody button[aria-label='Collapse']")[0]
      .trigger("click");
    await settle();

    expect(wrapper.find(".expanded-detail").exists()).toBe(false);
  });

  it("renders empty and loading states", () => {
    const emptyWrapper = mountTable({ data: [] });
    expect(emptyWrapper.find("tbody td").text()).toBe("No data");
    expect(emptyWrapper.find("tbody td").attributes("colspan")).toBe("2");

    const emptySlotWrapper = mountTable(
      { data: [] },
      { slots: { empty: () => "没有记录" } }
    );
    expect(emptySlotWrapper.find("tbody td").text()).toBe("没有记录");

    const loadingWrapper = mountTable({ data: [], loading: true });
    expect(loadingWrapper.find("tbody .animate-spin").exists()).toBe(true);
    expect(loadingWrapper.find("table").attributes("aria-busy")).toBe("true");

    const loadingSlotWrapper = mountTable(
      { data: [], loading: true },
      { slots: { loading: () => "加载中" } }
    );
    expect(loadingSlotWrapper.find("tbody td").text()).toBe("加载中");
  });

  it("keeps existing rows visible while loading", () => {
    const wrapper = mountTable({ loading: true });

    expect(wrapper.findAll("tbody tr")).toHaveLength(3);
    expect(wrapper.find("table").attributes("aria-busy")).toBe("true");
  });

  it("applies sticky header classes", () => {
    const wrapper = mountTable({ sticky: true });

    expect(wrapper.find("thead").classes()).toContain("sticky");
    expect(wrapper.find("thead").classes()).toContain("top-0");
  });

  it("isolates pinned and sticky z-indexes from the page stacking context", () => {
    const wrapper = mountTable({ sticky: true, pinnable: true });

    expect(wrapper.classes()).toContain("isolate");
  });

  it("emits row-click only for non-interactive content", async () => {
    const onRowClick = vi.fn();
    const interactiveColumns = [
      rowColumn.accessor("name", {
        header: "姓名",
        cell: () => h("a", { class: "action-link", href: "#" }, "链接"),
      }),
      rowColumn.accessor("age", { header: "年龄" }),
      rowColumn.display({
        id: "actions",
        header: "操作",
        cell: () => h("button", { class: "action-button" }, "操作"),
      }),
    ];
    const wrapper = mountTable({ columns: interactiveColumns, onRowClick });

    await wrapper.find(".action-link").trigger("click");
    await wrapper.find(".action-button").trigger("click");

    expect(onRowClick).not.toHaveBeenCalled();

    await wrapper.findAll("tbody tr")[0].findAll("td")[1].trigger("click");

    expect(onRowClick).toHaveBeenCalledTimes(1);
    expect(onRowClick).toHaveBeenCalledWith(rows[0]);
  });

  it("renders multi-level headers with spans", () => {
    interface GroupedRow {
      id: number;
      first: string;
      last: string;
      age: number;
    }

    const groupedColumn = createTableColumnHelper<GroupedRow>();
    const groupedColumns = [
      groupedColumn.group({
        id: "name",
        header: "姓名",
        columns: [
          groupedColumn.accessor("first", { header: "名" }),
          groupedColumn.accessor("last", { header: "姓" }),
        ],
      }),
      groupedColumn.accessor("age", { header: "年龄" }),
    ];

    const wrapper = mount(VTable, {
      props: {
        data: [{ id: 1, first: "三", last: "张", age: 20 }],
        columns: groupedColumns,
        rowKey: "id",
      },
    });

    const headerRows = wrapper.findAll("thead tr");
    expect(headerRows).toHaveLength(2);

    const topCells = headerRows[0].findAll("th");
    expect(topCells).toHaveLength(2);
    expect(topCells[0].text()).toBe("姓名");
    expect(topCells[0].attributes("colspan")).toBe("2");
    expect(topCells[1].text()).toBe("年龄");
    expect(topCells[1].attributes("rowspan")).toBe("2");

    expect(headerRows[1].findAll("th").map((cell) => cell.text())).toEqual([
      "名",
      "姓",
    ]);
  });

  it("applies column metadata, ui classes, and captions", () => {
    const metaColumns = [
      rowColumn.accessor("name", {
        header: "姓名",
        meta: {
          align: "right",
          width: 120,
          thClass: "custom-th",
          tdClass: "custom-td",
        },
      }),
    ];

    const wrapper = mountTable({
      columns: metaColumns,
      caption: "文章列表",
      ui: {
        root: "custom-root",
        th: "custom-ui-th",
        td: "custom-ui-td",
      },
    });

    expect(wrapper.find("caption").text()).toBe("文章列表");
    expect(wrapper.find(".custom-root").exists()).toBe(true);
    expect(wrapper.find("th").classes()).toContain("custom-th");
    expect(wrapper.find("th").classes()).toContain("custom-ui-th");
    expect(wrapper.find("th").classes()).toContain("text-right");
    expect(wrapper.find("th").attributes("style")).toContain("120px");
    expect(wrapper.find("td").classes()).toContain("custom-td");
    expect(wrapper.find("td").classes()).toContain("custom-ui-td");
    expect(wrapper.find("td").classes()).toContain("text-right");
  });

  it("supports controlled column visibility and exposes the table instance", async () => {
    const wrapper = mountTable({ columnVisibility: { age: false } });

    expect(wrapper.findAll("thead th")).toHaveLength(1);
    expect(wrapper.findAll("tbody tr")[0].findAll("td")).toHaveLength(1);

    const vm = wrapper.vm as unknown as {
      tableApi: ExposedTable;
      tableRef: HTMLTableElement | null;
    };

    expect(vm.tableApi.getRowModel().rows).toHaveLength(3);
    expect(vm.tableRef?.tagName).toBe("TABLE");

    vm.tableApi.getColumn("age")?.toggleVisibility(true);
    await settle();

    expect(wrapper.findAll("thead th")).toHaveLength(2);
    expect(wrapper.emitted("update:columnVisibility")).toBeTruthy();
  });

  it("renders resize handles and applies fixed column sizes", async () => {
    const wrapper = mountTable({ resizable: true });

    expect(wrapper.find("table").classes()).toContain("table-fixed");
    expect(wrapper.findAll("colgroup col")).toHaveLength(2);
    expect(wrapper.findAll("thead th")[0].attributes("style")).toContain(
      "width: 150px"
    );
    expect(wrapper.findAll("tbody td")[0].attributes("style")).toContain(
      "width: 150px"
    );

    const handles = wrapper.findAll("[role='separator']");
    expect(handles).toHaveLength(2);
    expect(handles[0].attributes("aria-label")).toBe("Resize column");

    handles[0].element.dispatchEvent(
      new MouseEvent("mousedown", { bubbles: true, clientX: 150 })
    );
    document.dispatchEvent(
      new MouseEvent("mousemove", { bubbles: true, clientX: 100 })
    );
    document.dispatchEvent(
      new MouseEvent("mouseup", { bubbles: true, clientX: 100 })
    );
    await settle();

    expect(wrapper.emitted("update:columnSizing")?.[0]?.[0]).toEqual({
      name: 100,
    });
    expect(wrapper.findAll("thead th")[0].attributes("style")).toContain(
      "width: 100px"
    );
  });

  it("pins columns and emits pinning changes", async () => {
    const wrapper = mountTable({
      pinnable: true,
      columnPinning: { start: [], end: ["age"] },
    });

    expect(wrapper.find("table").classes()).toContain("table-fixed");
    expect(wrapper.findAll("thead th")[1].attributes("style")).toContain(
      "right: 0px"
    );
    expect(wrapper.findAll("tbody td")[1].attributes("style")).toContain(
      "right: 0px"
    );
    expect(wrapper.findAll("tbody td")[1].classes()).toContain("sticky");

    const vm = wrapper.vm as unknown as { tableApi: ExposedTable };
    vm.tableApi.getColumn("name")?.pin("start");
    await settle();

    expect(wrapper.emitted("update:columnPinning")?.[0]?.[0]).toEqual({
      start: ["name"],
      end: ["age"],
    });
    expect(wrapper.findAll("thead th")[0].attributes("style")).toContain(
      "left: 0px"
    );
  });

  it("does not render a header menu for utility columns", () => {
    const wrapper = mountTable({
      selectable: true,
      expandable: true,
      headerMenu: true,
      pinnable: true,
    });

    expect(
      wrapper.findAll("thead button[aria-label='Column actions']")
    ).toHaveLength(2);
    expect(wrapper.findAll("thead th")[0].attributes("style")).not.toContain(
      "padding-right"
    );
  });

  it("renders a header menu for sortable and hideable columns", async () => {
    const wrapper = mountTable(
      { headerMenu: true },
      { attachTo: document.body }
    );

    const menuButtons = wrapper.findAll(
      "thead button[aria-label='Column actions']"
    );
    expect(menuButtons).toHaveLength(2);
    expect(wrapper.findAll("thead th")[0].attributes("style")).toContain(
      "padding-right: 2.5rem"
    );

    await menuButtons[0].trigger("click");
    await settle();
    await new Promise((resolve) => setTimeout(resolve, 50));

    const labels = Array.from(
      document.body.querySelectorAll(".dropdown-item-wrapper")
    ).map((item) => item.textContent?.trim());

    expect(labels).toContain("Sort ascending");
    expect(labels).toContain("Sort descending");
    expect(labels).toContain("Hide column");
    expect(labels).not.toContain("Pin to start");

    wrapper.unmount();
  });

  it("virtualizes rows when enabled", async () => {
    const manyRows = Array.from({ length: 100 }, (_, index) => ({
      id: index + 1,
      name: `name-${index + 1}`,
      age: index,
    }));

    const heightSpy = vi
      .spyOn(HTMLElement.prototype, "offsetHeight", "get")
      .mockReturnValue(400);
    const widthSpy = vi
      .spyOn(HTMLElement.prototype, "offsetWidth", "get")
      .mockReturnValue(800);

    try {
      const wrapper = mountTable({
        data: manyRows,
        virtualize: true,
        sticky: true,
      });
      await settle();

      const renderedRows = wrapper.findAll("tbody tr").length;
      expect(renderedRows).toBeGreaterThan(0);
      expect(renderedRows).toBeLessThan(manyRows.length);
    } finally {
      heightSpy.mockRestore();
      widthSpy.mockRestore();
    }
  });
});
