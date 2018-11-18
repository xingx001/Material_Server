package com.simple.springbootbasic.model.material.entity;

import lombok.Data;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author simple
 * @description 微信素材
 * @date 2018/11/6 09:33
 */
@Data
@Table(name = "t_wechat_media")
public class WechatMedia implements Serializable {
    private String mediaId;
    private String mediaName;
    private Date createTime;
    private String mediaType;
    private String mediaUrl;
    private String realUrl;
}
