/*
 * Copyright (c) 2018-2019.‭‭‭‭‭‭‭‭‭‭‭‭[zuoqinggang] www.pingfangushi.com
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package cn.smallbun.scaffold.framework.exception.handler;


import cn.smallbun.scaffold.framework.common.result.ApiRestResult;
import cn.smallbun.scaffold.framework.security.exception.HaveNotAuthorityException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;

import static cn.smallbun.scaffold.framework.exception.Exceptions.getStackTraceAsString;
import static cn.smallbun.scaffold.framework.exception.enums.Exception.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


/**
 * 全局异常处理
 *
 * @author SanLi
 * Created by 2689170096@qq.com on 2018/10/9
 */
@RestControllerAdvice
public class GlobalExceptionHandler implements Serializable {


	/**
	 * 默认异常处理
	 * @param e   {@link Exception}
	 * @return {@link Object}
	 */
	@ExceptionHandler(value = Exception.class)
	public Object defaultErrorHandler(Exception e) {
		//返回Rest错误
		return ApiRestResult.err(getStackTraceAsString(e)).build();
	}

	/**
	 * 参数验证异常
	 *
	 * @param exception {@link MethodArgumentNotValidException}
	 * @return {@link Object}
	 */
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Object methodArgumentNotValidHandler(MethodArgumentNotValidException exception) {
		return baseBindException(exception.getBindingResult());
	}

	/**
	 * 参数验证异常
	 *
	 * @param exception {@link BindException}
	 * @return {@link Object}
	 */
	@ExceptionHandler(value = BindException.class)
	public Object bindExceptionValidHandler(BindException exception) {
		return baseBindException(exception.getBindingResult());
	}


	/**
	 * 参数绑定异常公共处理方法
	 *
	 * @param bindingResult {@link BindingResult}
	 * @return {@link Object}
	 */
	private Object baseBindException(BindingResult bindingResult) {
		StringBuilder buffer = new StringBuilder();
		//解析原错误信息，封装后返回，此处返回非法的字段名称，原始值，错误信息
		for (FieldError error : bindingResult.getFieldErrors()) {
			buffer.append(error.getDefaultMessage()).append(",");
		}
		return ApiRestResult.err(buffer.substring(0, buffer.length() - 1), EX900000.getCode()).build();
	}


	/**
	 * 用户名或密码错误 BadCredentialsException
	 *
	 * @param e   {@link BadCredentialsException}
	 * @return {@link ApiRestResult}
	 */
	@ExceptionHandler(value = BadCredentialsException.class)
	public ApiRestResult badCredentialsException(BadCredentialsException e) {
		//返回Rest错误
		return new ApiRestResult<>().message(EX000101.getMessage()).status(EX000101.getCode())
				.result(getStackTraceAsString(e)).build();
	}

	/**
	 * 用户不存在
	 *
	 * @param e   {@link UsernameNotFoundException}
	 * @return {@link ApiRestResult}
	 */
	@ExceptionHandler(value = UsernameNotFoundException.class)
	public ApiRestResult usernameNotFoundException(UsernameNotFoundException e) {
		//返回Rest错误
		return new ApiRestResult<>().message(EX000107.getMessage()).status(EX000107.getCode())
				.result(getStackTraceAsString(e)).build();
	}

	/**
	 * 用户被锁定
	 *
	 * @param e   {@link LockedException}
	 * @return {@link ApiRestResult}
	 */
	@ExceptionHandler(value = LockedException.class)
	public ApiRestResult lockedException(LockedException e) {
		//返回Rest错误
		return new ApiRestResult<>().message(EX000103.getMessage()).status(getStackTraceAsString(e))
				.result(EX000103.getMessage()).build();
	}

	/**
	 * 如果由于没有足够信任凭据而拒绝身份验证请求，则抛出该异常。
	 * @param e {@link InsufficientAuthenticationException} e
	 * @return {@link ApiRestResult}
	 */
	@ExceptionHandler(value = InsufficientAuthenticationException.class)
	public ResponseEntity<ApiRestResult> insufficientAuthenticationException(InsufficientAuthenticationException e) {
		return new ResponseEntity<>(new ApiRestResult<>().message(e.getMessage()).result(getStackTraceAsString(e))
				.status(String.valueOf(UNAUTHORIZED.value())), UNAUTHORIZED);
	}

	/**
	 * 没有访问权限
	 *
	 * @param e   {@link AccessDeniedException}
	 * @return {@link ApiRestResult}
	 */
	@ExceptionHandler(value = AccessDeniedException.class)
	public ResponseEntity<ApiRestResult> methodAccessDeniedException(AccessDeniedException e) {
		return new ResponseEntity<>(new ApiRestResult<>().message(e.getMessage()).result(getStackTraceAsString(e))
				.status(String.valueOf(FORBIDDEN.value())), FORBIDDEN);
	}

	/**
	 * InternalAuthenticationServiceException
	 *
	 * @param e   {@link InternalAuthenticationServiceException}
	 * @return {@link ApiRestResult}
	 */
	@ExceptionHandler(value = InternalAuthenticationServiceException.class)
	public ApiRestResult internalAuthenticationServiceException(InternalAuthenticationServiceException e) {
		//没有权限
		if (e.getCause() instanceof HaveNotAuthorityException) {
			return new ApiRestResult<>().message(EX000105.getMessage()).result(getStackTraceAsString(e))
					.status(String.valueOf(EX000105.getCode()));
		}
		//禁用
		if (e.getCause() instanceof DisabledException) {
			return new ApiRestResult<>().message(EX000104.getMessage()).status(EX000104.getCode())
					.result(getStackTraceAsString(e)).build();
		}
		return new ApiRestResult<>().message(EX000106.getMessage()).result(getStackTraceAsString(e))
				.status(String.valueOf(EX000106.getCode()));
	}
}
