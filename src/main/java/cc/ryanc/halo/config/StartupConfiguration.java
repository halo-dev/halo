package cc.ryanc.halo.config;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.Theme;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.util.HaloUtil;
import cc.ryanc.halo.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
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
 * description: Springboot启动后
 */
@Slf4j
@Configuration
public class StartupConfiguration implements ApplicationListener<ContextRefreshedEvent>{

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
    }

    /**
     * 加载主题设置
     */
    private void loadActiveTheme(){
        try {
            String themeValue = optionsService.findOneOption("theme");
            if(HaloUtil.isNotNull(themeValue)){
                BaseController.THEME = themeValue;
            }
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
    }

    /**
     * 加载设置选项
     */
    private void loadOptions(){
        try{
            Map<String,String> options = optionsService.findAllOptions();
            if(options!=null&&!options.isEmpty()){
                HaloConst.OPTIONS = options;
            }
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
    }

    /**
     * 加载所有文件
     */
    private void loadFiles(){
        try {
            List<Attachment> attachments = attachmentService.findAllAttachments();
            if(null!=attachments){
                HaloConst.ATTACHMENTS = attachments;
            }
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
    }

    /**
     * 加载所有主题
     */
    private void loadThemes(){
        try{
            HaloConst.THEMES.clear();
            List<Theme> themes = HaloUtil.getThemes();
            if(null!=themes){
                HaloConst.THEMES = themes;
            }
        }catch (Exception e){
            log.error("加载主题失败："+e.getMessage());
        }
    }
}
