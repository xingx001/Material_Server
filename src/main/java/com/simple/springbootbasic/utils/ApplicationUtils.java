package com.simple.springbootbasic.utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author simple
 * @description 工具类
 * @date 2018/11/6 09:53
 */
public class ApplicationUtils {
    /**
     * 获取格式化的uuid
     * @return
     */
    public static String getUUIDFormat(){
        return UUID.randomUUID().toString().replace("-","");
    }

    /**
     * 原始uuid
     * @return
     */
    public static String getUUID(){
        return UUID.randomUUID().toString();
    }

    /**
     * 将json字符串转json对象
     * @param result
     * @return
     */
    public static JSONObject formatJSONObject(String result){
        return new JSONObject(result);
    }

    public static Date formatTenDate(Long time){
        return new Date(time*1000L);
    }

    /**
     * 提取富文本中的图片地址  并上传到微信换取url
     * @param htmlStr
     * @return
     */
    public static Set<String> getImgStr(String htmlStr) {
        Set<String> pics = new HashSet<String>();
        String img = "";
        Pattern p_image;
        Matcher m_image;
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }


    /**
     * 获取格式化日期 例如2017-01-01
     * @param date 当前时间
     * @param format 日期格式
     */
    public static String getFormatDate(Date date,String format){
        SimpleDateFormat f=new SimpleDateFormat(format);
        return f.format(date);
    }

}
