package com.simple.springbootbasic.model.material.controller;

import com.simple.springbootbasic.basic.properties.SimpleProperies;
import com.simple.springbootbasic.utils.RequestUtils;
import com.simple.springbootbasic.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author simple
 * @description 自定义菜单管理
 * @date 2018/11/14 11:08
 */
@RestController
@RequestMapping("/model/menu")
public class WechatMenuController {

    @Autowired
    private SimpleProperies simpleProperies;

    @Autowired
    private WechatUtils wechatUtils;
    /**
     * 获取菜单列表
     * @return
     */
    @RequestMapping("/getMenu.json")
    public String getMenu(){
        String url=String.format(simpleProperies.getWechat().getGetMenuUrl(),wechatUtils.getAccessToken());
        String result = RequestUtils.doGet(url);
        return result;
    }
}
