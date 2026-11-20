import type { FilterDropdownVariant } from "./types";

export interface FilterTriggerState {
  selected: boolean;
  open: boolean;
  variant: FilterDropdownVariant;
}

/**
 * Shared trigger classes for filter style dropdowns. Kept in a plain module so
 * every class is a static string that Tailwind can detect.
 */
export function getFilterTriggerClasses(state: FilterTriggerState) {
  const highlighted = state.selected || state.open;

  return [
    "inline-flex h-8 cursor-pointer select-none items-center whitespace-nowrap rounded-md text-sm transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-60",
    highlighted ? "text-gray-900" : "text-gray-700",
    state.selected ? "font-semibold" : "",
    state.variant === "outline"
      ? [
          "border bg-white px-3",
          state.open
            ? "border-primary"
            : "border-gray-300 hover:border-gray-400 hover:text-gray-900",
        ]
      : [
          "px-2 active:bg-gray-200",
          state.open ? "bg-gray-100" : "hover:bg-gray-100 hover:text-gray-900",
        ],
  ];
}
