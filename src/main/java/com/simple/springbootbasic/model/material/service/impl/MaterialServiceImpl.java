package com.simple.springbootbasic.model.material.service.impl;

import com.github.pagehelper.PageHelper;
import com.simple.springbootbasic.basic.base.impl.BaseServiceImpl;
import com.simple.springbootbasic.model.material.entity.WechatMedia;
import com.simple.springbootbasic.model.material.mapper.MaterialMapper;
import com.simple.springbootbasic.model.material.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author simple
 * @description TODO
 * @date 2018/11/6 09:36
 */
@Service
public class MaterialServiceImpl extends BaseServiceImpl<WechatMedia> implements MaterialService {

    @Autowired
    private MaterialMapper materialMapper;

    @Override
    public List<WechatMedia> pageList(Integer page, Integer size) {
        //分页核心代码
        PageHelper.startPage(page, size);
        List<WechatMedia> wechatMediaList = materialMapper.selectAllByOrder();
        return wechatMediaList;
    }

    @Override
    public void deleteMedia(String mediaId) {
        materialMapper.deleteMedia(mediaId);
    }

    @Override
    public Integer findMediaCount() {
        return materialMapper.findMediaCount();
    }

    @Override
    public WechatMedia selectByMediaId(String mediaId) {
        return materialMapper.selectByMediaId(mediaId);
    }
}
