import mitt from "mitt";

type Events = {
  /**
   * Emitted when a plugin's configuration map is updated.
   */
  "core:plugin:configMap:updated": { pluginName: string; group: string };
};

const events = mitt<Events>();

export { events };
