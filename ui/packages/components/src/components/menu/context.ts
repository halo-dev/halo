import type { InjectionKey, Ref } from "vue";

export interface MenuContext {
  openIds: Ref<string[] | undefined>;
}

export const menuContextKey: InjectionKey<MenuContext> = Symbol("v-menu");
