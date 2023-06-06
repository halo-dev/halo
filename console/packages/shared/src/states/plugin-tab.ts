export interface PluginTab {
  id: string;
  label: string;
  route: {
    name: string;
    params?: Record<string, string>;
  };
}
