package com.simple.springbootbasic.model.material.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author simple
 * @description TODO
 * @date 2018/11/8 11:37
 */
@Table(name = "t_wechat_news")
@Data
public class WechatNews implements Serializable {
    @Id
    private String mediaId;
    private String parentMediaId;
    private String newsTitle;
    private String newsAuthor;
    private String newsDigest;
    private String content;
    private Date createTime;
    private Date updateTime;
    private String thumbMediaId;
    private String contentSourceUrl;
    private Integer showCoverPic;
    private String newsUrl;
    private String thumbMediaUrl;
    private Integer needOpenComment;
    private Integer onlyFansCanComment;
    private String thumbMediaLocalUrl;


}
