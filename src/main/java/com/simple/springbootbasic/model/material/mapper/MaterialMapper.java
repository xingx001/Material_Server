package com.simple.springbootbasic.model.material.mapper;

import com.simple.springbootbasic.basic.base.MyMapper;
import com.simple.springbootbasic.model.material.entity.WechatMedia;

import java.util.List;

/**
 * @Description TODO
 * @Author Simple
 * @Date 2018/9/17 15:05
 * @Version 1.0
 */
public interface MaterialMapper extends MyMapper<WechatMedia> {
    void deleteMedia(String mediaId);

    Integer findMediaCount();

    List<WechatMedia> selectAllByOrder();
    WechatMedia selectByMediaId(String mediaId);
}
