const ownedStyles = new WeakSet<HTMLLinkElement>();

export function loadStyle(href: string, before: ChildNode | null = null) {
  return new Promise<HTMLLinkElement>(function (resolve, reject) {
    let shouldAppend = false;
    let el: HTMLLinkElement | null = document.querySelector(
      'link[href="' + href + '"]'
    );
    if (!el) {
      el = document.createElement("link");
      el.rel = "stylesheet";
      el.type = "text/css";
      el.href = href;
      shouldAppend = true;
      ownedStyles.add(el);
    } else if (el.hasAttribute("data-loaded")) {
      resolve(el);
      return;
    }

    const cleanup = () => {
      el?.removeEventListener("error", handleFailure);
      el?.removeEventListener("abort", handleFailure);
      el?.removeEventListener("load", handleLoad);
    };
    const handleFailure = (event: Event) => {
      cleanup();
      if (shouldAppend) {
        ownedStyles.delete(el as HTMLLinkElement);
        el?.remove();
      }
      reject(event);
    };
    const handleLoad = () => {
      cleanup();
      el?.setAttribute("data-loaded", "true");
      resolve(el as HTMLLinkElement);
    };
    el.addEventListener("error", handleFailure);
    el.addEventListener("abort", handleFailure);
    el.addEventListener("load", handleLoad);

    if (shouldAppend) document.head.insertBefore(el, before);
  });
}

export function unloadStyle(loadedStyle: unknown) {
  if (
    loadedStyle instanceof HTMLLinkElement &&
    ownedStyles.delete(loadedStyle)
  ) {
    loadedStyle.remove();
  }
}
