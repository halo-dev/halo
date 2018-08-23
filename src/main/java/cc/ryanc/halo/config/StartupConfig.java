package cc.ryanc.halo.config;

import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.Theme;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.cron.CronUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     应用启动的时候所执行的方法
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/22
 */
@Slf4j
@Configuration
public class StartupConfig implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private OptionsService optionsService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        this.loadActiveTheme();
        this.loadOptions();
        this.loadThemes();
        this.loadOwo();
        //启动定时任务
        CronUtil.start();
        log.info("The scheduled task starts successfully!");
    }

    /**
     * 加载主题设置
     */
    private void loadActiveTheme() {
        String themeValue = optionsService.findOneOption(BlogPropertiesEnum.THEME.getProp());
        if (StringUtils.isNotEmpty(themeValue) && !StringUtils.equals(themeValue, null)) {
            BaseController.THEME = themeValue;
        } else {
            //以防万一
            BaseController.THEME = "anatole";
        }
    }

    /**
     * 加载设置选项
     */
    private void loadOptions() {
        Map<String, String> options = optionsService.findAllOptions();
        if (options != null && !options.isEmpty()) {
            HaloConst.OPTIONS = options;
        }
    }

    /**
     * 加载所有主题
     */
    private void loadThemes() {
        HaloConst.THEMES.clear();
        List<Theme> themes = HaloUtils.getThemes();
        if (null != themes) {
            HaloConst.THEMES = themes;
        }
    }

    /**
     * 加载OwO表情
     */
    private void loadOwo() {
        Map<String, String> map = new HashMap<>(135);
        map.put("@[nico]", "<img src='/static/plugins/OwO/paopao/nico.png' alt='nico.png' style='vertical-align: middle;'>");
        map.put("@[OK]", "<img src='/static/plugins/OwO/paopao/OK.png' alt='OK.png' style='vertical-align: middle;'>");
        map.put("@[what]", "<img src='/static/plugins/OwO/paopao/what.png' alt='what.png' style='vertical-align: middle;'>");
        map.put("@[三道杠]", "<img src='/static/plugins/OwO/paopao/三道杠.png' alt='三道杠.png' style='vertical-align: middle;'>");
        map.put("@[不高兴]", "<img src='/static/plugins/OwO/paopao/不高兴.png' alt='不高兴.png' style='vertical-align: middle;'>");
        map.put("@[乖]", "<img src='/static/plugins/OwO/paopao/乖.png' alt='乖.png' style='vertical-align: middle;'>");
        map.put("@[你懂的]", "<img src='/static/plugins/OwO/paopao/你懂的.png' alt='你懂的.png' style='vertical-align: middle;'>");
        map.put("@[便便]", "<img src='/static/plugins/OwO/paopao/便便.png' alt='便便.png' style='vertical-align: middle;'>");
        map.put("@[冷]", "<img src='/static/plugins/OwO/paopao/冷.png' alt='冷.png' style='vertical-align: middle;'>");
        map.put("@[勉强]", "<img src='/static/plugins/OwO/paopao/勉强.png' alt='勉强.png' style='vertical-align: middle;'>");
        map.put("@[吃瓜]", "<img src='/static/plugins/OwO/paopao/吃瓜.png' alt='吃瓜.png' style='vertical-align: middle;'>");
        map.put("@[吃翔]", "<img src='/static/plugins/OwO/paopao/吃翔.png' alt='吃翔.png' style='vertical-align: middle;'>");
        map.put("@[吐]", "<img src='/static/plugins/OwO/paopao/吐.png' alt='吐.png' style='vertical-align: middle;'>");
        map.put("@[吐舌]", "<img src='/static/plugins/OwO/paopao/吐舌.png' alt='吐舌.png' style='vertical-align: middle;'>");
        map.put("@[呀咩爹]", "<img src='/static/plugins/OwO/paopao/呀咩爹.png' alt='呀咩爹.png' style='vertical-align: middle;'>");
        map.put("@[呵呵]", "<img src='/static/plugins/OwO/paopao/呵呵.png' alt='呵呵.png' style='vertical-align: middle;'>");
        map.put("@[呼]", "<img src='/static/plugins/OwO/paopao/呼.png' alt='呼.png' style='vertical-align: middle;'>");
        map.put("@[咦]", "<img src='/static/plugins/OwO/paopao/咦.png' alt='咦.png' style='vertical-align: middle;'>");
        map.put("@[哈哈]", "<img src='/static/plugins/OwO/paopao/哈哈.png' alt='哈哈.png' style='vertical-align: middle;'>");
        map.put("@[啊]", "<img src='/static/plugins/OwO/paopao/啊.png' alt='啊.png' style='vertical-align: middle;'>");
        map.put("@[喷]", "<img src='/static/plugins/OwO/paopao/喷.png' alt='喷.png' style='vertical-align: middle;'>");
        map.put("@[嘚瑟]", "<img src='/static/plugins/OwO/paopao/嘚瑟.png' alt='嘚瑟.png' style='vertical-align: middle;'>");
        map.put("@[大拇指]", "<img src='/static/plugins/OwO/paopao/大拇指.png' alt='大拇指.png' style='vertical-align: middle;'>");
        map.put("@[太开心]", "<img src='/static/plugins/OwO/paopao/太开心.png' alt='太开心.png' style='vertical-align: middle;'>");
        map.put("@[太阳]", "<img src='/static/plugins/OwO/paopao/太阳.png' alt='太阳.png' style='vertical-align: middle;'>");
        map.put("@[委屈]", "<img src='/static/plugins/OwO/paopao/委屈.png' alt='委屈.png' style='vertical-align: middle;'>");
        map.put("@[小乖]", "<img src='/static/plugins/OwO/paopao/小乖.png' alt='小乖.png' style='vertical-align: middle;'>");
        map.put("@[小红脸]", "<img src='/static/plugins/OwO/paopao/小红脸.png' alt='小红脸.png' style='vertical-align: middle;'>");
        map.put("@[开心]", "<img src='/static/plugins/OwO/paopao/开心.png' alt='开心.png' style='vertical-align: middle;'>");
        map.put("@[弱]", "<img src='/static/plugins/OwO/paopao/弱.png' alt='弱.png' style='vertical-align: middle;'>");
        map.put("@[彩虹]", "<img src='/static/plugins/OwO/paopao/彩虹.png' alt='彩虹.png' style='vertical-align: middle;'>");
        map.put("@[心碎]", "<img src='/static/plugins/OwO/paopao/心碎.png' alt='心碎.png' style='vertical-align: middle;'>");
        map.put("@[怒]", "<img src='/static/plugins/OwO/paopao/怒.png' alt='怒.png' style='vertical-align: middle;'>");
        map.put("@[惊哭]", "<img src='/static/plugins/OwO/paopao/惊哭.png' alt='惊哭.png' style='vertical-align: middle;'>");
        map.put("@[惊恐]", "<img src='/static/plugins/OwO/paopao/惊恐.png' alt='惊恐.png' style='vertical-align: middle;'>");
        map.put("@[惊讶]", "<img src='/static/plugins/OwO/paopao/惊讶.png' alt='惊讶.png' style='vertical-align: middle;'>");
        map.put("@[懒得理]", "<img src='/static/plugins/OwO/paopao/懒得理.png' alt='懒得理.png' style='vertical-align: middle;'>");
        map.put("@[手纸]", "<img src='/static/plugins/OwO/paopao/手纸.png' alt='手纸.png' style='vertical-align: middle;'>");
        map.put("@[挖鼻]", "<img src='/static/plugins/OwO/paopao/挖鼻.png' alt='挖鼻.png' style='vertical-align: middle;'>");
        map.put("@[捂嘴笑]", "<img src='/static/plugins/OwO/paopao/捂嘴笑.png' alt='捂嘴笑.png' style='vertical-align: middle;'>");
        map.put("@[星星月亮]", "<img src='/static/plugins/OwO/paopao/星星月亮.png' alt='星星月亮.png' style='vertical-align: middle;'>");
        map.put("@[汗]", "<img src='/static/plugins/OwO/paopao/汗.png' alt='汗.png' style='vertical-align: middle;'>");
        map.put("@[沙发]", "<img src='/static/plugins/OwO/paopao/沙发.png' alt='沙发.png' style='vertical-align: middle;'>");
        map.put("@[泪]", "<img src='/static/plugins/OwO/paopao/泪.png' alt='泪.png' style='vertical-align: middle;'>");
        map.put("@[滑稽]", "<img src='/static/plugins/OwO/paopao/滑稽.png' alt='滑稽.png' style='vertical-align: middle;'>");
        map.put("@[灯泡]", "<img src='/static/plugins/OwO/paopao/灯泡.png' alt='灯泡.png' style='vertical-align: middle;'>");
        map.put("@[爱心]", "<img src='/static/plugins/OwO/paopao/爱心.png' alt='爱心.png' style='vertical-align: middle;'>");
        map.put("@[犀利]", "<img src='/static/plugins/OwO/paopao/犀利.png' alt='犀利.png' style='vertical-align: middle;'>");
        map.put("@[狂汗]", "<img src='/static/plugins/OwO/paopao/狂汗.png' alt='狂汗.png' style='vertical-align: middle;'>");
        map.put("@[玫瑰]", "<img src='/static/plugins/OwO/paopao/玫瑰.png' alt='玫瑰.png' style='vertical-align: middle;'>");
        map.put("@[生气]", "<img src='/static/plugins/OwO/paopao/生气.png' alt='生气.png' style='vertical-align: middle;'>");
        map.put("@[疑问]", "<img src='/static/plugins/OwO/paopao/疑问.png' alt='疑问.png' style='vertical-align: middle;'>");
        map.put("@[真棒]", "<img src='/static/plugins/OwO/paopao/真棒.png' alt='真棒.png' style='vertical-align: middle;'>");
        map.put("@[睡觉]", "<img src='/static/plugins/OwO/paopao/睡觉.png' alt='睡觉.png' style='vertical-align: middle;'>");
        map.put("@[礼物]", "<img src='/static/plugins/OwO/paopao/礼物.png' alt='礼物.png' style='vertical-align: middle;'>");
        map.put("@[笑尿]", "<img src='/static/plugins/OwO/paopao/笑尿.png' alt='笑尿.png' style='vertical-align: middle;'>");
        map.put("@[笑眼]", "<img src='/static/plugins/OwO/paopao/笑眼.png' alt='笑眼.png' style='vertical-align: middle;'>");
        map.put("@[红领巾]", "<img src='/static/plugins/OwO/paopao/红领巾.png' alt='红领巾.png' style='vertical-align: middle;'>");
        map.put("@[胜利]", "<img src='/static/plugins/OwO/paopao/胜利.png' alt='胜利.png' style='vertical-align: middle;'>");
        map.put("@[花心]", "<img src='/static/plugins/OwO/paopao/花心.png' alt='花心.png' style='vertical-align: middle;'>");
        map.put("@[茶杯]", "<img src='/static/plugins/OwO/paopao/茶杯.png' alt='茶杯.png' style='vertical-align: middle;'>");
        map.put("@[药丸]", "<img src='/static/plugins/OwO/paopao/药丸.png' alt='药丸.png' style='vertical-align: middle;'>");
        map.put("@[蛋糕]", "<img src='/static/plugins/OwO/paopao/蛋糕.png' alt='蛋糕.png' style='vertical-align: middle;'>");
        map.put("@[蜡烛]", "<img src='/static/plugins/OwO/paopao/蜡烛.png' alt='蜡烛.png' style='vertical-align: middle;'>");
        map.put("@[鄙视]", "<img src='/static/plugins/OwO/paopao/鄙视.png' alt='鄙视.png' style='vertical-align: middle;'>");
        map.put("@[酷]", "<img src='/static/plugins/OwO/paopao/酷.png' alt='酷.png' style='vertical-align: middle;'>");
        map.put("@[酸爽]", "<img src='/static/plugins/OwO/paopao/酸爽.png' alt='酸爽.png' style='vertical-align: middle;'>");
        map.put("@[钱]", "<img src='/static/plugins/OwO/paopao/钱.png' alt='钱.png' style='vertical-align: middle;'>");
        map.put("@[钱币]", "<img src='/static/plugins/OwO/paopao/钱币.png' alt='钱币.png' style='vertical-align: middle;'>");
        map.put("@[阴险]", "<img src='/static/plugins/OwO/paopao/阴险.png' alt='阴险.png' style='vertical-align: middle;'>");
        map.put("@[音乐]", "<img src='/static/plugins/OwO/paopao/音乐.png' alt='音乐.png' style='vertical-align: middle;'>");
        map.put("@[香蕉]", "<img src='/static/plugins/OwO/paopao/香蕉.png' alt='香蕉.png' style='vertical-align: middle;'>");
        map.put("@[黑线]", "<img src='/static/plugins/OwO/paopao/黑线.png' alt='黑线.png' style='vertical-align: middle;'>");
        map.put("@(不出所料)", "<img src='/static/plugins/OwO/alu/不出所料.png' alt='不出所料.png' style='vertical-align: middle;'>");
        map.put("@(不说话)", "<img src='/static/plugins/OwO/alu/不说话.png' alt='不说话.png' style='vertical-align: middle;'>");
        map.put("@(不高兴)", "<img src='/static/plugins/OwO/alu/不高兴.png' alt='不高兴.png' style='vertical-align: middle;'>");
        map.put("@(中刀)", "<img src='/static/plugins/OwO/alu/中刀.png' alt='中刀.png' style='vertical-align: middle;'>");
        map.put("@(中指)", "<img src='/static/plugins/OwO/alu/中指.png' alt='中指.png' style='vertical-align: middle;'>");
        map.put("@(中枪)", "<img src='/static/plugins/OwO/alu/中枪.png' alt='中枪.png' style='vertical-align: middle;'>");
        map.put("@(亲亲)", "<img src='/static/plugins/OwO/alu/亲亲.png' alt='亲亲.png' style='vertical-align: middle;'>");
        map.put("@(便便)", "<img src='/static/plugins/OwO/alu/便便.png' alt='便便.png' style='vertical-align: middle;'>");
        map.put("@(傻笑)", "<img src='/static/plugins/OwO/alu/傻笑.png' alt='傻笑.png' style='vertical-align: middle;'>");
        map.put("@(内伤)", "<img src='/static/plugins/OwO/alu/内伤.png' alt='内伤.png' style='vertical-align: middle;'>");
        map.put("@(击掌)", "<img src='/static/plugins/OwO/alu/击掌.png' alt='击掌.png' style='vertical-align: middle;'>");
        map.put("@(口水)", "<img src='/static/plugins/OwO/alu/口水.png' alt='口水.png' style='vertical-align: middle;'>");
        map.put("@(吐)", "<img src='/static/plugins/OwO/alu/吐.png' alt='吐.png' style='vertical-align: middle;'>");
        map.put("@(吐舌)", "<img src='/static/plugins/OwO/alu/吐舌.png' alt='吐舌.png' style='vertical-align: middle;'>");
        map.put("@(吐血倒地)", "<img src='/static/plugins/OwO/alu/吐血倒地.png' alt='吐血倒地.png' style='vertical-align: middle;'>");
        map.put("@(呲牙)", "<img src='/static/plugins/OwO/alu/呲牙.png' alt='呲牙.png' style='vertical-align: middle;'>");
        map.put("@(咽气)", "<img src='/static/plugins/OwO/alu/咽气.png' alt='咽气.png' style='vertical-align: middle;'>");
        map.put("@(哭泣)", "<img src='/static/plugins/OwO/alu/哭泣.png' alt='哭泣.png' style='vertical-align: middle;'>");
        map.put("@(喜极而泣)", "<img src='/static/plugins/OwO/alu/喜极而泣.png' alt='喜极而泣.png' style='vertical-align: middle;'>");
        map.put("@(喷水)", "<img src='/static/plugins/OwO/alu/喷水.png' alt='喷水.png' style='vertical-align: middle;'>");
        map.put("@(喷血)", "<img src='/static/plugins/OwO/alu/喷血.png' alt='喷血.png' style='vertical-align: middle;'>");
        map.put("@(坐等)", "<img src='/static/plugins/OwO/alu/坐等.png' alt='坐等.png' style='vertical-align: middle;'>");
        map.put("@(大囧)", "<img src='/static/plugins/OwO/alu/大囧.png' alt='大囧.png' style='vertical-align: middle;'>");
        map.put("@(害羞)", "<img src='/static/plugins/OwO/alu/害羞.png' alt='害羞.png' style='vertical-align: middle;'>");
        map.put("@(小怒)", "<img src='/static/plugins/OwO/alu/小怒.png' alt='小怒.png' style='vertical-align: middle;'>");
        map.put("@(小眼睛)", "<img src='/static/plugins/OwO/alu/小眼睛.png' alt='小眼睛.png' style='vertical-align: middle;'>");
        map.put("@(尴尬)", "<img src='/static/plugins/OwO/alu/尴尬.png' alt='尴尬.png' style='vertical-align: middle;'>");
        map.put("@(得意)", "<img src='/static/plugins/OwO/alu/得意.png' alt='得意.png' style='vertical-align: middle;'>");
        map.put("@(惊喜)", "<img src='/static/plugins/OwO/alu/惊喜.png' alt='惊喜.png' style='vertical-align: middle;'>");
        map.put("@(想一想)", "<img src='/static/plugins/OwO/alu/想一想.png' alt='想一想.png' style='vertical-align: middle;'>");
        map.put("@(愤怒)", "<img src='/static/plugins/OwO/alu/愤怒.png' alt='愤怒.png' style='vertical-align: middle;'>");
        map.put("@(扇耳光)", "<img src='/static/plugins/OwO/alu/扇耳光.png' alt='扇耳光.png' style='vertical-align: middle;'>");
        map.put("@(投降)", "<img src='/static/plugins/OwO/alu/投降.png' alt='投降.png' style='vertical-align: middle;'>");
        map.put("@(抠鼻)", "<img src='/static/plugins/OwO/alu/抠鼻.png' alt='抠鼻.png' style='vertical-align: middle;'>");
        map.put("@(抽烟)", "<img src='/static/plugins/OwO/alu/抽烟.png' alt='抽烟.png' style='vertical-align: middle;'>");
        map.put("@(无奈)", "<img src='/static/plugins/OwO/alu/无奈.png' alt='无奈.png' style='vertical-align: middle;'>");
        map.put("@(无所谓)", "<img src='/static/plugins/OwO/alu/无所谓.png' alt='无所谓.png' style='vertical-align: middle;'>");
        map.put("@(无语)", "<img src='/static/plugins/OwO/alu/无语.png' alt='无语.png' style='vertical-align: middle;'>");
        map.put("@(暗地观察)", "<img src='/static/plugins/OwO/alu/暗地观察.png' alt='暗地观察.png' style='vertical-align: middle;'>");
        map.put("@(期待)", "<img src='/static/plugins/OwO/alu/期待.png' alt='期待.png' style='vertical-align: middle;'>");
        map.put("@(欢呼)", "<img src='/static/plugins/OwO/alu/欢呼.png' alt='欢呼.png' style='vertical-align: middle;'>");
        map.put("@(汗)", "<img src='/static/plugins/OwO/alu/汗.png' alt='汗.png' style='vertical-align: middle;'>");
        map.put("@(深思)", "<img src='/static/plugins/OwO/alu/深思.png' alt='深思.png' style='vertical-align: middle;'>");
        map.put("@(狂汗)", "<img src='/static/plugins/OwO/alu/狂汗.png' alt='狂汗.png' style='vertical-align: middle;'>");
        map.put("@(献花)", "<img src='/static/plugins/OwO/alu/献花.png' alt='献花.png' style='vertical-align: middle;'>");
        map.put("@(献黄瓜)", "<img src='/static/plugins/OwO/alu/献黄瓜.png' alt='献黄瓜.png' style='vertical-align: middle;'>");
        map.put("@(皱眉)", "<img src='/static/plugins/OwO/alu/皱眉.png' alt='皱眉.png' style='vertical-align: middle;'>");
        map.put("@(看不见)", "<img src='/static/plugins/OwO/alu/看不见.png' alt='看不见.png' style='vertical-align: middle;'>");
        map.put("@(看热闹)", "<img src='/static/plugins/OwO/alu/看热闹.png' alt='看热闹.png' style='vertical-align: middle;'>");
        map.put("@(肿包)", "<img src='/static/plugins/OwO/alu/肿包.png' alt='肿包.png' style='vertical-align: middle;'>");
        map.put("@(脸红)", "<img src='/static/plugins/OwO/alu/脸红.png' alt='脸红.png' style='vertical-align: middle;'>");
        map.put("@(蜡烛)", "<img src='/static/plugins/OwO/alu/蜡烛.png' alt='蜡烛.png' style='vertical-align: middle;'>");
        map.put("@(装大款)", "<img src='/static/plugins/OwO/alu/装大款.png' alt='装大款.png' style='vertical-align: middle;'>");
        map.put("@(观察)", "<img src='/static/plugins/OwO/alu/观察.png' alt='观察.png' style='vertical-align: middle;'>");
        map.put("@(赞一个)", "<img src='/static/plugins/OwO/alu/赞一个.png' alt='赞一个.png' style='vertical-align: middle;'>");
        map.put("@(邪恶)", "<img src='/static/plugins/OwO/alu/邪恶.png' alt='邪恶.png' style='vertical-align: middle;'>");
        map.put("@(锁眉)", "<img src='/static/plugins/OwO/alu/锁眉.png' alt='锁眉.png' style='vertical-align: middle;'>");
        map.put("@(长草)", "<img src='/static/plugins/OwO/alu/长草.png' alt='长草.png' style='vertical-align: middle;'>");
        map.put("@(阴暗)", "<img src='/static/plugins/OwO/alu/阴暗.png' alt='阴暗.png' style='vertical-align: middle;'>");
        map.put("@(高兴)", "<img src='/static/plugins/OwO/alu/高兴.png' alt='高兴.png' style='vertical-align: middle;'>");
        map.put("@(黑线)", "<img src='/static/plugins/OwO/alu/黑线.png' alt='黑线.png' style='vertical-align: middle;'>");
        map.put("@(鼓掌)", "<img src='/static/plugins/OwO/alu/鼓掌.png' alt='鼓掌.png' style='vertical-align: middle;'>");
        HaloConst.OWO = map;
    }
}
