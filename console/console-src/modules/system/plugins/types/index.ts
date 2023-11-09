export interface PluginInstallationErrorResponse {
  detail: string;
  instance: string;
  pluginName: string;
  requestId: string;
  status: number;
  timestamp: string;
  title: string;
  type: string;
}
