package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.util.HaloUtil;
import cc.ryanc.halo.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.server.PathParam;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

    @Autowired
    private LogsService logsService;

    /**
     * 渲染主题设置页面
     *
     * @return String
     */
    @GetMapping
    public String themes(Model model){
        model.addAttribute("activeTheme",BaseController.THEME);
        if(null!=HaloConst.THEMES){
            model.addAttribute("themes",HaloConst.THEMES);
        }
        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_theme";
    }

    /**
     * 激活主题
     *
     * @param siteTheme siteTheme
     */
    @GetMapping(value = "/set")
    @ResponseBody
    public boolean activeTheme(@PathParam("siteTheme") String siteTheme,
                              HttpServletRequest request){
        try {
            //保存主题设置项在数据库
            optionsService.saveOption("theme",siteTheme);
            //设置主题
            BaseController.THEME = siteTheme;
            log.info("已将主题改变为："+siteTheme);
            logsService.saveByLogs(
                    new Logs(LogsRecord.CHANGE_THEME,"更换为"+siteTheme,HaloUtil.getIpAddr(request),HaloUtil.getDate())
            );
            return true;
        }catch (Exception e){
            log.error("主题设置失败，当前主题为："+BaseController.THEME);
            return false;
        }
    }


    /**
     * 上传主题
     *
     * @param file file
     * @return String
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadTheme(@RequestParam("file") MultipartFile file,
                               HttpServletRequest request){
        try {
            if(!file.isEmpty()) {
                //获取项目根路径
                File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
                File themePath = new File(basePath.getAbsolutePath(), new StringBuffer("templates/themes/").append(file.getOriginalFilename()).toString());
                file.transferTo(themePath);
                log.info("上传主题成功，路径：" + themePath.getAbsolutePath());
                logsService.saveByLogs(
                        new Logs(LogsRecord.UPLOAD_THEME,file.getOriginalFilename(),HaloUtil.getIpAddr(request),HaloUtil.getDate())
                );
                HaloUtil.unZip(themePath.getAbsolutePath(),new File(basePath.getAbsolutePath(),"templates/themes/").getAbsolutePath());
                HaloUtil.removeFile(themePath.getAbsolutePath());
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
     *
     * @param theme theme名称
     */
    @GetMapping(value = "/options")
    public String setting(Model model,@RequestParam("theme") String theme){
        model.addAttribute("themeDir",theme);
        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "themes/"+theme+"/module/options";
    }

    /**
     * 编辑主题
     *
     * @return string
     */
    @GetMapping(value = "/editor")
    public String editor(Model model){
        List<String> tpls = HaloUtil.getTplName(BaseController.THEME);
        model.addAttribute("tpls",tpls);
        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_theme-editor";
    }

    /**
     * 获取模板文件内容
     *
     * @return string
     */
    @GetMapping(value = "/getTpl",produces = "text/text;charset=UTF-8")
    @ResponseBody
    public String getTplContent(@RequestParam("tplName") String tplName){
        String tplContent = "";
        try {
            //获取项目根路径
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            //获取主题路径
            File themesPath = new File(basePath.getAbsolutePath(),new StringBuffer("templates/themes/").append(BaseController.THEME).append("/").append(tplName).toString());
            tplContent = HaloUtil.getFileContent(themesPath.getAbsolutePath());
        }catch (Exception e){
            log.error("获取模板文件错误："+e.getMessage());
        }
        return tplContent;
    }

    /**
     * 保存修改模板
     *
     * @param tplName tplName
     * @param tplContent tplContent
     * @return string
     */
    @PostMapping(value = "/editor/save")
    @ResponseBody
    public boolean saveTpl(@RequestParam("tplName") String tplName,
                          @RequestParam("tplContent") String tplContent){
        if(StringUtils.isBlank(tplContent)){
            return false;
        }
        try {
            //获取项目根路径
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            //获取主题路径
            File tplPath = new File(basePath.getAbsolutePath(),new StringBuffer("templates/themes/").append(BaseController.THEME).append("/").append(tplName).toString());
            byte[] tplContentByte = tplContent.getBytes("UTF-8");
            Files.write(Paths.get(tplPath.getAbsolutePath()),tplContentByte);
        }catch (Exception e){
            log.error("文件保存失败："+e.getMessage());
            return false;
        }
        return true;
    }
}
