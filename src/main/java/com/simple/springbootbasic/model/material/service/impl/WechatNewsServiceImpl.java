package com.simple.springbootbasic.model.material.service.impl;

import com.github.pagehelper.PageHelper;
import com.simple.springbootbasic.basic.base.impl.BaseServiceImpl;
import com.simple.springbootbasic.model.material.entity.WechatMedia;
import com.simple.springbootbasic.model.material.entity.WechatNews;
import com.simple.springbootbasic.model.material.mapper.WechatNewsMapper;
import com.simple.springbootbasic.model.material.service.WechatNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author simple
 * @description TODO
 * @date 2018/11/8 14:28
 */
@Service
public class WechatNewsServiceImpl extends BaseServiceImpl<WechatNews> implements WechatNewsService {
    @Autowired
    private WechatNewsMapper wechatNewsMapper;

    @Override
    public List<WechatNews> pageList(Integer page, Integer size) {
        //分页核心代码
        PageHelper.startPage(page, size);
        List<WechatNews> wechatMediaList = wechatNewsMapper.selectAllByOrder();
        return wechatMediaList;
    }

    @Override
    public int selectNewsCount() {
        return wechatNewsMapper.selectNewsCount();
    }

    @Override
    public WechatNews selectByMediaId(String media_id) {
        return wechatNewsMapper.selectByMediaId(media_id);
    }
}
