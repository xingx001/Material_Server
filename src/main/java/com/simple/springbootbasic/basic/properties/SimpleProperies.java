package com.simple.springbootbasic.basic.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局配置文件
 */
@Component
@ConfigurationProperties(prefix = "simple")
@Data
public class SimpleProperies {
    private String example;
    private String errorPage;
    private String notFoundPage;
    private Boolean openAopLog;
    private String salt;
    private String unauthorizedPage;
    private String timeFormat;
    private String uuid;
    private String uploadPath;
    private String uploadPrefix;



    //shiro相关配置
    private ShiroProperties shiro = new ShiroProperties();

    //公众号相关配置
    private WetchatProperties wechat=new WetchatProperties();
}
