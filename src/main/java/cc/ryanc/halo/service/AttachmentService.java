package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <pre>
 *     附件业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/10
 */
public interface AttachmentService extends CrudService<Attachment, Long> {

    /**
     * 上传转发
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    Map<String, String> upload(MultipartFile file, HttpServletRequest request);

    /**
     * 原生服务器上传
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    Map<String, String> attachUpload(MultipartFile file, HttpServletRequest request);

    /**
     * 七牛云上传
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    Map<String, String> attachQiNiuUpload(MultipartFile file, HttpServletRequest request);

    /**
     * 又拍云上传
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    Map<String, String> attachUpYunUpload(MultipartFile file, HttpServletRequest request);

    /**
     * 阿里云上传
     * @param file      文件
     * @param request   request对象
     * @return
     */
    Map<String, String> attachAliyunUpload(MultipartFile file, HttpServletRequest request);

    /**
     * 七牛云删除附件
     *
     * @param key key
     * @return boolean
     */
    boolean deleteQiNiuAttachment(String key);

    /**
     * 又拍云删除附件
     *
     * @param fileName fileName
     * @return boolean
     */
    boolean deleteUpYunAttachment(String fileName);

    /**
     * 阿里云删除附件
     * @param fileName 文件名称
     * @return
     */
    boolean deleteAliyunAttachment(String fileName);

}
