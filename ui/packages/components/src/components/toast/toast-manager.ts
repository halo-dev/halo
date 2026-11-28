import {
  createToastManager,
  type GlobalToastManager,
  type ToastActionOptions as RekaToastActionOptions,
} from "reka-ui";
import { createVNode, markRaw, render } from "vue";
import type { ToastItemData } from "./internal";
import {
  cancelToastClosing,
  DEFAULT_TOAST_DURATION,
  markToastClosing,
  TOAST_MAX_COUNT,
} from "./toast-state";
import ToastHost from "./ToastHost.vue";
import type {
  ToastActionOptions,
  ToastApi,
  ToastApiProps,
  ToastEntry,
  ToastInstance,
  ToastPromiseOptions,
  ToastProps,
} from "./types";

let toastSequence = 0;

function nextToastId() {
  toastSequence += 1;
  return `halo-toast-${toastSequence}`;
}

function normalizeDuration(duration?: number) {
  if (duration === undefined) {
    return DEFAULT_TOAST_DURATION;
  }
  return duration > 0 ? duration : 0;
}

function toRekaAction(
  action?: ToastActionOptions
): RekaToastActionOptions | undefined {
  if (!action) {
    return undefined;
  }
  return {
    label: action.label,
    altText: action.altText || action.label,
    closeOnClick: action.closeOnClick ?? true,
    onClick: action.onClick,
  };
}

function toSensitivity(item: ToastItemData) {
  return item.type === "error"
    ? ("foreground" as const)
    : ("background" as const);
}

function toToastOptions(item: ToastItemData) {
  return {
    type: toSensitivity(item),
    status: item.status,
    duration: item.duration,
    actionProps: toRekaAction(item.action),
    data: item,
  };
}

export function createToastApi(
  manager: GlobalToastManager<ToastItemData> = createToastManager<ToastItemData>(),
  ensureHost?: () => void
): ToastEntry {
  interface Entry {
    id: string;
    item: ToastItemData;
    open: boolean;
    instance: ToastInstance;
  }

  const entries = new Map<string, Entry>();

  function buildItem(
    base: ToastItemData | undefined,
    props: ToastProps
  ): ToastItemData {
    return {
      type: props.type ?? base?.type ?? "success",
      content: props.content ?? base?.content ?? "",
      title: props.title ?? base?.title,
      count: props.count ?? base?.count ?? 0,
      closable: props.closable ?? base?.closable ?? true,
      frozenOnHover: props.frozenOnHover ?? base?.frozenOnHover ?? true,
      icon: props.icon ? markRaw(props.icon) : base?.icon,
      action: props.action ?? base?.action,
      customClass: props.customClass ?? base?.customClass,
      status: props.status ?? base?.status,
      duration:
        props.duration === undefined
          ? (base?.duration ?? DEFAULT_TOAST_DURATION)
          : normalizeDuration(props.duration),
    };
  }

  function openEntries() {
    return [...entries.values()].filter((entry) => entry.open);
  }

  function findGrouped(type: ToastItemData["type"], content: string) {
    for (const entry of entries.values()) {
      if (
        entry.open &&
        !entry.item.status &&
        entry.item.type === type &&
        entry.item.content === content
      ) {
        return entry;
      }
    }
    return undefined;
  }

  function enforceMaxCount() {
    while (openEntries().length >= TOAST_MAX_COUNT) {
      const oldest = openEntries()[0];
      if (!oldest) {
        return;
      }
      // Mark it closed locally first so the loop terminates even when the
      // toast provider has not subscribed to the manager yet.
      oldest.open = false;
      manager.close(oldest.id);
    }
  }

  function update(id: string, props: ToastProps = {}) {
    const entry = entries.get(id);
    if (!entry?.open) {
      return;
    }

    const item = buildItem(entry.item, props);
    entry.item = item;
    manager.add({ id, ...toToastOptions(item) });
  }

  function dismiss(id?: string) {
    manager.close(id);
  }

  function clear() {
    manager.close();
  }

  const toast = ((userProps: ToastProps): ToastInstance => {
    ensureHost?.();

    const status = userProps.status;
    const persistent = status === "loading";
    const grouping = userProps.grouping ?? true;
    const key = userProps.key;

    if (grouping && !key && !status) {
      const existing = findGrouped(
        userProps.type ?? "success",
        userProps.content ?? ""
      );
      if (existing) {
        existing.item = {
          ...existing.item,
          count: existing.item.count + 1,
        };
        manager.add({ id: existing.id, data: existing.item });
        return existing.instance;
      }
    }

    const id = key ? `halo-toast-key-${key}` : nextToastId();
    const existing = key ? entries.get(id) : undefined;

    if (existing?.open) {
      const item = buildItem(existing.item, userProps);
      if (persistent) {
        item.duration = 0;
      }
      existing.item = item;
      manager.add({ id, ...toToastOptions(item) });
      return existing.instance;
    }

    const item = buildItem(undefined, userProps);
    if (persistent) {
      item.duration = 0;
    }

    enforceMaxCount();

    const instance: ToastInstance = {
      id,
      close: () => manager.close(id),
    };

    const entry: Entry = { id, item, open: true, instance };
    entries.set(id, entry);

    manager.add({
      id,
      ...toToastOptions(item),
      onClose: () => {
        if (entries.get(id) === entry) {
          entry.open = false;
          markToastClosing(id);
        }
        userProps.onClose?.();
      },
      onRemove: () => {
        if (entries.get(id) === entry) {
          cancelToastClosing(id);
          entries.delete(id);
        }
      },
    });

    return instance;
  }) as ToastEntry;

  toast.success = ((content: string, props?: ToastApiProps) =>
    toast({ ...props, type: "success", content })) as ToastApi;
  toast.info = ((content: string, props?: ToastApiProps) =>
    toast({ ...props, type: "info", content })) as ToastApi;
  toast.warning = ((content: string, props?: ToastApiProps) =>
    toast({ ...props, type: "warning", content })) as ToastApi;
  toast.error = ((content: string, props?: ToastApiProps) =>
    toast({ ...props, type: "error", content })) as ToastApi;
  toast.loading = ((content: string, props?: ToastApiProps) =>
    toast({
      ...props,
      type: "info",
      content,
      status: "loading",
      duration: 0,
      grouping: false,
    })) as ToastApi;

  toast.dismiss = dismiss;
  toast.clear = clear;
  toast.update = update;

  toast.promise = async <T>(
    promise: Promise<T>,
    options: ToastPromiseOptions<T>
  ): Promise<T> => {
    const loadingProps =
      typeof options.loading === "string"
        ? { content: options.loading }
        : options.loading;

    const instance = toast({
      ...loadingProps,
      type: loadingProps.type ?? "info",
      status: "loading",
      duration: 0,
      grouping: false,
    });

    try {
      const value = await promise;
      const state = options.success;
      const next = typeof state === "function" ? state(value) : state;
      const props = typeof next === "string" ? { content: next } : next;

      update(instance.id, {
        ...props,
        type: props.type ?? "success",
        status: "success",
        duration: props.duration ?? DEFAULT_TOAST_DURATION,
      });

      return value;
    } catch (error) {
      const state = options.error;
      const next = typeof state === "function" ? state(error) : state;
      const props = typeof next === "string" ? { content: next } : next;

      update(instance.id, {
        ...props,
        type: props.type ?? "error",
        status: "error",
        duration: props.duration ?? DEFAULT_TOAST_DURATION,
      });

      throw error;
    }
  };

  return toast;
}

const defaultManager = createToastManager<ToastItemData>();

let hostMounted = false;

function ensureDefaultHost() {
  if (hostMounted || typeof document === "undefined") {
    return;
  }

  hostMounted = true;

  const container = document.createElement("div");
  container.className = "toast-host";
  document.body.appendChild(container);

  render(createVNode(ToastHost, { manager: defaultManager }), container);
}

export const Toast: ToastEntry = createToastApi(
  defaultManager,
  ensureDefaultHost
);
