package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.utils.HaloUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * @author : RYAN0UP
 * @date : 2017/12/19
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private LogsService logsService;

    /**
     * 刷新HaloConst
     */
    private void updateConst() {
        HaloConst.ATTACHMENTS.clear();
        HaloConst.ATTACHMENTS = attachmentService.findAllAttachments();
    }

    /**
     * 获取upload的所有图片资源并渲染页面
     *
     * @param model model
     * @return 模板路径admin/admin_attachment
     */
    @GetMapping
    public String attachments(Model model,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "18") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "attachId");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
        model.addAttribute("attachments", attachments);
        return "admin/admin_attachment";
    }

    /**
     * 跳转选择附件页面
     *
     * @param model model
     * @param page  page 当前页码
     * @return 模板路径admin/widget/_attachment-select
     */
    @GetMapping(value = "/select")
    public String selectAttachment(Model model,
                                   @RequestParam(value = "page", defaultValue = "0") Integer page,
                                   @RequestParam(value = "id",defaultValue = "none") String id,
                                   @RequestParam(value = "type",defaultValue = "normal") String type) {
        Sort sort = new Sort(Sort.Direction.DESC, "attachId");
        Pageable pageable = PageRequest.of(page, 18, sort);
        Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
        model.addAttribute("attachments", attachments);
        model.addAttribute("id", id);
        if(StringUtils.equals(type,"post")){
            return "admin/widget/_attachment-select-post";
        }
        return "admin/widget/_attachment-select";
    }

    /**
     * 上传附件
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @PostMapping(value = "/upload", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        return uploadAttachment(file, request);
    }

    /**
     * editor.md上传图片
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @PostMapping(value = "/upload/editor", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Map<String, Object> editorUpload(@RequestParam("editormd-image-file") MultipartFile file,
                                            HttpServletRequest request) {
        return uploadAttachment(file, request);
    }


    /**
     * 上传图片
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    private Map<String, Object> uploadAttachment(MultipartFile file, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (!file.isEmpty()) {
            try {
                //程序根路径，也就是/resources
                File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
                //upload的路径
                StringBuffer sbMedia = new StringBuffer("upload/");
                //获取当前年月以创建目录，如果没有该目录则创建
                sbMedia.append(HaloUtils.YEAR).append("/").append(HaloUtils.MONTH).append("/");
                File mediaPath = new File(basePath.getAbsolutePath(), sbMedia.toString());
                if (!mediaPath.exists()) {
                    mediaPath.mkdirs();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String nameWithOutSuffix = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')).replaceAll(" ","_").replaceAll(",","")+dateFormat.format(DateUtil.date())+new Random().nextInt(1000);
                String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
                String fileName = nameWithOutSuffix+"."+fileSuffix;
                file.transferTo(new File(mediaPath.getAbsoluteFile(), fileName));

                //压缩图片
                Thumbnails.of(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(fileName).toString()).size(256,256).keepAspectRatio(false).toFile(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(nameWithOutSuffix).append("_small.").append(fileSuffix).toString());

                //保存在数据库
                Attachment attachment = new Attachment();
                attachment.setAttachName(fileName);
                attachment.setAttachPath(new StringBuffer("/upload/").append(HaloUtils.YEAR).append("/").append(HaloUtils.MONTH).append("/").append(fileName).toString());
                attachment.setAttachSmallPath(new StringBuffer("/upload/").append(HaloUtils.YEAR).append("/").append(HaloUtils.MONTH).append("/").append(nameWithOutSuffix).append("_small.").append(fileSuffix).toString());
                attachment.setAttachType(file.getContentType());
                attachment.setAttachSuffix(new StringBuffer(".").append(fileSuffix).toString());
                attachment.setAttachCreated(DateUtil.date());
                attachment.setAttachSize(HaloUtils.parseSize(new File(mediaPath,fileName).length()));
                attachment.setAttachWh(HaloUtils.getImageWh(new File(mediaPath,fileName)));
                attachmentService.saveByAttachment(attachment);
                updateConst();
                log.info("上传文件[" + fileName + "]到[" + mediaPath.getAbsolutePath() + "]成功");
                logsService.saveByLogs(
                        new Logs(LogsRecord.UPLOAD_FILE, fileName, ServletUtil.getClientIP(request), DateUtil.date())
                );

                result.put("success", 1);
                result.put("message", "上传成功！");
                result.put("url", attachment.getAttachPath());
            } catch (Exception e) {
                log.error("未知错误：", e.getMessage());
                e.printStackTrace();
                result.put("success", 0);
                result.put("message", "上传失败！");
            }
        } else {
            log.error("文件不能为空");
        }
        return result;
    }

    /**
     * 处理获取附件详情的请求
     *
     * @param model    model
     * @param attachId 附件编号
     * @return 模板路径admin/widget/_attachment-detail
     */
    @GetMapping(value = "/attachment")
    public String attachmentDetail(Model model, @PathParam("attachId") Long attachId) {
        Optional<Attachment> attachment = attachmentService.findByAttachId(attachId);
        model.addAttribute("attachment", attachment.get());

        //设置选项
        model.addAttribute("options", HaloConst.OPTIONS);
        return "admin/widget/_attachment-detail";
    }

    /**
     * 移除附件的请求
     *
     * @param attachId 附件编号
     * @param request  request
     * @return true：移除附件成功，false：移除附件失败
     */
    @GetMapping(value = "/remove")
    @ResponseBody
    public JsonResult removeAttachment(@PathParam("attachId") Long attachId,
                                       HttpServletRequest request) {
        Optional<Attachment> attachment = attachmentService.findByAttachId(attachId);
        String delFileName = attachment.get().getAttachName();
        String delSmallFileName = delFileName.substring(0, delFileName.lastIndexOf('.')) + "_small" + attachment.get().getAttachSuffix();
        try {
            //删除数据库中的内容
            attachmentService.removeByAttachId(attachId);
            //刷新HaloConst变量
            updateConst();
            //删除文件
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File mediaPath = new File(basePath.getAbsolutePath(), attachment.get().getAttachPath().substring(0, attachment.get().getAttachPath().lastIndexOf('/')));
            File delFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delFileName).toString());
            File delSmallFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delSmallFileName).toString());
            if (delFile.exists() && delFile.isFile()) {
                if (delFile.delete() && delSmallFile.delete()) {
                    updateConst();
                    log.info("删除文件[" + delFileName + "]成功！");
                    logsService.saveByLogs(
                            new Logs(LogsRecord.REMOVE_FILE, delFileName, ServletUtil.getClientIP(request), DateUtil.date())
                    );
                } else {
                    log.error("删除附件[" + delFileName + "]失败！");
                    return new JsonResult(0,"删除失败！");
                }
            }
        } catch (Exception e) {
            log.error("删除附件[" + delFileName + "]失败！:", e.getMessage());
            return new JsonResult(0,"删除失败！");
        }
        return new JsonResult(1,"删除成功！");
    }
}
