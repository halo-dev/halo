export function loadStyle(href: string) {
  return new Promise(function (resolve, reject) {
    let shouldAppend = false;
    let el: HTMLLinkElement | null = document.querySelector(
      'script[src="' + href + '"]'
    );
    if (!el) {
      el = document.createElement("link");
      el.rel = "stylesheet";
      el.type = "text/css";
      el.href = href;
      shouldAppend = true;
    } else if (el.hasAttribute("data-loaded")) {
      resolve(el);
      return;
    }

    el.addEventListener("error", reject);
    el.addEventListener("abort", reject);
    el.addEventListener("load", function loadStyleHandler() {
      el?.setAttribute("data-loaded", "true");
      resolve(el);
    });

    if (shouldAppend) document.head.prepend(el);
  });
}
