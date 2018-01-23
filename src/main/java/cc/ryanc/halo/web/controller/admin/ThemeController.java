package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.RespStatus;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.util.HaloUtil;
import cc.ryanc.halo.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.websocket.server.PathParam;
import java.io.File;

/**
 * @author : RYAN0UP
 * @date : 2017/12/16
 * @version : 1.0
 * description: 主题控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/themes")
public class ThemeController extends BaseController{

    @Autowired
    private OptionsService optionsService;

    /**
     * 渲染主题设置页面
     * @return String
     */
    @GetMapping
    public String themes(Model model){
        model.addAttribute("activeTheme",BaseController.THEME);
        if(null!=HaloConst.THEMES){
            model.addAttribute("themes",HaloConst.THEMES);
        }
        model.addAttribute("options", HaloConst.OPTIONS);
        return "admin/theme";
    }

    /**
     * 激活主题
     * @param siteTheme siteTheme
     */
    @GetMapping(value = "/set")
    @ResponseBody
    public String activeTheme(@PathParam("siteTheme") String siteTheme){
        try {
            //保存主题设置项在数据库
            optionsService.saveOption("theme",siteTheme);
            //设置主题
            BaseController.THEME = siteTheme;
            log.info("已将主题改变为："+siteTheme);
            return RespStatus.SUCCESS;
        }catch (Exception e){
            log.error("主题设置失败，当前主题为："+BaseController.THEME);
            return RespStatus.ERROR;
        }
    }


    /**
     * 上传主题
     * @param file file
     * @return String
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadTheme(@RequestParam("file") MultipartFile file){
        try {
            if(!file.isEmpty()) {
                //获取项目根路径
                File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
                File themePath = new File(basePath.getAbsolutePath(), "templates/themes/" + file.getOriginalFilename());
                file.transferTo(themePath);
                log.info("上传主题成功，路径：" + themePath.getAbsolutePath());
                HaloUtil.unZip(themePath.getAbsolutePath(),new File(basePath.getAbsolutePath(),"templates/themes/").getAbsolutePath());
                HaloUtil.removeFile(themePath.getAbsolutePath());
                System.out.println(themePath);
                HaloConst.THEMES.clear();
                HaloConst.THEMES = HaloUtil.getThemes();
                return true;
            }else{
                log.error("上传失败，没有选择文件");
            }
        }catch (Exception e){
            log.error("上传失败："+e.getMessage());
        }
        return false;
    }

    /**
     * 跳转到主题设置
     * @param theme theme名称
     */
    @GetMapping(value = "/options")
    public String setting(Model model,@RequestParam("theme") String theme){
        model.addAttribute("options",HaloConst.OPTIONS);
        model.addAttribute("themeDir",theme);
        return "themes/"+theme+"/module/options";
    }
}
