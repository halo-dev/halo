import { documentFunction, sakura, type ThemeConfig } from "../main";
import { I18nFormat } from "../utils/i18nFormat";
import { Util } from "../utils/util";

export default class Post {
  /**
   * 为 mark（高亮）标签内的直接文本块添加 span 标签。
   *
   * 用于增加文本颜色与背景色的对比度，提高可读性。
   */
  @documentFunction()
  public addMarkTextSpan() {
    const markElements = document.querySelectorAll("mark");
    if (!markElements) {
      return;
    }
    markElements.forEach((markElement) => {
      const markNodes = markElement.childNodes;
      if (!markNodes) {
        return;
      }
      markNodes.forEach((node) => {
        if (node.nodeType === 3) {
          const spanElement = document.createElement("span");
          spanElement.className = "mark-text";
          spanElement.textContent = node.textContent;
          node.replaceWith(spanElement);
        }
      });
    });
  }

  /**
   * 自动计算打赏弹出位置
   * 子菜单位置为：父菜单宽度 / 2 - 子菜单宽度 / 2
   */
  @documentFunction()
  public autoCalculateSubmenuPosition() {
    const reword = sakura.getThemeConfig("post", "reward", Array<ThemeConfig>);
    if (!reword || reword.length === 0) {
      return;
    }
    const rewordOpenElement = document.querySelector(".reward-open") as HTMLElement;
    if (!rewordOpenElement) {
      return;
    }
    const rewardMainElement = rewordOpenElement.querySelector(".reward-main") as HTMLElement;
    if (!rewardMainElement) {
      return;
    }

    const parentWidth = rewordOpenElement.offsetWidth;
    // 由于 rewardMainElement 为隐藏元素，因此需要先显示出来，才能获取到其宽度
    rewardMainElement.style.visibility = "hidden";
    rewardMainElement.style.display = "block";
    const subWidth = rewardMainElement.offsetWidth;
    console.log(parentWidth, subWidth);
    rewardMainElement.style.display = "none";
    rewardMainElement.style.visibility = "visible";
    const subLeft = parentWidth / 2 - subWidth / 2;
    rewardMainElement.style.left = `${subLeft}px`;
  }

  /**
   * 注册原创文章复制携带版权功能
   */
  @documentFunction()
  public registerOriginalPostCopy() {
    const contentElement = document.querySelector(".entry-content") as HTMLElement;
    if (!contentElement) {
      return;
    }
    if (
      sakura.getPageConfig("isOriginal") == "false" ||
      !sakura.getThemeConfig("post", "post_original_copy", Boolean)?.valueOf()
    ) {
      return;
    }
    contentElement.addEventListener("copy", (event) => {
      event.preventDefault();
      const selection = window.getSelection();
      if (!selection) {
        return;
      }
      const selectedText = selection.toString();
      if (!selectedText || selectedText.length < 30) {
        return;
      }
      const postArticleElement = document.querySelector(".post-article") as HTMLElement;
      if (!postArticleElement) {
        return;
      }
      const owner = postArticleElement.getAttribute("data-owner");
      const url = postArticleElement.getAttribute("data-url");
      const siteName = postArticleElement.getAttribute("data-siteName");
      let copyrightTemplateHtml = `
      ${sakura.translate(
        "post.copyright_template_html.info",
        `# 商业转载请联系作者获得授权，非商业转载请注明出处。<br>`
      )}
      ${sakura.translate(
        "post.copyright_template_html.license",
        `# 协议(License): 署名-非商业性使用-相同方式共享 4.0 国际 (CC BY-NC-SA 4.0)<br>`
      )}
      ${sakura.translate("post.copyright_template_html.author", `# 作者(Author): ${owner} <br>`, { postAuthor: owner })}
      ${sakura.translate("post.copyright_template_html.url", `# 链接(URL): ${url} <br>`, { postUrl: url })}
      ${sakura.translate("post.copyright_template_html.url", `# 来源(Source): ${siteName} <br>`, {
        siteName: siteName,
      })}
      <br>
      `;
      const copyText = selection.toString();
      const htmlStr = `${copyrightTemplateHtml}${copyText.replace(/\r\n/g, "<br>")}`;
      const textStr = `${copyrightTemplateHtml.replace(/<br>/g, "\n")}${copyText.replace(/\r\n/g, "\n")}`;
      if (event.clipboardData) {
        event.clipboardData.setData("text/html", htmlStr);
        event.clipboardData.setData("text/plain", textStr);
      }
    });
  }

  /**
   * 文章分享页，微信二维码生成
   */
  @documentFunction()
  public async registerShareWechat() {
    const shareWechatElement = document.getElementById("qrcode");
    if (!shareWechatElement) {
      return;
    }
    // @ts-ignore
    const QRCode = await import("qrcode");
    QRCode.toCanvas(shareWechatElement, shareWechatElement.getAttribute("data-url"), {
      width: 120,
      color: {
        dark: "#000000",
        light: "#ffffff",
      },
    });
  }

  /**
   * 注册文章阅读字数提醒功能
   */
  @documentFunction()
  public registerWordCountToast() {
    if (!sakura.getThemeConfig("post", "post_word_count_toast", Boolean)?.valueOf()) {
      return;
    }

    const contentElement = document.querySelector(".entry-content") as HTMLElement;
    if (!contentElement) {
      return;
    }

    const postWordCount = Util.getWordCount(contentElement);
    if (postWordCount > 0) {
      const seconds = Util.caclEstimateReadTime(postWordCount);
      let type = "NORMAL" as "NORMAL" | "MEDIUM" | "DIFFICULTY";
      if (seconds <= 60 * 10) {
        type = "NORMAL";
      } else if (seconds > 60 * 10 && seconds <= 60 * 30) {
        type = "MEDIUM";
      } else {
        type = "DIFFICULTY";
      }
      let remind = "";
      const timeString = I18nFormat.secondToTimeString(seconds);
      if (window.innerWidth > 860) {
        const defaultRemind = {
          normal: "文章篇幅适中，可以放心阅读。",
          medium: "文章比较长，建议分段阅读。",
          difficulty: "文章内容已经很陈旧了，也许不再适用！",
        } as { [key: string]: string };
        remind =
          sakura.getThemeConfig("post", `post_word_count_toast_${type.toLowerCase()}`, String)?.valueOf() ||
          sakura.translate("post.word_count_toast.remind", defaultRemind[type.toLowerCase()], {
            context: type.toLowerCase(),
          });
      }
      this.createPostToast(
        contentElement,
        sakura.translate(
          "post.word_count_toast.content",
          `文章共 <b>${postWordCount}</b> 字，阅读完预计需要 <b>${timeString}</b>。${remind}`,
          {
            postWordCount: postWordCount,
            timeString: timeString,
            remind: remind,
          }
        ),
        type,
        "word_count"
      );
    }
  }

  /**
   * 注册文章最近更新时间提醒功能
   */
  @documentFunction()
  public registerEditTimeToast() {
    const contentElement = document.querySelector(".entry-content") as HTMLElement;
    if (!contentElement) {
      return;
    }

    if (!sakura.getThemeConfig("post", "post_edit_time_toast", Boolean)?.valueOf()) {
      return;
    }

    if (!sakura.getPageConfig("postLastModifyTime")) {
      return;
    }

    const postLastModifyTime = sakura.getPageConfig("postLastModifyTime");
    const editTime = new Date(postLastModifyTime);
    const time = new Date().getTime() - editTime.getTime();
    let type = "NORMAL" as "NORMAL" | "MEDIUM" | "DIFFICULTY";
    if (time <= 1000 * 60 * 60 * 24 * 30) {
      type = "NORMAL";
    } else if (time > 1000 * 60 * 60 * 24 * 30 && time <= 1000 * 60 * 60 * 24 * 90) {
      type = "MEDIUM";
    } else {
      type = "DIFFICULTY";
    }
    let remind = "";
    if (window.innerWidth > 860) {
      const defaultRemind = {
        normal: "近期有所更新，请放心阅读！",
        medium: "文章内容已经有一段时间没有更新了，也许不再适用！",
        difficulty: "文章内容已经很陈旧了，也许不再适用！",
      } as { [key: string]: string };
      remind =
        sakura.getThemeConfig("post", `post_edit_time_toast_${type.toLowerCase()}`, String)?.valueOf() ||
        sakura.translate("post.edit_time_toast.remind", defaultRemind[type.toLowerCase()], {
          context: type.toLowerCase(),
        });
    }
    const sinceLastTime = I18nFormat.RelativeTimeFormat(editTime.getTime());
    this.createPostToast(
      contentElement,
      sakura.translate("post.edit_time_toast.content", `文章内容上次编辑时间于 <b>${sinceLastTime}</b>。${remind}`, {
        sinceLastTime: sinceLastTime,
        remind: remind,
      }),
      type,
      "last_time"
    );
  }

  private createPostToast(
    parentElement: HTMLElement,
    message: string,
    type: "NORMAL" | "MEDIUM" | "DIFFICULTY",
    className?: string
  ) {
    const types = {
      NORMAL: "rgba(167, 210, 226, 1)",
      MEDIUM: "rgba(255, 197, 160, 1)",
      DIFFICULTY: "rgba(239, 206, 201, 1)",
    };
    const toastDivString = `<div class="${className} minicode" style="background-color: ${types[type]}">
      <span class="content-toast">
        ${message}
      </span>
      <div class="hide-minicode">
        <span class="iconify iconify--small" data-icon="fa:times"></span>
      </div>
    </div>`;
    if (parentElement.querySelector(`.${className}`)) {
      return;
    }
    const parse = new DOMParser();
    const toastDocument = parse.parseFromString(toastDivString, "text/html");
    const toastElement = toastDocument.querySelector(`.${className}`) as HTMLElement;
    const hideMinicode = toastElement.querySelector(".hide-minicode") as HTMLElement;
    hideMinicode.addEventListener(
      "click",
      () => {
        toastElement.classList.add("hide");
      },
      { once: true }
    );
    parentElement.insertAdjacentElement("afterbegin", toastElement);
  }
}
