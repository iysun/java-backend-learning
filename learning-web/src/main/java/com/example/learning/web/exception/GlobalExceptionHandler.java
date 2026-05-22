package com.example.learning.web.exception;

import com.example.learning.web.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception ex) {
        String message = "参数校验失败";
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validException = (MethodArgumentNotValidException) ex;
            if (validException.getBindingResult().getFieldError() != null) {
                message = validException.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        return Result.fail(400, message);
    }

    @ExceptionHandler(RpcException.class)
    public Result<Void> handleRpcException(RpcException ex) {
        log.error("Dubbo 调用失败", ex);
        return Result.fail(503, "远程服务调用失败，请确认 learning-service 已启动");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.fail(500, ex.getMessage());
    }
}
