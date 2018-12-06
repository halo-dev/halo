package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Attachment;
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
public interface AttachmentService {

    /**
     * 添加附件信息
     *
     * @param attachment attachment
     * @return Attachment
     */
    Attachment save(Attachment attachment);

    /**
     * 查询所有附件信息
     *
     * @return List
     */
    List<Attachment> findAll();

    /**
     * 查询所有附件，分页
     *
     * @param pageable pageable
     * @return Page
     */
    Page<Attachment> findAll(Pageable pageable);

    /**
     * 根据编号查询
     *
     * @param attachId attachId
     * @return Attachment
     */
    Optional<Attachment> findByAttachId(Long attachId);

    /**
     * 根据编号移除
     *
     * @param attachId attachId
     * @return Attachment
     */
    Attachment remove(Long attachId);

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
     * 获取附件总数
     *
     * @return Long
     */
    Long getCount();
}
