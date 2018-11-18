package com.simple.springbootbasic.utils;

import com.oracle.tools.packager.Log;
import com.simple.springbootbasic.basic.properties.SimpleProperies;
import com.simple.springbootbasic.basic.properties.WetchatProperties;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author simple
 * @description 微信相关工具类
 * @date 2018/11/5 18:59
 */
@Component
public class WechatUtils {

    @Autowired
    private SimpleProperies simpleProperies;

    @Autowired
    private RedisUtil redisUtil;

    public  String getAccessToken(){
        WetchatProperties wechat = simpleProperies.getWechat();
        String accessToken="";
        if(redisUtil.hasKey(wechat.getAppid())){
            accessToken= (String) redisUtil.get(wechat.getAppid());
        }else{
            String result = RequestUtils.doGet(String.format(wechat.getAccessTokenUrl(), "client_credential",wechat.getAppid(), wechat.getSecret()));
            System.out.println(result);
            Log.info(result);
            JSONObject jsonObject=new JSONObject(result);
            if(jsonObject.has("access_token")){
                accessToken=jsonObject.getString("access_token");
                redisUtil.set(wechat.getAppid(),accessToken,3600);
            }
        }
        return accessToken;
    }
}
