import { documentFunction, sakura } from "../main";
// @ts-ignore
import cloneDeep from "lodash.clonedeep";
// @ts-ignore
import cloud from "d3-cloud";
import { Util } from "../utils/util";
declare const wordClouds: object[];

export default class Tags {
  @documentFunction()
  public registerTags() {
    const tagChips = document.querySelectorAll(".tags-container .chip") as NodeListOf<HTMLElement>;
    tagChips.forEach((tagChip) => {
      // 如果为白色则设置随机色
      let color = tagChip.style.backgroundColor;
      if (!color || color === "rgb(255, 255, 255)") {
        color = Util.generateColor();
      }
      tagChip.style.backgroundColor = color;
    });
  }

  /**
   *
   * 注册标签云
   *
   * @description 生成标签云
   * @see https://github.com/jasondavies/d3-cloud
   */
  @documentFunction()
  public async registerTagsWordCloud() {
    const wordCloudElement = document.getElementById("tag-wordcloud");
    if (!wordCloudElement) {
      return;
    }
    // @ts-ignore
    const d3 = await import("d3");
    const cloneWords = cloneDeep(wordClouds);
    var layout = cloud()
      .size([wordCloudElement.offsetWidth, wordCloudElement.offsetHeight])
      .words(cloneWords)
      .rotate(() => {
        return Math.random() * 60 - 30;
      })
      .fontSize(function (d: any) {
        return 100 - (1 / (d.size + 1)) * 120;
      })
      .padding(5)
      .on("end", (words: object[]) => {
        d3.select("#tag-wordcloud")
          .append("svg")
          .attr("width", layout.size()[0])
          .attr("height", layout.size()[1])
          .append("g")
          .attr("transform", "translate(" + layout.size()[0] / 2 + "," + layout.size()[1] / 2 + ")")
          .selectAll("text")
          .data(words)
          .enter()
          .append("svg:a")
          .attr("xlink:href", function (d: any) {
            return d.link;
          })
          .attr("data-pjax", "")
          .append("text")
          .style("font-size", function (d: any) {
            return d.size;
          })
          .style("font-family", "Impact")
          .style("cursor", "pointer")
          .style("font-weight", 500)
          .style("fill", (d: any) => {
            return !d.color || d.color === "#ffffff" ? Util.generateColor() : d.color;
          })
          .attr("text-anchor", "middle")
          .attr("transform", function (d: any) {
            return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
          })
          .text(function (d: any) {
            return d.text;
          });
        // 如果存在 pjax，则刷新 wordCloudElement
        if (sakura.$pjax) {
          // TODO 虽然添加上了，但似乎没生效
          sakura.$pjax.refresh(wordCloudElement);
        }
      });
    layout.start();
  }
}
