package run.halo.app.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.handler.file.FileHandlers;
import run.halo.app.model.dto.AttachmentDTO;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.service.OptionService;


/**
 * Test for Attachment Service implementation
 *
 * @date 2022-5-17
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    AttachmentRepository attachmentRepository;

    @Mock
    OptionService optionService;

    @Mock
    FileHandlers fileHandlers;

    @InjectMocks
    AttachmentServiceImpl attachmentService;

    @Test
    void convertToDtoNormal() {
        Attachment attachment = new Attachment();
        attachment.setName("fake-name");
        attachment.setFileKey("upload/2022/05/fake-name.png");
        attachment.setPath("upload/2022/05/fake-name.png");
        attachment.setThumbPath("upload/2022/05/fake-name-thumbnail.png");
        attachment.setType(AttachmentType.LOCAL);

        Mockito.when(optionService.getBlogBaseUrl()).thenReturn("https://mock.halo.run");
        Mockito.when(optionService.isEnabledAbsolutePath()).thenReturn(false);
        AttachmentDTO attachmentDTO = attachmentService.convertToDto(attachment);

        Assertions.assertEquals("/upload/2022/05/fake-name.png", attachmentDTO.getPath());
        Assertions.assertEquals("/upload/2022/05/fake-name-thumbnail.png",
            attachmentDTO.getThumbPath());
        Mockito.verify(optionService, Mockito.times(1)).getBlogBaseUrl();
    }

    @Test
    void convertToDtoWithChinese() {
        Attachment attachment = new Attachment();
        attachment.setName("图片");
        attachment.setFileKey("upload/2022/05/图片.png");
        attachment.setPath("upload/2022/05/图片.png");
        attachment.setThumbPath("upload/2022/05/图片-thumbnail.png");
        attachment.setType(AttachmentType.LOCAL);

        Mockito.when(optionService.getBlogBaseUrl()).thenReturn("https://mock.halo.run");
        Mockito.when(optionService.isEnabledAbsolutePath()).thenReturn(false);
        AttachmentDTO attachmentDTO = attachmentService.convertToDto(attachment);

        Assertions.assertEquals("/upload/2022/05/%E5%9B%BE%E7%89%87.png", attachmentDTO.getPath());
        Assertions.assertEquals("/upload/2022/05/%E5%9B%BE%E7%89%87-thumbnail.png",
            attachmentDTO.getThumbPath());
        Mockito.verify(optionService, Mockito.times(1)).getBlogBaseUrl();
    }

    @Test
    void convertToDtoWithSpecialChar() {
        Attachment attachment = new Attachment();
        attachment.setName("100%1#");
        attachment.setFileKey("upload/2022/05/100%1#.png");
        attachment.setPath("upload/2022/05/100%1#.png");
        attachment.setThumbPath("upload/2022/05/100%1#-thumbnail.png");
        attachment.setType(AttachmentType.LOCAL);

        Mockito.when(optionService.getBlogBaseUrl()).thenReturn("https://mock.halo.run");
        Mockito.when(optionService.isEnabledAbsolutePath()).thenReturn(false);
        AttachmentDTO attachmentDTO = attachmentService.convertToDto(attachment);

        Assertions.assertEquals("/upload/2022/05/100%251%23.png", attachmentDTO.getPath());
        Assertions.assertEquals("/upload/2022/05/100%251%23-thumbnail.png",
            attachmentDTO.getThumbPath());
        Mockito.verify(optionService, Mockito.times(1)).getBlogBaseUrl();
    }

    @Test
    void convertToDtoWithFormerVersionFile() {
        //     Attachment attachment = new Attachment();
        //     attachment.setName("之前版本上传的图片");
        //     attachment.setFileKey("upload/2022/04/之前版本上传的图片.png");
        //     attachment.setPath("upload/2022/04/之前版本上传的图片.png");
        //     attachment.setThumbPath("upload/2022\\04\\之前版本上传的图片-thumbnail.png");
        //     attachment.setType(AttachmentType.LOCAL);
        //
        //     Mockito.when(optionService.getBlogBaseUrl()).thenReturn("https://mock.halo.run");
        //     Mockito.when(optionService.isEnabledAbsolutePath()).thenReturn(false);
        //     AttachmentDTO attachmentDTO = attachmentService.convertToDto(attachment);
        //
        //     Assertions.assertEquals(
        //         "/upload/2022/04/%E4%B9%8B%E5%89%8D%E7%89%88%E6%9C%AC%E4%B8%8A%E4%BC%A0%E7%9A
        //         %84%E5"
        //             + "%9B%BE%E7%89%87.png",
        //         attachmentDTO.getPath());
        //     Assertions.assertEquals(
        //         "/upload/2022/04/%E4%B9%8B%E5%89%8D%E7%89%88%E6%9C%AC%E4%B8%8A%E4%BC%A0%E7%9A
        //         %84%E5"
        //             + "%9B%BE%E7%89%87-thumbnail.png",
        //         attachmentDTO.getThumbPath());
        //     Mockito.verify(optionService, Mockito.times(1)).getBlogBaseUrl();
    }
}