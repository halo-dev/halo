import { TableView as TiptapTableView } from "@tiptap/extension-table";
import type { EditorView, Node as ProseMirrorNode } from "@/tiptap/pm";
import { normalizeTableLayoutMode, type TableLayoutMode } from "./attributes";

export class HaloTableView extends TiptapTableView {
  declare dom: HTMLDivElement;

  declare table: HTMLTableElement;

  private readonly cleanups = new Set<() => void>();

  private resizeObserver?: ResizeObserver;

  private animationFrame?: number;

  constructor(
    node: ProseMirrorNode,
    cellMinWidth: number,
    view: EditorView,
    HTMLAttributes: Record<string, unknown> = {}
  ) {
    super(node, cellMinWidth, view, HTMLAttributes);

    this.dom.className = "halo-table-wrapper";
    this.dom.dataset.gapCursorClickArea = "";
    this.dom.dataset.tableLayout = this.getLayoutMode(node);
    this.table.dataset.gapCursorAnchor = "";
    this.dom.style.boxSizing = "border-box";
    this.dom.style.overflowX = "auto";
    this.dom.style.overflowY = "hidden";
    this.dom.style.width = "100%";
    this.dom.style.maxWidth = "100%";
    this.dom.style.minWidth = "0";

    const onWheel = (event: WheelEvent) => {
      this.handleHorizontalWheel(event);
    };
    const onScroll = () => {
      this.scheduleShadowUpdate();
    };

    this.dom.addEventListener("wheel", onWheel, { passive: false });
    this.dom.addEventListener("scroll", onScroll, { passive: true });
    this.cleanups.add(() => this.dom.removeEventListener("wheel", onWheel));
    this.cleanups.add(() => this.dom.removeEventListener("scroll", onScroll));

    if (typeof ResizeObserver !== "undefined") {
      this.resizeObserver = new ResizeObserver(() => {
        this.scheduleShadowUpdate();
      });
      this.resizeObserver.observe(this.dom);
      this.resizeObserver.observe(this.table);
      this.cleanups.add(() => this.resizeObserver?.disconnect());
    }

    this.applyLayout(node);
    this.scheduleShadowUpdate();
  }

  update(node: ProseMirrorNode) {
    if (!super.update(node)) {
      return false;
    }

    this.applyLayout(node);
    this.scheduleShadowUpdate();
    return true;
  }

  destroy() {
    if (this.animationFrame !== undefined) {
      cancelAnimationFrame(this.animationFrame);
      this.animationFrame = undefined;
    }

    this.cleanups.forEach((cleanup) => cleanup());
    this.cleanups.clear();
  }

  private getLayoutMode(node: ProseMirrorNode): TableLayoutMode {
    return normalizeTableLayoutMode(node.attrs.layoutMode) ?? "auto";
  }

  private applyLayout(node: ProseMirrorNode) {
    const layoutMode = this.getLayoutMode(node);
    this.dom.dataset.tableLayout = layoutMode;
    this.table.dataset.tableLayout = layoutMode;
    this.table.style.display = "table";
    this.table.style.tableLayout = layoutMode;

    if (layoutMode === "auto") {
      this.table.style.width = "100%";
      this.table.style.minWidth = "100%";
      Array.from(this.colgroup.children).forEach((column) => {
        const element = column as HTMLTableColElement;
        element.style.removeProperty("width");
        element.style.setProperty("min-width", `${this.cellMinWidth}px`);
      });
      return;
    }

    Array.from(this.colgroup.children).forEach((column) => {
      (column as HTMLTableColElement).style.removeProperty("min-width");
    });
  }

  private scheduleShadowUpdate() {
    if (this.animationFrame !== undefined) {
      return;
    }

    this.animationFrame = requestAnimationFrame(() => {
      this.animationFrame = undefined;
      this.updateTableShadow();
    });
  }

  private updateTableShadow() {
    const { scrollWidth, clientWidth, scrollLeft } = this.dom;
    const hasOverflow = scrollWidth > clientWidth;
    const maxScrollLeft = Math.max(0, scrollWidth - clientWidth);

    this.dom.classList.toggle(
      "table-left-shadow",
      hasOverflow && scrollLeft > 0
    );
    this.dom.classList.toggle(
      "table-right-shadow",
      hasOverflow && scrollLeft < maxScrollLeft
    );
  }

  private handleHorizontalWheel(event: WheelEvent) {
    const { scrollWidth, clientWidth, scrollLeft } = this.dom;
    const maxScrollLeft = Math.max(0, scrollWidth - clientWidth);
    const canScroll =
      scrollWidth > clientWidth &&
      ((event.deltaY < 0 && scrollLeft > 0) ||
        (event.deltaY > 0 && scrollLeft < maxScrollLeft));

    if (!canScroll) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();
    this.dom.scrollBy({ left: event.deltaY });
  }
}
