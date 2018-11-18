package com.simple.springbootbasic.model.material.service;

import com.simple.springbootbasic.basic.base.BaseService;
import com.simple.springbootbasic.model.material.entity.WechatNews;

/**
 * @author simple
 * @description TODO
 * @date 2018/11/8 14:27
 */
public interface WechatNewsService extends BaseService<WechatNews> {
    int selectNewsCount();

    WechatNews selectByMediaId(String media_id);
}
