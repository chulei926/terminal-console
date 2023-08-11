package com.leichu.terminal.console.config;

import com.alibaba.fastjson.JSONObject;
import com.leichu.terminal.console.common.BaseException;
import com.leichu.terminal.console.interactive.exception.GenericException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.xml.bind.ValidationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 全局异常处理.
 *
 * @author chul 2022-08-15.
 */
@RestControllerAdvice
public class GlobalExceptionResolver extends DefaultErrorAttributes {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionResolver.class);
	/**
	 * 通用的错误提示信息
	 */
	private static final String DEFAULT_ERROR_INFO = "request error！";
	@Resource
	private HttpServletRequest request;
	@Resource
	private HttpServletResponse response;

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ExceptionResult> handleException(BaseException e) {
		logger.error("{} {}{} request error! {}", request.getMethod(), request.getRequestURI(),
				StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString(),
				e.getLocalizedMessage(), e);
		HttpStatus status = e.getHttpStatus();
		if (null == status) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		String message = StringUtils.isNotBlank(e.getLocalizedMessage()) ? e.getLocalizedMessage() : status.getReasonPhrase();
		return new ResponseEntity<>(new ExceptionResult(status.value(), message), status);
	}

	@ExceptionHandler(GenericException.class)
	public ResponseEntity<ExceptionResult> handleException(GenericException e) {
		logger.error("{} {}{} request error! {}", request.getMethod(), request.getRequestURI(),
				StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString(),
				e.getLocalizedMessage(), e);
		HttpStatus status = HttpStatus.resolve(e.getCode());
		if (null == status) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		String message = StringUtils.isNotBlank(e.getLocalizedMessage()) ? e.getLocalizedMessage() : status.getReasonPhrase();
		return new ResponseEntity<>(new ExceptionResult(status.value(), message), status);
	}

	/**
	 * 验证Request Body.
	 *
	 * @param e MethodArgumentNotValidException.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResult> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		logger.error("{} {}{} request error! {}", request.getMethod(), request.getRequestURI(),
				StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString(),
				e.getLocalizedMessage(), e);
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = e.getFieldErrors().stream().map(v -> v.getField() + ":" + v.getDefaultMessage()).collect(Collectors.joining(";"));
		return new ResponseEntity<>(new ExceptionResult(status.value(), message), status);
	}

	/**
	 * 校验PathVariable/RequestParam.
	 *
	 * @param e ConstraintViolationException.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ExceptionResult> handleConstraintViolationException(ConstraintViolationException e) {
		logger.error("{} {}{} request error! {}", request.getMethod(), request.getRequestURI(),
				StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString(),
				e.getLocalizedMessage(), e);
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
		return new ResponseEntity<>(new ExceptionResult(status.value(), message), status);
	}

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
		Throwable e = this.getError(webRequest);
		String errorInfo = DEFAULT_ERROR_INFO;
		if (e != null) {
			logger.error("{}", e.getLocalizedMessage(), e);
			if (e.getCause() instanceof Error) {
				System.exit(0);
			}
		}
		if (canReturnExceptionMessage(e)) {
			errorInfo = e.getLocalizedMessage();
			if (e instanceof BindException) {
				errorInfo = Objects.requireNonNull(((BindException) e).getFieldError()).getDefaultMessage();
			}
		}
		Map<String, Object> srcErrorAttributes = super.getErrorAttributes(webRequest, options);
		String message = (String) srcErrorAttributes.get("message");
		if ("No message available".equals(message) || StringUtils.isBlank(message)) {
			message = DEFAULT_ERROR_INFO;
		}

		// 最终返回的数据与统一的异常结果对象保持一致
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("errorCode", srcErrorAttributes.get("status"));
		errorAttributes.put("errorInfo", Objects.equals(errorInfo, DEFAULT_ERROR_INFO) ? message : errorInfo);
		return errorAttributes;
	}

	/**
	 * 是否可以提取异常信息返回给前端
	 *
	 * @param e 异常对象
	 * @return 是否可提取异常信息
	 */
	private boolean canReturnExceptionMessage(Throwable e) {
		return e instanceof HttpMessageConversionException
				|| e instanceof ValidationException
				|| e instanceof MethodArgumentTypeMismatchException
				|| e instanceof IllegalStateException
				|| e instanceof ServletException
				|| e instanceof BindException;
	}


	private static class ExceptionResult {

		private Integer errorCode;
		private String errorInfo;

		public ExceptionResult(Integer errorCode, String errorInfo) {
			this.errorCode = errorCode;
			this.errorInfo = errorInfo;
		}

		public Integer getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(Integer errorCode) {
			this.errorCode = errorCode;
		}

		public String getErrorInfo() {
			return errorInfo;
		}

		public void setErrorInfo(String errorInfo) {
			this.errorInfo = errorInfo;
		}

		@Override
		public String toString() {
			return JSONObject.toJSONString(this);
		}
	}


}
