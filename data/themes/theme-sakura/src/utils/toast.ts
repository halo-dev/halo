interface ToastOption {
  duration?: number;
  width?: number;
  height?: number;
  top?: string | number;
  background?: string;
  color?: string;
  fontSize?: number;
}

export default class Toast {
  private _t: HTMLElement | null = null;
  private _timeOut: NodeJS.Timeout | null = null;
  private _settings: Required<ToastOption> = {
    duration: 2000,
    width: 260,
    height: 60,
    top: "top",
    background: "#fe9600",
    color: "#fff",
    fontSize: 14,
  };

  constructor(opt?: ToastOption) {
    if (opt) {
      this._settings = { ...this._settings, ...opt };
    }
  }

  public create(text: string, duration?: number) {
    // 清除原有的Toast
    if (this._timeOut) {
      clearTimeout(this._timeOut);
      if (this._t) {
        document.body.removeChild(this._t);
        this._t = null;
      }
    }

    if (!text) {
      console.error("提示文本不能为空");
      return;
    }

    this._t = document.createElement("div");
    this._t.className = "t-toast";
    this._t.innerHTML = `<p class="message"><span>${text}</span></p>`;
    document.body.appendChild(this._t);
    this.setStyle();

    this._timeOut = setTimeout(() => {
      // 移除
      if (this._t) {
        document.body.removeChild(this._t);
        this._t = null;
      }
      this._timeOut = null;
    }, duration ?? this._settings.duration);
  }

  private setStyle() {
    if (!this._t) {
      console.error("Toast元素未创建");
      return;
    }

    this._t.style.width = `${this._settings.width}px`;
    this._t.style.height = `${this._settings.height}px`;
    this._t.style.position = "fixed";
    this._t.style.textAlign = "center";
    this._t.style.zIndex = "99999";
    if (typeof this._settings.top === "string" && isNaN(Number(this._settings.top))) {
      if (this._settings.top === "center") {
        this._t.style.top = `${window.innerHeight / 2}px`;
      } else if (this._settings.top === "top") {
        this._t.style.top = "0px";
      }
    } else {
      this._t.style.top = typeof this._settings.top === "number" ? `${this._settings.top}px` : "";
    }

    this._t.style.left = "50%";
    this._t.style.marginLeft = `-${this._settings.width / 2}px`;
    this._t.style.background = this._settings.background;
    this._t.style.color = this._settings.color;
    this._t.style.borderBottomLeftRadius = "4px";
    this._t.style.borderBottomRightRadius = "4px";
    this._t.style.fontSize = `${this._settings.fontSize}px`;
    this._t.style.display = "flex";
    this._t.style.justifyContent = "center";
    this._t.style.alignItems = "center";
  }
}