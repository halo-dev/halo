export const MIN_ROW_HEIGHT = 40;
export const MAX_ROW_HEIGHT = 2000;

export const TABLE_LAYOUT_MODES = ["auto", "fixed"] as const;
export type TableLayoutMode = (typeof TABLE_LAYOUT_MODES)[number];

export const CELL_VERTICAL_ALIGNS = ["top", "middle", "bottom"] as const;
export type CellVerticalAlign = (typeof CELL_VERTICAL_ALIGNS)[number];

const SAFE_COLOR_PATTERN =
  /^(?:#[\da-f]{3,8}|(?:rgb|hsl)a?\([\d\s.,%+-]+\)|[a-z]+|var\(--[\w-]+\))$/i;

export function normalizeTableLayoutMode(
  value: unknown
): TableLayoutMode | null {
  return TABLE_LAYOUT_MODES.includes(value as TableLayoutMode)
    ? (value as TableLayoutMode)
    : null;
}

export function normalizeCellVerticalAlign(
  value: unknown
): CellVerticalAlign | null {
  return CELL_VERTICAL_ALIGNS.includes(value as CellVerticalAlign)
    ? (value as CellVerticalAlign)
    : null;
}

export function normalizeRowHeight(value: unknown): number | null {
  const parsed =
    typeof value === "number"
      ? value
      : typeof value === "string"
        ? Number.parseFloat(value)
        : Number.NaN;

  if (!Number.isFinite(parsed)) {
    return null;
  }

  return Math.min(MAX_ROW_HEIGHT, Math.max(MIN_ROW_HEIGHT, Math.round(parsed)));
}

export function normalizeCssColor(value: unknown): string | null {
  if (typeof value !== "string") {
    return null;
  }

  const normalized = value.trim();
  if (
    !normalized ||
    normalized.length > 64 ||
    normalized.includes(";") ||
    /(?:url|expression)\s*\(/i.test(normalized) ||
    !SAFE_COLOR_PATTERN.test(normalized)
  ) {
    return null;
  }

  if (
    typeof CSS !== "undefined" &&
    typeof CSS.supports === "function" &&
    !CSS.supports("color", normalized)
  ) {
    return null;
  }

  return normalized;
}

export function parseTableLayoutMode(element: HTMLElement): TableLayoutMode {
  const wrapperLayout = element
    .closest<HTMLElement>("[data-table-layout]")
    ?.getAttribute("data-table-layout");
  const explicitLayout =
    element.getAttribute("data-table-layout") ?? wrapperLayout;
  const normalizedLayout = normalizeTableLayoutMode(explicitLayout);
  if (normalizedLayout) {
    return normalizedLayout;
  }

  if (element.style.tableLayout === "fixed") {
    return "fixed";
  }

  const hasFixedColumns = Array.from(
    element.querySelectorAll<HTMLTableColElement>("colgroup col")
  ).some((column) => {
    return Boolean(column.style.width || column.getAttribute("width"));
  });

  return hasFixedColumns ? "fixed" : "auto";
}

export function parseRowHeight(element: HTMLElement): number | null {
  return normalizeRowHeight(
    element.getAttribute("data-row-height") || element.style.height
  );
}

export function parseCellVerticalAlign(
  element: HTMLElement
): CellVerticalAlign | null {
  return normalizeCellVerticalAlign(
    element.getAttribute("data-vertical-align") || element.style.verticalAlign
  );
}

export function parseCellBackgroundColor(element: HTMLElement): string | null {
  return normalizeCssColor(
    element.getAttribute("data-background-color") ||
      element.style.backgroundColor
  );
}

export function parseColumnWidths(element: HTMLElement): number[] | null {
  const colwidth = element.getAttribute("colwidth");
  if (colwidth) {
    const widths = colwidth
      .split(",")
      .map((width) => Number.parseInt(width, 10))
      .filter((width) => Number.isFinite(width) && width > 0);
    return widths.length ? widths : null;
  }

  const row = element.parentElement;
  const table = element.closest("table");
  if (!row || !table) {
    return null;
  }

  const columnIndex = Array.from(row.children)
    .slice(0, Array.from(row.children).indexOf(element))
    .reduce((index, cell) => {
      return index + Math.max(1, Number(cell.getAttribute("colspan")) || 1);
    }, 0);
  const colspan = Math.max(
    1,
    Number.parseInt(element.getAttribute("colspan") ?? "1", 10)
  );
  const columns = Array.from(
    table.querySelectorAll<HTMLTableColElement>(":scope > colgroup > col"),
    (column) => (column.getAttribute("width") || column.style.width).trim()
  );
  const isAuto = parseTableLayoutMode(table) === "auto";
  // Auto layout takes its weights from the first row. Keep missing weights
  // for newly inserted columns instead of mixing percentages into that scale.
  const importPercentages =
    isAuto &&
    !Array.from(table.rows[0]?.cells ?? []).some((cell) =>
      cell.getAttribute("colwidth")
    ) &&
    columns.every((width) => width.endsWith("%"));
  const widths = columns
    .slice(columnIndex, columnIndex + colspan)
    .map((width) => {
      if (isAuto && width.endsWith("%")) {
        // colwidth stores integers; retain two decimal places of percentages.
        return importPercentages
          ? Math.round(Number.parseFloat(width) * 100)
          : Number.NaN;
      }
      return Number.parseInt(width, 10);
    });

  return widths.length === colspan &&
    widths.every((width) => Number.isFinite(width) && width > 0)
    ? widths
    : null;
}

export function joinStyles(
  ...styles: Array<string | null | undefined | false>
): string {
  return styles
    .filter(
      (style): style is string =>
        typeof style === "string" && Boolean(style.trim())
    )
    .map((style) => style.trim().replace(/;?$/, ";"))
    .join(" ");
}
