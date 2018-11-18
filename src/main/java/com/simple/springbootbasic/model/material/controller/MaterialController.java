package com.simple.springbootbasic.model.material.controller;

import com.simple.springbootbasic.basic.properties.SimpleProperies;
import com.simple.springbootbasic.basic.properties.WetchatProperties;
import com.simple.springbootbasic.basic.result.ResponseCode;
import com.simple.springbootbasic.basic.result.ResultJsonData;
import com.simple.springbootbasic.basic.result.ResultJsonUtils;
import com.simple.springbootbasic.model.material.entity.WechatMedia;
import com.simple.springbootbasic.model.material.entity.WechatNews;
import com.simple.springbootbasic.model.material.service.MaterialService;
import com.simple.springbootbasic.model.material.service.WechatNewsService;
import com.simple.springbootbasic.utils.ApplicationUtils;
import com.simple.springbootbasic.utils.RedisUtil;
import com.simple.springbootbasic.utils.RequestUtils;
import com.simple.springbootbasic.utils.WechatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author simple
 * @description 微信素材管理接口
 * @date 2018/11/5 16:15
 */
@RestController
@RequestMapping("/model/material")
@Slf4j
public class MaterialController {

    @Autowired
    private SimpleProperies simpleProperies;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private WechatUtils wechatUtils;

    @Autowired
    private WechatNewsService wechatNewsService;


    /**
     * 获取accessToken
     *
     * @return
     */
    @RequestMapping("/get_accessToken.json")
    public ResultJsonData getAccessToken() {
        WetchatProperties wechat = simpleProperies.getWechat();
        String accessToken = "";
        if (redisUtil.hasKey(wechat.getAppid())) {
            accessToken = (String) redisUtil.get(wechat.getAppid());
        } else {
            String result = RequestUtils.doGet(String.format(wechat.getAccessTokenUrl(), "client_credential", wechat.getAppid(), wechat.getSecret()));
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("access_token")) {
                accessToken = jsonObject.getString("access_token");
                redisUtil.set(wechat.getAppid(), accessToken, 3600);
            }
        }
        return new ResultJsonUtils().success((Object) accessToken);
    }

    /**
     * 上传素材
     *
     * @param type  素材类型
     * @param media form-data
     * @return
     */
    @RequestMapping("/upload_media.json")
    public ResultJsonData uploadMedia(String type, MultipartFile[] media, HttpServletRequest request) {
        //获取项目根路径
        String root = request.getServletContext().getRealPath("/") + "upload";
        //获取当天日期
        SimpleDateFormat simple = new SimpleDateFormat(simpleProperies.getUploadPrefix());
        String date = simple.format(new Date());
        String filePath = root + File.separator + date;
        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //上传到本地
        String url = String.format(simpleProperies.getWechat().getUploadMediaUrl(), wechatUtils.getAccessToken(), type);
        String mediaId="";
        String mediaUrl="";
        String localUrl="";
        for (MultipartFile file : media) {
            String fileName = file.getOriginalFilename();
            String suffixType = fileName.substring(fileName.lastIndexOf("."));
            String newFileName = ApplicationUtils.getUUIDFormat() + suffixType;
            File item = new File(dirFile, newFileName);
            try {
                file.transferTo(item);
                String dataPath = simpleProperies.getUploadPath() + File.separator + date + File.separator + newFileName;
                localUrl=dataPath;
                //保存到数据库
                WechatMedia wechatMedia = new WechatMedia();
                wechatMedia.setCreateTime(new Date());
                wechatMedia.setMediaName(fileName);
                wechatMedia.setMediaType(type);
                wechatMedia.setMediaUrl(dataPath);
                //上传到微信服务器
                String result = RequestUtils.f_upload(item, url, fileName);
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("media_id")) {
                    mediaId=jsonObject.getString("media_id");
                    wechatMedia.setMediaId(mediaId);
                }else{
                    return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(),"上传失败");
                }
                if (jsonObject.has("url")) {
                    mediaUrl=jsonObject.getString("url");
                    wechatMedia.setRealUrl(mediaUrl);
                }
                materialService.save(wechatMedia);
                log.info("upload success:[{}]", fileName);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("upload error:[{}]", fileName);
            }

        }
        //返回media_id 和 local_url , media_url
        Map<String,String> resData=new HashMap<>();
        resData.put("mediaId",mediaId);
        resData.put("mediaUrl",mediaUrl);
        resData.put("localUrl",localUrl);
        return new ResultJsonUtils().success(resData);
    }

    /**
     * 删除素材 同时会删除公众号下的素材
     *
     * @param medias
     * @return
     */
    @RequestMapping("/delete_media.json")
    public ResultJsonData deleteMedia(String[] medias,HttpServletRequest request) {
        //获取项目根路径
        String root = request.getServletContext().getRealPath("/");
        Integer count = 0;
        Integer successCount = 0;
        Integer errorCount = 0;

        for (String media : medias) {
            count++;
            //删除微信服务器上的
            String url = String.format(simpleProperies.getWechat().getDeleteMediaUrl(), wechatUtils.getAccessToken());
            Map<String, String> params = new HashMap<>();
            params.put("media_id", media);
            String jsonData = new JSONObject(params).toString();
            String result = RequestUtils.doPost(url, jsonData);
            JSONObject jsonObject = ApplicationUtils.formatJSONObject(result);
            if (jsonObject.has("errcode")) {
                if (jsonObject.getInt("errcode") == 0) {
                    //删除本地照片
                    WechatMedia wechatMedia = materialService.selectByMediaId(media);
                    File file=new File(root,wechatMedia.getMediaUrl());
                    if(file.exists()){
                        file.delete();
                    }
                    //删除照片数据
                    materialService.deleteMedia(media);
                    successCount++;
                } else {
                    errorCount++;
                }
            } else {
                errorCount++;
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("count", count);
        res.put("successCount", successCount);
        res.put("errorCount", errorCount);
        return new ResultJsonUtils().success(res);
    }

    /**
     * 删除图文消息
     * @param mediaId
     * @param request
     * @return
     */
    @RequestMapping("/deleteNews.json")
    public ResultJsonData deleteNews(String mediaId,HttpServletRequest request){
        //删除本地图文
        WechatNews wechatNews = wechatNewsService.selectByMediaId(mediaId);
        if(wechatNews!=null){
            int delete = wechatNewsService.delete(wechatNews);
            if(delete==1){
                //删除微信上的图文
                String url = String.format(simpleProperies.getWechat().getDeleteMediaUrl(), wechatUtils.getAccessToken());
                Map<String, String> params = new HashMap<>();
                params.put("media_id", mediaId);
                String jsonData = new JSONObject(params).toString();
                String result = RequestUtils.doPost(url, jsonData);
                JSONObject jsonObject = ApplicationUtils.formatJSONObject(result);
                if (jsonObject.has("errcode")) {
                    if (jsonObject.getInt("errcode") == 0) {
                        //删除成功
                        return ResultJsonUtils.success();
                    }
                }
            }
        }
        return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(),"删除失败");
    }

    /**
     * 查询本地图片素材列表
     *
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/find_all_media.json")
    public ResultJsonData findAllMedia(@RequestParam(name = "page", defaultValue = "1") Integer page, @RequestParam(name = "size", defaultValue = "18") Integer size) {
        try {
            List<WechatMedia> wechatMediaList = materialService.pageList(page, size);
            //查询素材总数
            Integer count = materialService.findMediaCount();
            return new ResultJsonUtils().success(wechatMediaList, count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(), "查询失败");
        }
    }

    /**
     * 查询公众号素材列表
     *
     * @param type
     * @param offset
     * @param count
     * @return
     */
    @RequestMapping("/find_media_list.json")
    public String findAllMediaForWechat(String type, @RequestParam(name = "offset", defaultValue = "0") Integer offset, @RequestParam(name = "count", defaultValue = "6") Integer count) {
        String url = String.format(simpleProperies.getWechat().getGetMaterialListUrl(), wechatUtils.getAccessToken());
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("offset", offset);
        params.put("count", count);
        String jsonData = new JSONObject(params).toString();
        String result = RequestUtils.doPost(url, jsonData);
        return result;
    }

    /**
     * 查询素材数量
     *
     * @return
     */
    @RequestMapping("/media_count.json")
    public String getMediaCount() {
        String url = String.format(simpleProperies.getWechat().getGetMaterialCountUrl(), wechatUtils.getAccessToken());
        String result = RequestUtils.doGet(url);
        return result;
    }

    /**
     * 同步公众号素材
     *
     * @param materialType 素材类型
     * @return
     */
    @RequestMapping("/sync_media.json")
    public ResultJsonData syncMedia(HttpServletRequest request, String materialType) {
        //获取项目根路径
        String root = request.getServletContext().getRealPath("/") + "upload";
        //查找公众号素材数量
        String imageCountUrl = String.format(simpleProperies.getWechat().getGetMaterialCountUrl(), wechatUtils.getAccessToken());
        String result = RequestUtils.doGet(imageCountUrl);
        JSONObject jsonObject = ApplicationUtils.formatJSONObject(result);
        //同步照片素材
        if (jsonObject.has("image_count") && "image".equals(materialType)) {
            JSONObject imageDataObject = this.findWechatMaterial(jsonObject.getInt("image_count"), materialType);
            if (imageDataObject.has("item")) {
                JSONArray item = imageDataObject.getJSONArray("item");
                int count = 0;
                for (int a = 0; a < item.length(); a++) {
                    JSONObject itemJSONObject = item.getJSONObject(a);
                    String media_id = itemJSONObject.getString("media_id");
                    //查找media_id是否存在数据库
                    WechatMedia wechatMedia = materialService.selectByMediaId(media_id);
                    if (wechatMedia == null) {
                        //下载图片 保存数据库
                        WechatMedia temp = new WechatMedia();
                        temp.setMediaId(itemJSONObject.getString("media_id"));
                        String fileName = itemJSONObject.getString("name");
                        temp.setMediaName(fileName);
                        temp.setRealUrl(itemJSONObject.getString("url"));
                        temp.setMediaType("image");
                        Date update_time = new Date((itemJSONObject.getLong("update_time")) * 1000L);
                        temp.setCreateTime(update_time);
                        //下载图片
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(simpleProperies.getUploadPrefix());
                        String format = simpleDateFormat.format(update_time);
                        //扩展名
                        String suffixType = ".jpeg";
                        if (fileName.lastIndexOf(".") != -1) {
                            suffixType = fileName.substring(fileName.lastIndexOf("."));
                        }
                        String mediaUrl = this.downImage(itemJSONObject.getString("url"), root, format, suffixType);
                        temp.setMediaUrl(mediaUrl);
                        materialService.save(temp);
                        count++;
                    }
                }
                Map<String, Object> res = new HashMap<>();
                res.put("addCount", count);
                return new ResultJsonUtils().success(res);

            } else {
                return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(), "找不到公众号素材");
            }

        } else if (jsonObject.has("news_count") && "news".equals(materialType)) {
            int count = 0;
            //同步图文消息素材
            JSONObject newsDataObject = this.findWechatMaterial(jsonObject.getInt("news_count"), materialType);
            if (newsDataObject.has("item")) {
                JSONArray items = newsDataObject.getJSONArray("item");
                for (int a = 0; a < items.length(); a++) {
                    JSONObject itemObject = items.getJSONObject(a);
                    boolean flag = this.saveNews(itemObject, root);
                    if (flag) {
                        count++;
                    }

                }
                Map<String, Object> res = new HashMap<>();
                res.put("addCount", count);
                return new ResultJsonUtils().success(res);
            }

            return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(), "找不到公众号素材");
        } else {
            return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(), "找不到公众号素材");
        }
    }

    /**
     * 查询图文列表
     * @return
     */
    @RequestMapping("/news_list.json")
    public ResultJsonData newsList(@RequestParam(name = "page", defaultValue = "1") Integer page, @RequestParam(name = "size", defaultValue = "6") Integer size) throws Exception {
        List<WechatNews> wechatNews = wechatNewsService.pageList(page,size);
        return new ResultJsonUtils().success(wechatNews,wechatNewsService.selectNewsCount());
    }

    /**
     * 根据图文id查询图文信息
     * @param mediaId
     * @return
     */
    @RequestMapping("/findNewsById.json")
    public ResultJsonData findNewsById(String mediaId){
        WechatNews wechatNews = wechatNewsService.selectByMediaId(mediaId);
        return new ResultJsonUtils().success(wechatNews);
    }

    /**
     * 添加图文
     * @param newsData
     * @return
     */
    @RequestMapping("/saveNews.json")
    public ResultJsonData saveNews(String newsData,HttpServletRequest request){
        //获取项目根路径
        String root = request.getServletContext().getRealPath("/") + "upload";
        String url=String.format(simpleProperies.getWechat().getAddNewsUrl(),wechatUtils.getAccessToken());
        String result = RequestUtils.doPost(url, newsData);
        JSONObject jsonObject = ApplicationUtils.formatJSONObject(result);
        if(jsonObject.has("media_id")){
            String mediaId=jsonObject.getString("media_id");
            Map<String,String> resData=new HashMap<>();
            resData.put("mediaId",mediaId);
            //添加到数据库
            String getMaterialUrl=String.format(simpleProperies.getWechat().getGetMaterialUrl(),wechatUtils.getAccessToken());
            String newsDataJson = RequestUtils.doPost(getMaterialUrl, result);
            WechatNews wechatNews=new WechatNews();
            wechatNews.setMediaId(mediaId);
            wechatNews=this.saveNews(wechatNews,newsDataJson,root);
            wechatNewsService.save(wechatNews);
            if(wechatNews.getNewsTitle()==null){
                return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(),"保存图文失败");
            }
            return new ResultJsonUtils().success(resData);
        }
        return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(),"保存图文失败");
    }

    /**
     * 修改图文消息
     * @param newsData
     * @param mediaId
     * @param request
     * @return
     */
    @RequestMapping("/updateNews.json")
    public ResultJsonData updateNews(String newsData,String mediaId,HttpServletRequest request) throws Exception {
        //获取项目根路径
        String root = request.getServletContext().getRealPath("/") + "upload";
        String url=String.format(simpleProperies.getWechat().getUpdateNewsUrl(),wechatUtils.getAccessToken());
        //提取图文中的图片地址 上传到微信 换取微信支持的url
        String contentData = new JSONObject(newsData).getJSONObject("articles").getString("content");
        String contentWx = new JSONObject(newsData).getJSONObject("articles").getString("content");
        Set<String> imgStr = ApplicationUtils.getImgStr(contentWx);
        //替换图片url被转义的符号& -> &amp;
        for(String img:imgStr){
            String newImg=img.replace("&amp;","&");
            //下载图片
            String formatData= ApplicationUtils.getFormatDate(new Date(),"yyyy-MM-dd");
//            String resource = this.downImage(newImg, root,formatData, ".gif");
            String b=root + File.separator + formatData;
            String filename = RequestUtils.downResource(url, root + File.separator + formatData, ".png");
            //上传到微信
            String baseUrl="http://" + request.getServerName() //服务器地址
                    + ":"
                    + request.getServerPort();           //端口号
            String result = root + File.separator + formatData + File.separator + filename;
            //todo 待解决 ！ 替换图片为换取到的微信url
            String uploadUrl=String.format(simpleProperies.getWechat().getUploadNewsImgUrl(),wechatUtils.getAccessToken());
            String s = RequestUtils.f_upload(new File(result), uploadUrl, filename);
            System.out.println(s);
        }

//
//        String result = RequestUtils.doPost(url, newsData);
//        JSONObject jsonObject = ApplicationUtils.formatJSONObject(result);
//        if(jsonObject.has("errcode")){
//            int errcode=jsonObject.getInt("errcode");
//            if(errcode==0){
//                //修改数据库里的图文
//                Map<String,String> resData=new HashMap<>();
//                resData.put("media_id",mediaId);
//                //添加到数据库
//                String getMaterialUrl=String.format(simpleProperies.getWechat().getGetMaterialUrl(),wechatUtils.getAccessToken());
//                String result1="{\"media_id\":\""+mediaId+"\"}";
//                String newsDataJson = RequestUtils.doPost(getMaterialUrl, result1);
//                WechatNews wechatNews=new WechatNews();
//                wechatNews.setMediaId(mediaId);
//                wechatNews=this.saveNews(wechatNews,newsDataJson,root);
//                if(wechatNews.getNewsTitle()==null){
//                    return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(),"保存图文失败");
//                }
//                wechatNewsService.updateAll(wechatNews);
//                return new ResultJsonUtils().success();
//            }
//        }
        return ResultJsonUtils.error(ResponseCode.SERVERERROR.getCode(),"保存图文失败");

    }


    /**
     * 解析并添加图文消息到数据库
     *
     * @param itemObject
     * @return
     */
    private boolean saveNews(JSONObject itemObject, String root) {
        String media_id = itemObject.getString("media_id");
        // 查询该media_id是否存在数据库 如果存在 则进行修改 否则创建
        WechatNews news = wechatNewsService.selectByMediaId(media_id);
        if (news == null) {
            news = new WechatNews();
        }
        if (itemObject.has("content")) {
            JSONObject content = itemObject.getJSONObject("content");
            if (content.has("news_item")) {
                JSONArray news_items = content.getJSONArray("news_item");
                for (int a = 0; a < news_items.length(); a++) {
                    JSONObject c = news_items.getJSONObject(a);
//                    if(a!=0) {
//                        //todo 多个图文 添加子图文
//                        news=new WechatNews();
//                        news.setMediaId(ApplicationUtils.getUUIDFormat());
//                        news.setParentMediaId(media_id);
//                    }
                    news.setContent(c.getString("content"));
                    news.setNewsTitle(c.getString("title"));
                    news.setShowCoverPic(c.getInt("show_cover_pic"));
                    news.setNewsAuthor(c.getString("author"));
                    news.setNewsDigest(c.getString("digest"));
                    news.setNewsUrl(c.getString("url"));
                    news.setContentSourceUrl(c.getString("content_source_url"));
                    news.setNeedOpenComment(c.getInt("need_open_comment"));
                    news.setOnlyFansCanComment(c.getInt("only_fans_can_comment"));
                    news.setThumbMediaUrl(c.getString("thumb_url"));
                    //设置时间
                    news.setCreateTime(ApplicationUtils.formatTenDate(content.getLong("create_time")));
                    news.setUpdateTime(ApplicationUtils.formatTenDate(content.getLong("update_time")));
                    if(StringUtils.isBlank(news.getThumbMediaUrl()) || !(c.getString("thumb_url").equals(news.getThumbMediaUrl())) || !(c.getString("thumb_media_id").equals(news.getThumbMediaId()))){
                        //如果没有封面 或者封面发生变化 重新下载新的图片
                        //下载封面
                        String thumb_url = c.getString("thumb_url");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(simpleProperies.getUploadPrefix());
                        String format = simpleDateFormat.format(ApplicationUtils.formatTenDate(content.getLong("update_time")));
                        //扩展名
                        String suffixType = ".jpeg";
                        String local_url = downImage(thumb_url, root, format, suffixType);
                        news.setThumbMediaLocalUrl(local_url);
                    }
                    news.setThumbMediaUrl(c.getString("thumb_url"));
                    news.setThumbMediaId(c.getString("thumb_media_id"));
                    if (news.getMediaId() == null) {
                        //新建
                        news.setMediaId(media_id);
                        //save
                        wechatNewsService.save(news);

                    } else {
                        //修改
                        wechatNewsService.updateAll(news);
                    }

                }
                return true;
            }
        }
        return false;
    }

    /**
     * 下载图片
     *
     * @param url
     * @param root
     * @param formatDate
     * @param type
     * @return
     */
    private String downImage(String url, String root, String formatDate, String type) {
        String fileName = RequestUtils.downResource(url, root + File.separator + formatDate, type);
        if (fileName != null) {
            String result = simpleProperies.getUploadPath() + File.separator + formatDate + File.separator + fileName;
            return result;
        }
        return "";
    }

    /**
     * 查询公众号素材列表
     *
     * @param count
     * @return
     */
    private JSONObject findWechatMaterial(Integer count, String type) {
        //查询公众号所有素材
        int image_count = count;
        String imageListUrl = String.format(simpleProperies.getWechat().getGetMaterialListUrl(), wechatUtils.getAccessToken());
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("offset", 0);
        params.put("count", image_count);
        String jsonData = new JSONObject(params).toString();
        String imageList = RequestUtils.doPost(imageListUrl, jsonData);
        JSONObject materialDataObject = ApplicationUtils.formatJSONObject(imageList);
        return materialDataObject;
    }

    /**
     * json转图文实体类
     * @param news
     * @param json
     * @param root
     * @return
     */
    private WechatNews saveNews(WechatNews news,String json,String root){
        JSONObject jsonObject = ApplicationUtils.formatJSONObject(json);
        if(jsonObject.has("news_item")){
            JSONArray news_item = jsonObject.getJSONArray("news_item");
            for (int a=0;a<news_item.length();a++){
                JSONObject c=news_item.getJSONObject(a);
                news.setContent(c.getString("content"));
                news.setNewsTitle(c.getString("title"));
                news.setShowCoverPic(c.getInt("show_cover_pic"));
                news.setNewsAuthor(c.getString("author"));
                news.setNewsDigest(c.getString("digest"));
                news.setNewsUrl(c.getString("url"));
                news.setContentSourceUrl(c.getString("content_source_url"));
                news.setThumbMediaId(c.getString("thumb_media_id"));
                news.setNeedOpenComment(c.getInt("need_open_comment"));
                news.setOnlyFansCanComment(c.getInt("only_fans_can_comment"));
                news.setThumbMediaUrl(c.getString("thumb_url"));
                //下载封面
                String thumb_url = c.getString("thumb_url");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(simpleProperies.getUploadPrefix());
                String format = simpleDateFormat.format(ApplicationUtils.formatTenDate(jsonObject.getLong("update_time")));
                //扩展名
                String suffixType = ".jpeg";
                String local_url = downImage(thumb_url, root, format, suffixType);
                news.setThumbMediaLocalUrl(local_url);
            }
            //设置时间
            news.setCreateTime(ApplicationUtils.formatTenDate(jsonObject.getLong("create_time")));
            news.setUpdateTime(ApplicationUtils.formatTenDate(jsonObject.getLong("update_time")));
        }
        return news;
    }
}


