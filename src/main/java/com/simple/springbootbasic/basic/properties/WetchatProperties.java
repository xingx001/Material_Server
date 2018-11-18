package com.simple.springbootbasic.basic.properties;

import lombok.Data;

/**
 * @author simple
 * @description 公众号配置文件
 * @date 2018/11/5 16:39
 */
@Data
public class WetchatProperties {
    private String appid;
    private String secret;

    private String accessTokenUrl;
    private String uploadMediaUrl;
    private String deleteMediaUrl;
    private String getMaterialUrl;
    private String getMaterialListUrl;
    private String getMaterialCountUrl;
    private String addNewsUrl;
    private String updateNewsUrl;
    private String uploadNewsImgUrl;
    private String getMenuUrl;
    private String deleteMenuUrl;
    private String createMenuUrl;
}
