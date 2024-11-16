/**
 * WindowEventProxy
 *
 * @description 代理 window 事件。使用节流处理事件，防止事件频繁触发
 */
export class WindowEventProxy {
  private static eventThrottles: Map<string, Set<EventListener>> = new Map();

  public static addEventListener(eventType: string, listener: EventListener, delay: number) {
    let throttle: EventListener | undefined = WindowEventProxy.throttle(listener, delay);
    let throttles = this.eventThrottles.get(eventType);
    if (!throttles) {
      throttles = new Set();
      this.eventThrottles.set(eventType, throttles);
    }
    throttles.add(throttle);
    window.addEventListener(eventType, throttle);
  }

  public static throttle(fn: Function, delay: number) {
    let timer: number | null = null;
    return (...args: any[]) => {
      if (!timer) {
        fn.apply(this, args);
        timer = window.setTimeout(() => {
          timer = null;
        }, delay);
      }
    };
  }
}
