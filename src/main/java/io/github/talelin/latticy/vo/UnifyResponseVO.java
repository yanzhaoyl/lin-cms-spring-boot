package io.github.talelin.latticy.vo;

import io.github.talelin.autoconfigure.bean.Code;
import io.github.talelin.autoconfigure.util.RequestUtil;
import io.github.talelin.latticy.common.util.ResponseUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

/**
 * 统一API响应结果封装
 *
 * @author pedro@TaleLin
 * @author colorful@TaleLin
 * @author Juzi@TaleLin
 */
@Data
@Slf4j
@Builder
@AllArgsConstructor
public class UnifyResponseVO<T> {

	/** 响应状态码 */
	private Integer code;

	/** 响应消息 */
	private T message;

	/** 请求方式和uri */
	private String request;
	
	

	/** SUCCESS(0, "OK", "成功"), */
	public UnifyResponseVO() {
		this.code = Code.SUCCESS.getCode();
		this.request = RequestUtil.getSimpleRequest();


	}

	public UnifyResponseVO(int code) {
		this.code = code;
		this.request = RequestUtil.getSimpleRequest();
	}

	public UnifyResponseVO(T message) {
		this.code = Code.SUCCESS.getCode();
		this.message = message;
		this.request = RequestUtil.getSimpleRequest();
	}

	public UnifyResponseVO(int code, T message) {
		this.code = code;
		this.message = message;
		this.request = RequestUtil.getSimpleRequest();
	}

	public UnifyResponseVO(T message, HttpStatus httpStatus) {
		this.code = Code.SUCCESS.getCode();
		this.message = message;
		this.request = RequestUtil.getSimpleRequest();
		ResponseUtil.setCurrentResponseHttpStatus(httpStatus.value());
	}

	public UnifyResponseVO(int code, T message, HttpStatus httpStatus) {
		this.code = code;
		this.message = message;
		this.request = RequestUtil.getSimpleRequest();
		ResponseUtil.setCurrentResponseHttpStatus(httpStatus.value());
	}

}
