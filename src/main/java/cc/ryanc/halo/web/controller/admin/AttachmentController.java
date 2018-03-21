package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.dto.RespStatus;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.util.HaloUtil;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/12/19
 * @version : 1.0
 * description: 附件
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
    private void updateConst(){
        HaloConst.ATTACHMENTS.clear();
        HaloConst.ATTACHMENTS = attachmentService.findAllAttachments();
    }

    /**
     * 获取upload的所有图片资源并渲染页面
     *
     * @param model model
     * @return String
     */
    @GetMapping
    public String attachments(Model model,
                              @RequestParam(value = "page",defaultValue = "0") Integer page,
                              @RequestParam(value = "size",defaultValue = "18") Integer size){
        Sort sort = new Sort(Sort.Direction.DESC,"attachId");
        Pageable pageable = new PageRequest(page,size,sort);
        Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
        model.addAttribute("attachments",attachments);

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_attachment";
    }

    /**
     * 跳转选择附件页面
     *
     * @param model model
     * @param page page
     * @return string
     */
    @GetMapping(value = "/select")
    public String selectAttachment(Model model,
                                   @RequestParam(value = "page",defaultValue = "0") Integer page,
                                   @RequestParam(value = "id") String id){
        Sort sort = new Sort(Sort.Direction.DESC,"attachId");
        Pageable pageable = new PageRequest(page,18,sort);
        Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
        model.addAttribute("attachments",attachments);
        model.addAttribute("id",id);

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/widget/_attachment-select";
    }


    /**
     * 上传文件
     *
     * @param file file
     */
    @PostMapping(value = "/upload",produces = { "application/json;charset=UTF-8" })
    @ResponseBody
    public boolean uploadAttachment(@RequestParam("file") MultipartFile file,
                                    HttpServletRequest request){
        if(!file.isEmpty()){
            try{
                File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
                StringBuffer sbMedia = new StringBuffer("upload/");
                sbMedia.append(HaloUtil.YEAR).append("/").append(HaloUtil.MONTH).append("/");
                File mediaPath = new File(basePath.getAbsolutePath(),sbMedia.toString());
                if(!mediaPath.exists()){
                    mediaPath.mkdirs();
                }
                file.transferTo(new File(mediaPath.getAbsoluteFile(),file.getOriginalFilename()));
                String fileName = file.getOriginalFilename();
                String nameWithOutSuffix = fileName.substring(0,fileName.lastIndexOf('.'));
                String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.')+1);

                //保存在数据库
                Attachment attachment = new Attachment();
                attachment.setAttachName(fileName);
                attachment.setAttachPath(new StringBuffer("/upload/").append(HaloUtil.YEAR).append("/").append(HaloUtil.MONTH).append("/").append(fileName).toString());
                attachment.setAttachSmallPath(new StringBuffer("/upload/").append(HaloUtil.YEAR).append("/").append(HaloUtil.MONTH).append("/").append(nameWithOutSuffix).append("_small.").append(fileSuffix).toString());
                attachment.setAttachType(file.getContentType());
                attachment.setAttachSuffix(new StringBuffer(".").append(fileSuffix).toString());
                attachment.setAttachCreated(HaloUtil.getDate());
                attachmentService.saveByAttachment(attachment);

                //剪裁图片
                HaloUtil.cutCenterImage(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(fileName).toString(),new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(nameWithOutSuffix).append("_small.").append(fileSuffix).toString(),500,500,fileSuffix);

                updateConst();
                log.info("上传文件["+file.getOriginalFilename()+"]到["+mediaPath.getAbsolutePath()+"]成功");
                logsService.saveByLogs(
                        new Logs(LogsRecord.UPLOAD_FILE,file.getOriginalFilename(),HaloUtil.getIpAddr(request),HaloUtil.getDate())
                );
                return true;
            }catch (Exception e){
                log.error("未知错误："+e.getMessage());
            }
        }else {
            log.error("文件不能为空");
        }
        return false;
    }

    /**
     * 处理获取附件详情的请求
     *
     * @param attachId attachId
     * @return string
     */
    @GetMapping(value = "/attachment")
    public String attachmentDetail(Model model,@PathParam("attachId") Long attachId){
        Optional<Attachment> attachment = attachmentService.findByAttachId(attachId);
        model.addAttribute("attachment",attachment.get());

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/widget/_attachment-detail";
    }

    /**
     * 移除附件的请求
     *
     * @param attachId attachId
     * @return string
     */
    @GetMapping(value = "/remove")
    @ResponseBody
    public String removeAttachment(@PathParam("attachId") Long attachId,
                                   HttpServletRequest request){
        Optional<Attachment> attachment = attachmentService.findByAttachId(attachId);
        String delFileName = attachment.get().getAttachName();
        String delSmallFileName = delFileName.substring(0,delFileName.lastIndexOf('.'))+"_small"+attachment.get().getAttachSuffix();
        try {
            //删除数据库中的内容
            attachmentService.removeByAttachId(attachId);
            //刷新HaloConst变量
            updateConst();
            //删除文件
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File mediaPath = new File(basePath.getAbsolutePath(),attachment.get().getAttachPath().substring(0,attachment.get().getAttachPath().lastIndexOf('/')));
            File delFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delFileName).toString());
            File delSmallFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delSmallFileName).toString());
            if(delFile.exists() && delFile.isFile()){
                if(delFile.delete()&&delSmallFile.delete()){
                    updateConst();
                    log.info("删除文件["+delFileName+"]成功！");
                    logsService.saveByLogs(
                            new Logs(LogsRecord.REMOVE_FILE,delFileName,HaloUtil.getIpAddr(request),HaloUtil.getDate())
                    );
                }else{
                    log.error("删除附件["+delFileName+"]失败！");
                    return RespStatus.ERROR;
                }
            }
        }catch (Exception e){
            log.error("删除附件["+delFileName+"]失败！"+e.getMessage());
            return RespStatus.ERROR;
        }
        return RespStatus.SUCCESS;
    }
}
