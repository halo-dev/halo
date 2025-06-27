let lastMouseMoveTime = 0;

export const throttle = <T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay: number
): ((...args: Parameters<T>) => void) => {
  return (...args: Parameters<T>) => {
    const now = Date.now();
    if (now - lastMouseMoveTime >= delay) {
      lastMouseMoveTime = now;
      fn(...args);
    }
  };
};
