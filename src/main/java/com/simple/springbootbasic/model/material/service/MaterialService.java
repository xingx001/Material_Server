package com.simple.springbootbasic.model.material.service;

import com.simple.springbootbasic.basic.base.BaseService;
import com.simple.springbootbasic.model.material.entity.WechatMedia;

/**
 * @author simple
 * @description TODO
 * @date 2018/11/6 09:36
 */
public interface MaterialService extends BaseService<WechatMedia> {
    void deleteMedia(String mediaId);
    Integer findMediaCount();
    WechatMedia selectByMediaId(String mediaId);
}
