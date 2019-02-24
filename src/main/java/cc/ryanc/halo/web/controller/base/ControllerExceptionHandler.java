package cc.ryanc.halo.web.controller.base;

import cc.ryanc.halo.exception.HaloException;
import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.utils.ExceptionUtils;
import cc.ryanc.halo.utils.ValidationUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.Map;

/**
 * Exception handler of controller.
 *
 * @author johnniang
 */
@RestControllerAdvice
public class ControllerExceptionHandler {

    private final Logger log = Logger.getLogger(getClass());

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public JsonResult handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        JsonResult jsonResult = handleBaseException(e);
        if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            jsonResult = handleBaseException(e.getCause());
        }
        jsonResult.setMsg("Failed to validate request parameter");
        return jsonResult;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public JsonResult handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        JsonResult jsonResult = handleBaseException(e);
        jsonResult.setMsg(String.format("Missing request parameter, required %s type %s parameter", e.getParameterType(), e.getParameterName()));
        return jsonResult;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public JsonResult handleConstraintViolationException(ConstraintViolationException e) {
        JsonResult jsonResult = handleBaseException(e);
        jsonResult.setCode(HttpStatus.BAD_REQUEST.value());
        jsonResult.setMsg("Filed validation error");
        jsonResult.setResult(e.getConstraintViolations());
        return jsonResult;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public JsonResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        JsonResult jsonResult = handleBaseException(e);
        jsonResult.setCode(HttpStatus.BAD_REQUEST.value());
        jsonResult.setMsg("Filed validation error");
        Map<String, String> errMap = ValidationUtils.mapWithFieldError(e.getBindingResult().getFieldErrors());
        jsonResult.setResult(errMap);
        return jsonResult;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public JsonResult handleNoHandlerFoundException(NoHandlerFoundException e) {
        JsonResult jsonResult = handleBaseException(e);
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        jsonResult.setCode(status.value());
        jsonResult.setMsg(status.getReasonPhrase());
        return jsonResult;
    }

    @ExceptionHandler(HaloException.class)
    public ResponseEntity<JsonResult> handleHaloException(HaloException e) {
        JsonResult jsonResult = handleBaseException(e);
        jsonResult.setCode(e.getStatus().value());
        jsonResult.setResult(e.getErrorData());
        return new ResponseEntity<>(jsonResult, e.getStatus());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public JsonResult handleGlobalException(Exception e) {
        JsonResult jsonResult = handleBaseException(e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        jsonResult.setCode(status.value());
        jsonResult.setMsg(status.getReasonPhrase());
        return jsonResult;
    }

    private JsonResult handleBaseException(Throwable t) {
        Assert.notNull(t, "Throwable must not be null");

        log.error("Captured an exception", t);

        JsonResult jsonResult = new JsonResult();
        jsonResult.setMsg(t.getMessage());

        if (log.isDebugEnabled()) {
            jsonResult.setDevMsg(ExceptionUtils.getStackTrace(t));
        }

        return jsonResult;
    }

}
