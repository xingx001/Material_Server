package com.simple.springbootbasic.basic.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.springbootbasic.basic.interceptor.OriginInterceptor;
import com.simple.springbootbasic.basic.properties.SimpleProperies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;

/**
 * web配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SimpleProperies simpleProperies;

    /**
     * 配置前端json格式
     * @return
     */
    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(simpleProperies.getTimeFormat()));
        return mapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OriginInterceptor()).addPathPatterns("/**/*.json");
    }


    ///**
    // * 只处理这些请求路径后缀
    // * @param dispatcherServlet
    // * @return
    // */
    //@Bean
    //public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
    //    ServletRegistrationBean registration = new ServletRegistrationBean(
    //            dispatcherServlet);
    //    registration.addUrlMappings("/");
    //    return registration;
    //}

}
