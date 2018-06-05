package cc.ryanc.halo.config;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.Theme;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.cron.CronUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;
import java.util.Map;

/**
 * @author : RYAN0UP
 * @date : 2017/12/22
 * @version : 1.0
 */
@Slf4j
@Configuration
public class StartupConfig implements ApplicationListener<ContextRefreshedEvent>{

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private AttachmentService attachmentService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadActiveTheme();
        this.loadOptions();
        this.loadFiles();
        this.loadThemes();
        //启动定时任务
        CronUtil.start();
        log.info("定时任务启动成功！");
    }

    /**
     * 加载主题设置
     */
    private void loadActiveTheme(){
        String themeValue = optionsService.findOneOption("theme");
        if(StringUtils.isNotEmpty("themeValue") && !StringUtils.equals(themeValue,null)){
            BaseController.THEME = themeValue;
        }else{
            //以防万一
            BaseController.THEME = "anatole";
        }
    }

    /**
     * 加载设置选项
     */
    private void loadOptions(){
        Map<String,String> options = optionsService.findAllOptions();
        if(options!=null&&!options.isEmpty()){
            HaloConst.OPTIONS = options;
        }
    }

    /**
     * 加载所有文件
     */
    private void loadFiles(){
        List<Attachment> attachments = attachmentService.findAllAttachments();
        if(null!=attachments){
            HaloConst.ATTACHMENTS = attachments;
        }
    }

    /**
     * 加载所有主题
     */
    private void loadThemes(){
        HaloConst.THEMES.clear();
        List<Theme> themes = HaloUtils.getThemes();
        if(null!=themes){
            HaloConst.THEMES = themes;
        }
    }
}
