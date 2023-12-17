import type { Component } from "vue";

export interface EditorProvider {
  name: string;
  displayName: string;
  logo?: string;
  component: Component;
  rawType: string;
}
