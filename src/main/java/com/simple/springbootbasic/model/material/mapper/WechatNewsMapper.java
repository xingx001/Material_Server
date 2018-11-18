package com.simple.springbootbasic.model.material.mapper;

import com.simple.springbootbasic.basic.base.MyMapper;
import com.simple.springbootbasic.model.material.entity.WechatNews;

import java.util.List;

/**
 * @author simple
 * @description TODO
 * @date 2018/11/8 14:26
 */
public interface WechatNewsMapper extends MyMapper<WechatNews> {
    List<WechatNews> selectAllByOrder();
    int selectNewsCount();

    WechatNews selectByMediaId(String media_id);
}
