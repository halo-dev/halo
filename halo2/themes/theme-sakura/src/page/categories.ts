import { documentFunction, sakura } from "../main";
import { Util } from "../utils/util";
declare const categoryRadar: Map<string, number>;

export default class Categories {
  @documentFunction()
  public registerCategories() {
    const categoryChips = document.querySelectorAll(".categories-container .chip") as NodeListOf<HTMLElement>;
    categoryChips.forEach((chip) => {
      if (!chip.style.backgroundColor) {
        chip.style.backgroundColor = Util.generateColor();
      }
    });
  }

  @documentFunction()
  public async registerCategoryRadarChart() {
    const echartElement = document.getElementById("category-echarts");
    if (!echartElement) {
      return;
    }

    const echarts = await import("echarts/core");
    await import("echarts/charts").then((module) => {
      echarts.use([module.RadarChart]);
    });
    await import("echarts/components").then((module) => {
      echarts.use([
        module.TitleComponent,
        module.TooltipComponent,
      ]);
    });
    await import("echarts/renderers").then((module) => {
      echarts.use([module.CanvasRenderer]);
    });
    const values = Object.values(categoryRadar) as number[];
    const keys = Object.keys(categoryRadar) as string[];
    if (keys.length < 3) {
      return;
    }
    echartElement.classList.add("category-echarts");
    const maxNum = Math.ceil(values.reduce((prev, current) => (prev > current ? prev : current)) / 5) * 5;
    const categoryChart = echarts.init(echartElement);
    const color = document.querySelectorAll(".dark").length > 0 ? "#ccc" : "black";
    categoryChart.setOption({
      title: {
        text: sakura.translate("page.categories.radar_title", "文章分类雷达图"),
        left: "center",
        top: "25px",
        textStyle: {
          fontSize: 22,
          fontWeight: "normal",
          color: color,
        },
      },
      tooltip: {
        trigger: "item",
        textStyle: {
          align: "left",
        },
      },
      radar: [
        {
          indicator: (function () {
            var indicators = [];
            for (var i = 0; i < keys.length; i++) {
              indicators.push({ text: keys[i], max: maxNum });
            }
            return indicators;
          })(),
          name: {
            textStyle: {
              color: color,
            },
          },
          center: ["50%", "60%"],
          radius: "60%",
        },
      ],
      series: [
        {
          type: "radar",
          itemStyle: {
            color: "rgb(123,234,185)",
          },
          lineStyle: {
            color: "rgb(123,234,185)",
          },
          areaStyle: {
            color: "rgb(123,234,185)",
          },
          data: [
            {
              value: values,
              name: sakura.translate("page.categories.radar_series_title", "文章分类数量"),
            },
          ],
        },
      ],
    });
  }
}
