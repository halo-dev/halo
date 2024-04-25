import type { Component, Raw } from "vue";

export interface UserTab {
  id: string;
  label: string;
  component: Raw<Component>;
  permissions?: string[];
  priority: number;
}

export interface UserProfileTab {
  id: string;
  label: string;
  component: Raw<Component>;
  permissions?: string[];
  priority: number;
}
