package info.yangguo.waf.config;

import com.google.common.collect.Maps;
import info.yangguo.waf.exception.UnauthorizedException;
import info.yangguo.waf.model.Result;
import info.yangguo.waf.util.JsonUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.cors.CorsUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@ResponseBody
public class ExceptionHandlerAdvice {
    private final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.warn(ExceptionUtils.getFullStackTrace(e));
        addCorsHeader(request, response);
        Result result = new Result();
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setValue("数据格式错误");
        return result;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.warn(ExceptionUtils.getFullStackTrace(e));
        addCorsHeader(request, response);
        List<ObjectError> errorList = e.getBindingResult().getAllErrors();
        Map errMap = Maps.newHashMap();
        for (ObjectError err : errorList) {
            errMap.put(((FieldError) err).getField(), err.getDefaultMessage());
        }
        Result result = new Result();
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setValue(JsonUtil.toJson(errMap, true));

        return result;
    }


    @ExceptionHandler(ServletRequestBindingException.class)
    public Result handleServletRequestBindingException(ServletRequestBindingException e, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.error(ExceptionUtils.getFullStackTrace(e));
        addCorsHeader(request, response);
        Result result = new Result();
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setValue("Header/Body不正确");
        return result;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result handleServletRequestUnauthorizedException(UnauthorizedException e, HttpServletRequest request, HttpServletResponse response) {
        addCorsHeader(request, response);
        Result result = new Result();
        result.setCode(HttpStatus.UNAUTHORIZED.value());
        result.setValue("请登录");
        return result;
    }

    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        if (CorsUtils.isCorsRequest(request)) {
            Enumeration<String> headers = request.getHeaderNames();
            String origin = null;
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                if (header.toLowerCase().equals("origin")) {
                    origin = request.getHeader(header);
                }
            }
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
    }
}