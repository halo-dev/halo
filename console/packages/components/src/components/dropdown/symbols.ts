import type { InjectionKey } from "vue";

export const DropdownContextInjectionKey: InjectionKey<{
  hide: () => void;
}> = Symbol("dropdown-context");
