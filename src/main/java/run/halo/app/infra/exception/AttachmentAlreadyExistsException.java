package run.halo.app.infra.exception;

import org.springframework.web.server.ServerWebInputException;

/**
 * AttachmentAlreadyExistsException accepts filename parameter as detail message arguments.
 *
 * @author johnniang
 */
public class AttachmentAlreadyExistsException extends ServerWebInputException {

    public AttachmentAlreadyExistsException(String filename) {
        super("File " + filename + " already exists.", null, null, null, new Object[] {filename});
    }

}
