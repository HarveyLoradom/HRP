package com.hrp.common.exception;

import com.hrp.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有模块的异常，返回统一的响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常（@Valid注解）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数验证失败: {} - {}", request.getRequestURI(), message);
        return Result.error(400, "参数验证失败: " + message);
    }

    /**
     * 处理参数绑定异常（@ModelAttribute注解）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数绑定失败: {} - {}", request.getRequestURI(), message);
        return Result.error(400, "参数绑定失败: " + message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = "缺少必需的请求参数: " + e.getParameterName();
        logger.warn("缺少请求参数: {} - {}", request.getRequestURI(), message);
        return Result.error(400, message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = "参数类型不匹配: " + e.getName() + " 应为 " + e.getRequiredType().getSimpleName();
        logger.warn("参数类型不匹配: {} - {}", request.getRequestURI(), message);
        return Result.error(400, message);
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = "不支持的请求方法: " + e.getMethod() + "，支持的方法: " + String.join(", ", e.getSupportedMethods());
        logger.warn("请求方法不支持: {} - {}", request.getRequestURI(), message);
        return Result.error(405, message);
    }

    /**
     * 处理请求体不可读异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        logger.warn("请求体解析失败: {} - {}", request.getRequestURI(), e.getMessage());
        String message = "请求体格式错误，请检查JSON格式";
        if (e.getMessage() != null && e.getMessage().contains("JSON")) {
            message = "JSON格式错误: " + e.getMessage();
        }
        return Result.error(400, message);
    }

    /**
     * 处理404异常（请求路径不存在）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        logger.warn("请求路径不存在: {} - {}", request.getRequestURI(), e.getRequestURL());
        return Result.error(404, "请求路径不存在: " + e.getRequestURL());
    }

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        logger.error("数据库访问异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        String message = "数据库操作失败";
        // 可以根据具体的异常类型返回更详细的错误信息
        if (e.getCause() instanceof SQLException) {
            SQLException sqlException = (SQLException) e.getCause();
            // 处理常见的SQL异常
            if (sqlException.getMessage().contains("Duplicate entry")) {
                message = "数据已存在，不能重复添加";
            } else if (sqlException.getMessage().contains("foreign key constraint")) {
                message = "存在关联数据，无法删除";
            } else if (sqlException.getMessage().contains("doesn't exist")) {
                message = "数据不存在";
            }
        }
        return Result.error(500, message);
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleSQLException(SQLException e, HttpServletRequest request) {
        logger.error("SQL异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        String message = "数据库操作失败";
        if (e.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，不能重复添加";
        } else if (e.getMessage().contains("foreign key constraint")) {
            message = "存在关联数据，无法删除";
        }
        return Result.error(500, message);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统内部错误：空指针异常");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("非法参数异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(400, "参数错误: " + e.getMessage());
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        logger.warn("非法状态异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(400, "操作状态错误: " + e.getMessage());
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }
}




