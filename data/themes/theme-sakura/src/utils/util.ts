export class Util {
  /**
   * 对 Promise 进行封装，使其支持重试
   *
   * @param promiseFn 需要重试的方法
   * @param maxRetries 最大重试次数
   * @param interval 重试间隔
   * @returns
   */
  public static async retry<T>(promiseFn: () => Promise<T>, maxRetries = 3, interval = 1000): Promise<T> {
    try {
      const result = await promiseFn();
      return result;
    } catch (error) {
      if (maxRetries > 0) {
        await new Promise((resolve) => setTimeout(resolve, interval));
        return await Util.retry(promiseFn, maxRetries - 1, interval);
      } else {
        throw error;
      }
    }
  }

  /**
   * 将 JSON 字符串/字符串转换为 Map 对象
   *
   * @template K Map 对象的键的类型
   * @template V Map 对象的值的类型
   * @param {string|object} json 要转换的 JSON 字符串或Json 对象
   * @returns {Map<K, V>} 转换后的 Map 对象
   * @throws 当传入的 JSON 字符串格式不正确时，会抛出异常
   */
  public static jsonToMap<K extends PropertyKey, V>(json: string | object): Map<K, V> {
    try {
      let obj = json;
      if (json instanceof String) {
        obj = JSON.parse(json.toString()) as Record<K, V>;
      }
      const entries = Object.entries(obj) as Array<[K, V]>;
      return new Map(entries);
    } catch (error: any) {
      throw new Error(`解析 JSON 失败：${error.message}`);
    }
  }

  /*
   * 生成随机颜色
   * @returns {string} 颜色值，如：#ffffff
   */
  public static generateColor(): string {
    // 随机生成色相
    const hue = Math.floor(Math.random() * 360);
    // 随机生成饱和度在50%~100%之间
    const saturation = Math.floor(Math.random() * 50) + 50;
    // 随机生成亮度在40%~90%之间
    const lightness = Math.floor(Math.random() * 40) + 50;

    // 将 HSL 转为 RGB
    const h = hue / 60;
    const s = saturation / 100;
    const l = lightness / 100;
    const c = (1 - Math.abs(2 * l - 1)) * s;
    const x = c * (1 - Math.abs((h % 2) - 1));
    const m = l - c / 2;
    let r, g, b;
    if (0 <= h && h < 1) {
      [r, g, b] = [c, x, 0];
    } else if (1 <= h && h < 2) {
      [r, g, b] = [x, c, 0];
    } else if (2 <= h && h < 3) {
      [r, g, b] = [0, c, x];
    } else if (3 <= h && h < 4) {
      [r, g, b] = [0, x, c];
    } else if (4 <= h && h < 5) {
      [r, g, b] = [x, 0, c];
    } else {
      [r, g, b] = [c, 0, x];
    }
    r = Math.round((r + m) * 255);
    g = Math.round((g + m) * 255);
    b = Math.round((b + m) * 255);

    // 将 RGB 转为十六进制
    const red = r.toString(16).padStart(2, "0");
    const green = g.toString(16).padStart(2, "0");
    const blue = b.toString(16).padStart(2, "0");

    return `#${red}${green}${blue}`;
  }

  static PatternString = {
    cjk: "\\p{Script=Han}|\\p{Script=Kana}|\\p{Script=Hira}|\\p{Script=Hangul}",
    word: "[\\p{L}|\\p{N}|._]+",
  };

  static wordPattern = new RegExp(`${Util.PatternString.cjk}|${Util.PatternString.word}`, "gu");

  /**
   * 统计文章字数
   * 按词统计
   *
   * @param document 文章的 DOM 对象
   * @returns 文章字数, 0 表示没有内容
   */
  public static getWordCount(document: Document | Element) {
    return document.textContent?.normalize().match(Util.wordPattern)?.length ?? 0;
  }

  /**
   * 根据文章字数，计算预计阅读时间，单位 秒
   *
   * @param wordCount 文章字数
   * @param coefficient 文章难度系数，默认难度为 3，最小为 1，最大为 6。值越小代表越难以理解
   * @returns 预计阅读时间，单位 秒
   */
  public static caclEstimateReadTime(wordCount: number, coefficient: number = 3) {
    // 默认阅读速度 200 字每分钟
    const defaultSpeed = 400;
    coefficient = Math.min(6, Math.max(1, coefficient));
    // 每降低一个难度，就提升每分钟 100 的阅读速度。
    var speed = defaultSpeed + (coefficient - 1) * 100;
    // 根据字数计算所需秒数
    // 将分钟转化为对应的时间
    return Math.ceil((wordCount / speed) * 60);
  }
}
