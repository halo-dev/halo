import { documentFunction, sakura } from "../main";
import { WindowEventProxy } from "../utils/eventProxy";
declare const SearchWidget: any;

/**
 * 全局事件模块
 */
export class Events {
  /**
   * 注册搜索事件，兼容搜索组件 2.3.x 及以下版本
   *
   * @returns
   */
  @documentFunction(false)
  public searchModal() {
    const jsToggerSearch = document.querySelector(".searchbox") as HTMLElement;
    if (!jsToggerSearch) {
      return;
    }

    if (!sakura.getPageConfig("showSearchModal")) {
      jsToggerSearch.addEventListener("click", () => {
        SearchWidget.open();
      });
      return;
    }

    const jsSearchModal = document.querySelector(".js-search-modal.search-form-modal") as HTMLElement;
    if (!jsSearchModal) {
      return;
    }
    jsToggerSearch.addEventListener("click", () => {
      jsSearchModal.classList.add("is-visible");
    });

    jsSearchModal.addEventListener("submit", (event: SubmitEvent) => {
      if (!sakura.$pjax) {
        return;
      }
      event.preventDefault();
      const form = event.target;
      if (!(form && form instanceof HTMLFormElement)) {
        return;
      }
      const action = form.action;
      const keyword = form.keyword;
      sakura.$pjax.loadUrl(`${action}?${keyword.name}=${keyword.value}`);
      jsSearchModal.classList.remove("is-visible");
      sakura.$pjax.refresh();
    });

    const jsSearchClose = document.querySelector(".search-close") as HTMLElement;
    if (!jsSearchClose) {
      return;
    }

    jsSearchClose.addEventListener("click", () => {
      jsSearchModal.classList.remove("is-visible");
    });
  }
  /**
   * 注册滚动事件
   *
   */
  @documentFunction(false)
  public registerScrollEvent() {
    const offset = (document.querySelector(".site-header") as HTMLElement)?.offsetHeight || 75;
    const backToTopElement = document.querySelector(".cd-top") as HTMLElement;
    const mobileBackToTopElement = document.querySelector(".m-cd-top") as HTMLElement;
    const changeSkinElement = document.querySelector(".change-skin-gear") as HTMLDivElement;
    const mobileChangeSkinElement = document.querySelector(".mobile-change-skin") as HTMLDivElement;
    window.addEventListener("scroll", () => {
      if (document.documentElement.scrollTop > offset) {
        backToTopElement?.classList.add("cd-is-visible");
        changeSkinElement.style.bottom = "0";
        if (backToTopElement.offsetHeight > window.innerHeight) {
          backToTopElement.style.top = `${window.innerHeight - backToTopElement.offsetHeight - offset}px`;
        } else {
          backToTopElement.style.top = "0";
        }
      } else {
        backToTopElement.style.top = "-900px";
        backToTopElement?.classList.remove("cd-is-visible");
        changeSkinElement.style.bottom = "-100px";
      }

      if (document.documentElement.scrollTop > 0) {
        mobileBackToTopElement.classList.add("cd-is-visible");
        mobileChangeSkinElement.classList.add("cd-is-visible");
      } else {
        mobileBackToTopElement.classList.remove("cd-is-visible");
        mobileChangeSkinElement.classList.remove("cd-is-visible");
      }
    });
  }

  /**
   * 注册回到顶部点击事件
   */

  @documentFunction(false)
  public registerBackToTopEvent() {
    const backToTopElements = document.querySelectorAll(".cd-top, .m-cd-top");
    backToTopElements.forEach((backToTopElement) => {
      backToTopElement.addEventListener("click", (event) => {
        event.preventDefault();
        if (window.pageYOffset > 0) {
          window.scrollTo({
            top: 0,
            behavior: "smooth",
          });
        }
      });
    });
  }

  /**
   * 注册监听复制事件
   */
  @documentFunction(false)
  public registerCopyEvent() {
    WindowEventProxy.addEventListener(
      "copy",
      () => {
        if (sakura.$toast) {
          sakura.$toast.create(
            sakura.translate("common.events.copy", "复制成功！<br>Copied to clipboard successfully!"),
            2000
          );
        }
      },
      2000
    );
  }

  /**
   * 注册代码块双击放大事件
   */
  @documentFunction()
  public registerCodeBlockZoomEvent() {
    const preElements = document.querySelectorAll("pre") as NodeListOf<HTMLElement>;
    preElements.forEach((preElement) => {
      preElement.addEventListener("dblclick", (event) => {
        if (event.target !== preElement) {
          return;
        }
        preElement.classList.toggle("code-block-fullscreen");
        document.querySelector("html")?.classList.toggle("code-block-fullscreen-html-scroll");
      });
    });
  }

  /**
   * 注册 hashchange 事件
   */
  @documentFunction(false)
  public registerNavigationChangeEvent() {
    window.addEventListener(
      "hashchange",
      (event: Event) => {
        const hashchangeEvent = event as HashChangeEvent;
        if (hashchangeEvent.oldURL.includes("#gallery-")) {
          return;
        }
        const id = location.hash.substring(1);
        if (!id.match(/^[A-z0-9_-]+$/)) {
          return;
        }
        const targetElement = document.getElementById(id);
        if (!targetElement) {
          return;
        }
        if (!targetElement.tagName.match(/^(?:a|select|input|button|textarea)$/i)) {
          targetElement.tabIndex = -1;
        }
        targetElement.focus();
      },
      false
    );
  }

  /**
   * 注册移动端导航栏事件
   *
   * @description: Register mobile navigation event
   * @param {*}
   * @return {*}
   */
  @documentFunction(false)
  public registerMobileNav() {
    const documents = document.querySelectorAll(".container, .site-nav-toggle, .site-sidebar");
    document.querySelector(".nav-toggle")?.addEventListener("click", () => {
      documents.forEach((element) => {
        element.classList.add("open");
      });
    });

    document.querySelector(".site-sidebar")?.addEventListener("click", () => {
      documents.forEach((element) => {
        element.classList.remove("open");
      });
    });
  }

  /**
   * 注册导航栏滚动事件
   *
   * @description: Register header event
   * @param {*}
   * @return {*}
   */
  @documentFunction(false)
  public registerHeaderEvent() {
    const topmostCoordinate = 0;
    let currentTopCoordinate = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop;
    window.addEventListener("scroll", () => {
      const scrollTopCoordinate = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop;
      const siteHeaderElement = document.querySelector(".site-header");
      if (scrollTopCoordinate === topmostCoordinate) {
        siteHeaderElement?.classList.remove("yya");
      } else {
        siteHeaderElement?.classList.add("yya");
      }
      if (scrollTopCoordinate > currentTopCoordinate) {
        siteHeaderElement?.classList.remove("sabit");
      } else {
        siteHeaderElement?.classList.add("sabit");
      }
      currentTopCoordinate = scrollTopCoordinate;
      // @ts-ignore
      import("nprogress").then((module) => {
        module.default.configure({
          minimum: 0,
          template: `
          <div class="bar" role="bar">
            <div class="peg"></div>
          </div>`,
        });
        const surPlus = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const coorY = scrollTopCoordinate / surPlus;
        module.default.set(coorY);
      });
    });
  }

  /**
   * 注册 list-pagination 分页加载事件
   *
   * @description: Register list-pagination event
   * @param {*}
   * @return {*}
   */
  @documentFunction()
  public registerPostListPaginationEvent() {
    const paginationElement = document.getElementById("pagination");
    if (!paginationElement) {
      return;
    }
    const listPaginationLinkElement = paginationElement.querySelector("a");
    if (!listPaginationLinkElement) {
      return;
    }
    listPaginationLinkElement.addEventListener("click", (event) => {
      event.preventDefault();
      const postListElement = document.getElementById("main");
      if (!postListElement) {
        return;
      }
      const targetElement = event.target as HTMLLinkElement;
      const url = targetElement.href;
      targetElement.classList.add("loading");
      targetElement.textContent = "";
      fetch(url, {
        method: "GET",
      })
        .then((response) => response.text())
        .then((html) => {
          const parser = new DOMParser();
          const doc = parser.parseFromString(html, "text/html");
          const postListNewElements = doc.querySelectorAll("#main .post");
          if (postListNewElements && postListNewElements.length > 0) {
            postListNewElements.forEach((element) => {
              postListElement.appendChild(element);
            });
          }
          // 绑定新的 DOM 元素至 pjax 上
          if (sakura.$pjax) {
            sakura.$pjax.refresh(postListElement);
          }
          const nextPaginationElement = doc.querySelector("#pagination a") as HTMLLinkElement;
          if (nextPaginationElement) {
            targetElement.href = nextPaginationElement.href;
          } else {
            paginationElement.innerHTML = `<span>${sakura.translate("page.theend", "没有更多文章了")}</span>`;
          }
        })
        .catch((error) => {
          console.error(error);
        })
        .finally(() => {
          targetElement.classList.remove("loading");
          targetElement.textContent = sakura.translate("page.next", "下一页");
          if (sakura.$localize) {
            sakura.$localize(".post");
          }
        });
      return false;
    });
  }

  /**
   * 注册主题切换事件
   */
  @documentFunction(false)
  public registerThemeChangeEvent() {
    const themeChangeButtonElements = document.querySelectorAll(".theme-change-js");
    themeChangeButtonElements.forEach((element) => {
      element.addEventListener("click", () => {
        document.querySelector(".skin-menu")?.classList.toggle("show");
      });
    });
  }

  /**
   * 注册每项主题点击事件及默认主题
   */
  @documentFunction(false)
  public registerThemeItemClickEventAndDefaultTheme() {
    const themeModelElement = document.querySelector(".skin-menu") as HTMLElement;
    const themeItemElements = themeModelElement?.querySelectorAll(".skin-menu .menu-item");
    themeItemElements?.forEach((element) => {
      const themeData: ThemeItemOptions = JSON.parse(element.getAttribute("data-item") || "{}");
      if (themeData.bg_isdefault) {
        this.registerThemeRevert(themeData);
      }
      element.addEventListener("click", () => {
        this.registerThemeRevert(themeData);
        localStorage.setItem("sakuraTheme", JSON.stringify(themeData));
        // 隐藏主题开关
        themeModelElement?.classList.remove("show");
        localStorage.setItem("systemMode", "false");
      });
    });

    WindowEventProxy.addEventListener(
      "scroll",
      () => {
        themeModelElement?.classList.remove("show");
      },
      200
    );
  }

  /**
   * 注册主题回显功能
   */
  @documentFunction(false)
  public registerThemeRevert(themeData?: ThemeItemOptions) {
    if (!themeData) {
      const localThemeData = localStorage.getItem("sakuraTheme");
      if (!localThemeData) {
        return;
      }
      themeData = JSON.parse(localThemeData);
    }
    const bodyElement = document.querySelector("body") as HTMLBodyElement;
    if (themeData?.bg_url) {
      bodyElement.style.backgroundImage = `url(${themeData?.bg_url})`;
    } else {
      bodyElement.style.backgroundImage = "";
    }
    if (themeData?.bg_night) {
      bodyElement.classList.add("dark");
    } else {
      bodyElement.classList.remove("dark");
    }

    switch (themeData?.bg_img_strategy) {
      case "cover":
        bodyElement.style.backgroundSize = "cover";
        break;
      case "no-repeat":
      case "repeat":
        bodyElement.style.backgroundRepeat = themeData.bg_img_strategy;
        break;
      default:
        bodyElement.style.backgroundSize = "auto";
        bodyElement.style.backgroundRepeat = "auto";
        break;
    }
  }

  /**
   * 监听系统暗色模式切换事件
   *
   * 切换为跟随系统的暗色模式有以下几种情况：
   * 1、用户首次安装，还未具有 systemMode 本地存储，此时如果监听到系统暗色模式切换事件，则跟随系统暗色模式。并将 systemMode 设置为 true
   * 2、切换为暗色模式后，用户手动切换为浅色模式，将 systemMode 设置为 false
   * 3、再次监听到系统暗色模式切换事件，将 systemMode 设置为 true
   */
  @documentFunction(false)
  public registerSystemDarkModeChangeEvent() {
    const bodyElement = document.querySelector("body") as HTMLBodyElement;
    const systemMode = localStorage.getItem("systemMode");
    const systemDarkMode = window.matchMedia("(prefers-color-scheme: dark)");

    if (!systemMode || systemMode === "true") {
      if (systemDarkMode.matches) {
        bodyElement.classList.add("dark");
      } else {
        bodyElement.classList.remove("dark");
      }
    }

    window.matchMedia("(prefers-color-scheme: dark)").onchange = (event) => {
      if (event.matches) {
        bodyElement.classList.add("dark");
      } else {
        bodyElement.classList.remove("dark");
      }
      localStorage.setItem("systemMode", "true");
    };
  }
}

interface ThemeItemOptions {
  bg_name: string;
  bg_url?: string;
  bg_img_strategy: string;
  bg_icon: string;
  bg_night: boolean;
  bg_isdefault: boolean;
}
