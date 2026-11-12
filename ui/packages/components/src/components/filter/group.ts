import type { InjectionKey } from "vue";

export interface FilterDropdownGroupEntry {
  isActive: () => boolean;
  clear: () => void;
}

export interface FilterDropdownGroupContext {
  register: (id: symbol, entry: FilterDropdownGroupEntry) => void;
  unregister: (id: symbol) => void;
}

export const filterDropdownGroupKey: InjectionKey<FilterDropdownGroupContext> =
  Symbol("FilterDropdownGroup");
