package io.github.talelin.latticy.common.exception;

import cn.hutool.core.util.StrUtil;
import io.github.talelin.autoconfigure.bean.Code;
import io.github.talelin.autoconfigure.exception.HttpException;
import io.github.talelin.latticy.common.configuration.CodeMessageConfiguration;
import io.github.talelin.latticy.vo.UnifyResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.talelin.autoconfigure.util.RequestUtil.getSimpleRequest;

/**
 * 全局异常捕获类
 * 
 * @author pedro@TaleLin
 * @author colorful@TaleLin
 * @author Juzi@TaleLin
 */

/**
 * @Order或者接口Ordered的作用是定义Spring IOC容器中Bean的执行顺序的优先级，而不是定义Bean的加载顺序，Bean的加载顺序不受@Order或Ordered接口的影响；
 * 
 *                               order的值越小，优先级越高
 *                               order如果不标注数字，默认最低优先级，因为其默认值是int最大值
 *                               该注解等同于实现Ordered接口getOrder方法，并返回数字。
 */
@Order
@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

	@Value("${spring.servlet.multipart.max-file-size:20M}")
	private String maxFileSize;

	/**
	 * @ExceptionHandler 配置的 value 指定需要拦截的异常类型
	 *                   <p>
	 *                   HttpException请求发生错误(输入的网址错误or请求的方式错误or权限不够)
	 *                   </p>
	 * @param exception 捕获的异常
	 * @param request   请求
	 * @param response  响应
	 * 
	 * @return UnifyResponseVO 统一API响应结果封装
	 */
	@ExceptionHandler({ HttpException.class })
	public UnifyResponseVO<String> processException(HttpException exception, HttpServletRequest request,
			HttpServletResponse response)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

//		log.debug("当前线程为 {}，请求方法为 {}，请求路径为：{}", Thread.currentThread().getName(), request.getMethod(),
//				request.getRequestURL().toString());
//		

		UnifyResponseVO<String> unifyResponse = new UnifyResponseVO<String>();

		unifyResponse.setRequest(getSimpleRequest(request));

		int code = exception.getCode();

		log.debug("异常状态码:{}", code);

		boolean defaultMessage = exception.ifDefaultMessage();
		
		log.debug("是否是默认消息:{}", defaultMessage);

		unifyResponse.setCode(code);
		
		response.setStatus(exception.getHttpCode());
		log.debug("返回的状态码:{}", exception.getHttpCode());
		
		String errorMessage = CodeMessageConfiguration.getMessage(code);
		
		
		if (StrUtil.isBlank(errorMessage) || !defaultMessage) {
			unifyResponse.setMessage(exception.getMessage());
			log.error("", exception);
		} else {
			unifyResponse.setMessage(errorMessage);
			log.error("", exception.getClass().getConstructor(int.class, String.class).newInstance(code, errorMessage));
		}
		return unifyResponse;
	}

	/**
	 * @ExceptionHandler 配置的 value 指定需要拦截的异常类型
	 *                   <p>
	 *                   ConstraintViolationException 格式化验证未通过
	 *                   </p>
	 * @param exception 捕获的异常
	 * @param request   请求
	 * @param response  响应
	 * 
	 * @return UnifyResponseVO 统一API响应结果封装
	 */
	@ExceptionHandler({ ConstraintViolationException.class })
	public UnifyResponseVO<Map<String, Object>> processException(ConstraintViolationException exception,
			HttpServletRequest request, HttpServletResponse response) {
		log.error("", exception);
		Map<String, Object> msg = new HashMap<>();
		exception.getConstraintViolations().forEach(constraintViolation -> {
			String template = constraintViolation.getMessage();
			String path = constraintViolation.getPropertyPath().toString();
			msg.put(StrUtil.toUnderlineCase(path), template);
		});
		UnifyResponseVO<Map<String, Object>> unifyResponse = new UnifyResponseVO<Map<String, Object>>();
		unifyResponse.setRequest(getSimpleRequest(request));
		unifyResponse.setMessage(msg);
		unifyResponse.setCode(Code.PARAMETER_ERROR.getCode());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return unifyResponse;
	}

	/**
	 * @ExceptionHandler 配置的 value 指定需要拦截的异常类型
	 *                   <p>
	 *                   NoHandlerFoundException 请求路径错误
	 *                   </p>
	 * @param exception 捕获的异常
	 * @param request   请求
	 * @param response  响应
	 * 
	 * @return UnifyResponseVO 统一API响应结果封装
	 */
	@ExceptionHandler({ NoHandlerFoundException.class })
	public UnifyResponseVO<String> processException(NoHandlerFoundException exception, HttpServletRequest request,
			HttpServletResponse response) {
		log.error("", exception);
		UnifyResponseVO<String> unifyResponse = new UnifyResponseVO<String>();
		unifyResponse.setRequest(getSimpleRequest(request));
		String message = CodeMessageConfiguration.getMessage(10025);
		if (StrUtil.isBlank(message)) {
			unifyResponse.setMessage(exception.getMessage());
		} else {
			unifyResponse.setMessage(message);
		}
		unifyResponse.setCode(Code.NOT_FOUND.getCode());
		response.setStatus(HttpStatus.NOT_FOUND.value());
		return unifyResponse;
	}

	/**
	 * MissingServletRequestParameterException
	 */
	@ExceptionHandler({ MissingServletRequestParameterException.class })
	public UnifyResponseVO<String> processException(MissingServletRequestParameterException exception,
			HttpServletRequest request, HttpServletResponse response) {
		log.error("", exception);
		UnifyResponseVO<String> result = new UnifyResponseVO<String>();
		result.setRequest(getSimpleRequest(request));

		String errorMessage = CodeMessageConfiguration.getMessage(10150);
		if (StrUtil.isBlank(errorMessage)) {
			result.setMessage(exception.getMessage());
		} else {
			result.setMessage(errorMessage + exception.getParameterName());
		}
		result.setCode(Code.PARAMETER_ERROR.getCode());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return result;
	}

	/**
	 * MethodArgumentTypeMismatchException
	 */
	@ExceptionHandler({ MethodArgumentTypeMismatchException.class })
	public UnifyResponseVO<String> processException(MethodArgumentTypeMismatchException exception,
			HttpServletRequest request, HttpServletResponse response) {
		log.error("", exception);
		UnifyResponseVO<String> result = new UnifyResponseVO<String>();
		result.setRequest(getSimpleRequest(request));
		String errorMessage = CodeMessageConfiguration.getMessage(10160);
		if (StrUtil.isBlank(errorMessage)) {
			result.setMessage(exception.getMessage());
		} else {
			result.setMessage(exception.getValue() + errorMessage);
		}
		result.setCode(Code.PARAMETER_ERROR.getCode());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return result;
	}

	/**
	 * ServletException
	 */
	@ExceptionHandler({ ServletException.class })
	public UnifyResponseVO<String> processException(ServletException exception, HttpServletRequest request,
			HttpServletResponse response) {
		log.error("", exception);
		UnifyResponseVO<String> result = new UnifyResponseVO<String>();
		result.setRequest(getSimpleRequest(request));
		result.setMessage(exception.getMessage());
		result.setCode(Code.FAIL.getCode());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return result;
	}

	/**
	 * MethodArgumentNotValidException
	 */
	@ExceptionHandler({ MethodArgumentNotValidException.class })
	public UnifyResponseVO<Map<String, Object>> processException(MethodArgumentNotValidException exception,
			HttpServletRequest request, HttpServletResponse response) {
		log.error("", exception);
		BindingResult bindingResult = exception.getBindingResult();
		List<ObjectError> errors = bindingResult.getAllErrors();
		Map<String, Object> msg = new HashMap<>();
		errors.forEach(error -> {
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				msg.put(StrUtil.toUnderlineCase(fieldError.getField()), fieldError.getDefaultMessage());
			} else {
				msg.put(StrUtil.toUnderlineCase(error.getObjectName()), error.getDefaultMessage());
			}
		});
		UnifyResponseVO<Map<String, Object>> result = new UnifyResponseVO<Map<String, Object>>();
		result.setRequest(getSimpleRequest(request));
		result.setMessage(msg);
		result.setCode(Code.PARAMETER_ERROR.getCode());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return result;
	}

	/**
	 * HttpMessageNotReadableException
	 */
	@ExceptionHandler({ HttpMessageNotReadableException.class })
	public UnifyResponseVO<String> processException(HttpMessageNotReadableException exception,
			HttpServletRequest request, HttpServletResponse response) {
		log.error("", exception);
		UnifyResponseVO<String> result = new UnifyResponseVO<String>();
		result.setRequest(getSimpleRequest(request));
		String errorMessage = CodeMessageConfiguration.getMessage(10170);
		if (StrUtil.isBlank(errorMessage)) {
			result.setMessage(exception.getMessage());
		} else {
			result.setMessage(errorMessage);
		}
		result.setCode(Code.PARAMETER_ERROR.getCode());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return result;
	}

	/**
	 * TypeMismatchException
	 */
	@ExceptionHandler({ TypeMismatchException.class })
	public UnifyResponseVO<String> processException(TypeMismatchException exception, HttpServletRequest request,
			HttpServletResponse response) {
		log.error("", exception);
		UnifyResponseVO<String> result = new UnifyResponseVO<String>();
		result.setRequest(getSimpleRequest(request));
		result.setMessage(exception.getMessage());
		result.setCode(Code.PARAMETER_ERROR.getCode());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return result;
	}

	/**
	 * MaxUploadSizeExceededException
	 */
	@ExceptionHandler({ MaxUploadSizeExceededException.class })
	public UnifyResponseVO<String> processException(MaxUploadSizeExceededException exception,
			HttpServletRequest request, HttpServletResponse response) {
		log.error("", exception);
		UnifyResponseVO<String> result = new UnifyResponseVO<String>();
		result.setRequest(getSimpleRequest(request));
		String errorMessage = CodeMessageConfiguration.getMessage(10180);
		if (StrUtil.isBlank(errorMessage)) {
			result.setMessage(exception.getMessage());
		} else {
			log.info("这是个未知的私有字符串:{}", maxFileSize);
			result.setMessage(errorMessage + maxFileSize);
		}
		result.setCode(Code.FILE_TOO_LARGE.getCode());
		response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
		return result;
	}

	/**
	 * Exception
	 */
	@ExceptionHandler({ Exception.class })
	public UnifyResponseVO<String> processException(Exception exception, HttpServletRequest request,
			HttpServletResponse response) {
		
		log.error("", exception);
		
		UnifyResponseVO<String> result = new UnifyResponseVO<String>();
		result.setRequest(getSimpleRequest(request));
		result.setMessage(Code.INTERNAL_SERVER_ERROR.getZhDescription());
		result.setCode(Code.INTERNAL_SERVER_ERROR.getCode());
		
		
		
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return result;
	}
}
