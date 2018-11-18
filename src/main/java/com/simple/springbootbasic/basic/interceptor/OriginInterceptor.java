package com.simple.springbootbasic.basic.interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description 处理跨域
 * @Author Simple
 * @Date 2018/9/30 11:06
 * @Version 1.0
 */
public class OriginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        return true;
    }

}
