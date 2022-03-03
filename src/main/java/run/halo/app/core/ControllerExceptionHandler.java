package run.halo.app.core;

import java.util.Map;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import run.halo.app.exception.AbstractHaloException;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.utils.ExceptionUtils;
import run.halo.app.utils.ValidationUtils;

/**
 * Exception handler of controller.
 *
 * @author johnniang
 */
@RestControllerAdvice(value = {"run.halo.app.controller.admin.api",
    "run.halo.app.controller.content.api"})
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleDataIntegrityViolationException(
        DataIntegrityViolationException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            baseResponse = handleBaseException(e.getCause());
        }
        baseResponse.setMessage("字段验证错误，请完善后重试！");
        return baseResponse;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setMessage(
            String.format("请求字段缺失, 类型为 %s，名称为 %s", e.getParameterType(), e.getParameterName()));
        return baseResponse;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        BaseResponse<Map<String, String>> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponse.setMessage("字段验证错误，请完善后重试！");
        baseResponse.setData(ValidationUtils.mapWithValidError(e.getConstraintViolations()));
        return baseResponse;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        BaseResponse<Map<String, String>> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponse.setMessage("字段验证错误，请完善后重试！");
        Map<String, String> errMap =
            ValidationUtils.mapWithFieldError(e.getBindingResult().getFieldErrors());
        baseResponse.setData(errMap);
        return baseResponse;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return baseResponse;
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public BaseResponse<?> handleHttpMediaTypeNotAcceptableException(
        HttpMediaTypeNotAcceptableException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        return baseResponse;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponse.setMessage("缺失请求主体");
        return baseResponse;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public BaseResponse<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        baseResponse.setStatus(status.value());
        baseResponse.setMessage(status.getReasonPhrase());
        return baseResponse;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleUploadSizeExceededException(MaxUploadSizeExceededException e) {
        BaseResponse<Object> response = handleBaseException(e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage("当前请求超出最大限制：" + e.getMaxUploadSize() + " bytes");
        return response;
    }

    @ExceptionHandler(AbstractHaloException.class)
    public ResponseEntity<BaseResponse<?>> handleHaloException(AbstractHaloException e) {
        BaseResponse<Object> baseResponse = handleBaseException(e);
        baseResponse.setStatus(e.getStatus().value());
        baseResponse.setData(e.getErrorData());
        return new ResponseEntity<>(baseResponse, e.getStatus());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<?> handleGlobalException(Exception e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        baseResponse.setStatus(status.value());
        baseResponse.setMessage(status.getReasonPhrase());
        return baseResponse;
    }

    private <T> BaseResponse<T> handleBaseException(Throwable t) {
        Assert.notNull(t, "Throwable must not be null");

        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.setMessage(t.getMessage());

        log.error("Captured an exception:", t);

        if (log.isDebugEnabled()) {
            baseResponse.setDevMessage(ExceptionUtils.getStackTrace(t));
        }

        return baseResponse;
    }

}

