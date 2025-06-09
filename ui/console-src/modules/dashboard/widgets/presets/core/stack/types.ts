export interface SimpleWidget {
  i: string;
  id: string;
  config?: Record<string, unknown>;
}

export interface StackWidgetConfig {
  auto_play?: boolean;
  auto_play_interval?: number;
  widgets: SimpleWidget[];
}
