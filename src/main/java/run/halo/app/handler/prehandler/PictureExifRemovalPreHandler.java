package run.halo.app.handler.prehandler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import run.halo.app.model.properties.AttachmentProperties;
import run.halo.app.service.OptionService;

/**
 * @author eziosudo
 * @date 2022-06-16
 */
@Slf4j
@Component
@Order(value = 1)
public class PictureExifRemovalPreHandler implements FilePreHandler {

    @Autowired
    private OptionService optionService;

    @Override
    public byte[] preProcess(byte[] bytes) {
        if (!isRemoveExifEnable()) {
            return bytes;
        }
        try {
            ImageMetadata metadata = Imaging.getMetadata(bytes);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null == jpegMetadata) {
                return bytes;
            }
            final TiffImageMetadata exif = jpegMetadata.getExif();
            if (null == exif) {
                return bytes;
            }
            final TiffOutputSet outputSet = exif.getOutputSet();
            if (null == outputSet) {
                return bytes;
            }
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 OutputStream os = new BufferedOutputStream(bos)) {
                for (TiffOutputDirectory directory : outputSet.getDirectories()) {
                    for (TiffOutputField field : directory.getFields()) {
                        if (!StringUtils.equalsAnyIgnoreCase("Orientation", field.tagInfo.name)) {
                            outputSet.removeField(field.tagInfo);
                        }
                    }
                }
                new ExifRewriter().updateExifMetadataLossless(bytes, os, outputSet);
                bytes = bos.toByteArray();
                return bytes;
            }
        } catch (IllegalArgumentException e) {
            log.info("Cannot parse to image format.");
        } catch (ImageWriteException | ImageReadException | IOException e) {
            log.info("Cannot get metadata from bytes.", e);
        } catch (Exception e) {
            log.info("Cannot check or remove Exif from bytes.", e);
        }
        return bytes;
    }

    private boolean isRemoveExifEnable() {
        return optionService.getByPropertyOrDefault(
            AttachmentProperties.REMOVE_IMAGE_EXIF_ENABLE,
            Boolean.class, false);
    }

}
