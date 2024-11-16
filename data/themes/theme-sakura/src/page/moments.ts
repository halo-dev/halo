import { documentFunction, sakura } from "../main";
import { HaloApi } from "../utils/haloApi";

export default class Moments {
  /**
   * 注册 moment 列表分页加载事件
   *
   * @description: Register moment list pagination event
   * @param {*}
   * @return {*}
   */
  @documentFunction()
  public registerMomentListPagination() {
    const paginationElement = document.getElementById("moment-list-pagination");
    if (!paginationElement) {
      return;
    }
    const listPaginationLinkElement = paginationElement.querySelector("a");
    if (!listPaginationLinkElement) {
      return;
    }
    listPaginationLinkElement.addEventListener("click", (event) => {
      event.preventDefault();
      const momentContainerElement = document.querySelector(".moments-container .moments-inner");
      if (!momentContainerElement) {
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
          const momentNewContainerElement = doc.querySelector(".moments-container .moments-inner") as HTMLElement;
          if (momentNewContainerElement) {
            this.registerMomentItem(momentNewContainerElement);
            const momentListNewElements = momentNewContainerElement.querySelectorAll(".moments-item");
            if (momentListNewElements && momentListNewElements.length > 0) {
              momentListNewElements.forEach((element) => {
                momentContainerElement.appendChild(element);
                // 重新执行 Halo 评论组件初始化
                const commentScriptElement = element.querySelector(".comment-box .comment script:last-of-type") as HTMLScriptElement;
                const code: string =
                  commentScriptElement?.text ||
                  commentScriptElement?.textContent ||
                  commentScriptElement?.innerHTML ||
                  "";
                const parent: ParentNode | null = commentScriptElement.parentNode;
                parent?.removeChild(commentScriptElement);
                const script: HTMLElementTagNameMap["script"] = document.createElement("script");
                script.type = "text/javascript";
                script.appendChild(document.createTextNode(code));
                parent?.appendChild(script);
              });
            }
          }
          const nextPaginationElement = doc.querySelector("#moment-list-pagination a") as HTMLLinkElement;
          if (nextPaginationElement) {
            targetElement.href = nextPaginationElement.href;
          } else {
            paginationElement.innerHTML = "";
          }
        })
        .catch((error) => {
          console.error(error);
        })
        .finally(() => {
          targetElement.classList.remove("loading");
          targetElement.textContent = sakura.translate("page.moments.loadmore", "加载更多...");
          if (sakura.$localize) {
            sakura.$localize(".moments-inner");
          }
        });
    });
  }

  /**
   * 注册 moment 子项功能，需保证每个子项只会执行一次
   *
   * @description: Register moment item function, ensure that each item will only be executed once
   * @param containerElement
   * @return {*}
   * @param {*}
   */
  @documentFunction()
  public registerMomentItem(containerElement?: HTMLElement) {
    const momentContainerElement = containerElement || document.querySelector(".moments-container .moments-inner");
    if (!momentContainerElement) {
      return;
    }
    const momentItemElements = momentContainerElement?.querySelectorAll(".moments-item") as NodeListOf<HTMLElement>;
    if (!momentItemElements || momentItemElements.length <= 0) {
      return;
    }

    momentItemElements.forEach((momentItemElement: HTMLElement) => {
      this.registerMomentItemLike(momentItemElement);
      this.registerMomentItemComment(momentItemElement);
    });
  }

  private registerMomentItemLike(itemElement: HTMLElement) {
    const likedIds = JSON.parse(localStorage.getItem("momentlikedIds") || "[]") as string[];
    const likeButtonElement = itemElement.querySelector(".moment-tools .moment-like");
    if (!likeButtonElement) {
      return;
    }
    const momentName = itemElement.getAttribute("data-name") || "";
    if (likedIds && likedIds?.includes(momentName)) {
      likeButtonElement.classList.add("on");
      return;
    }
    likeButtonElement.addEventListener(
      "click",
      () => {
        let upvoteCount = Number(likeButtonElement.getAttribute("data-links") || "0");
        HaloApi.like("moment.halo.run", "moments", momentName).then(() => {
          upvoteCount += 1;
          likedIds.push(momentName);
          likeButtonElement.classList.add("on");
          likeButtonElement.setAttribute("data-links", upvoteCount.toString());
          const likeTitleElement = likeButtonElement.querySelector(".moment-like-text");
          if (likeTitleElement) {
            likeTitleElement.textContent = upvoteCount.toString();
          }
          localStorage.setItem("momentlikedIds", JSON.stringify(likedIds));
        });
      },
      { once: true }
    );
  }

  private registerMomentItemComment(itemElement: HTMLElement) {
    const commentButtonElement = itemElement.querySelector(".moment-tools .comment-js");
    if (!commentButtonElement) {
      return;
    }
    commentButtonElement.addEventListener("click", () => {
      const commentBoxElement = itemElement.querySelector(".comment-box");
      commentBoxElement?.classList.toggle("is-show");
    });
  }
}
