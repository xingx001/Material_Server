package com.simple.springbootbasic.utils;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @Description request工具类
 * @Author Simple
 * @Date 2018/7/19 17:03
 * @Version 1.0
 */
public class RequestUtils {

    /**
     * 发送 get请求
     *
     * @param url 请求url
     */
    public static String doGet(String url) {
        CloseableHttpClient httpClient = null;
        HttpGet httpGet = null;
        String result = null;
        try {
            httpClient = HttpClients.createDefault();
            httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "utf8");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                //关闭连接
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 知乎上找到到传统上传方式 https://www.zhihu.com/question/65960909
     *
     * @param file
     * @param url
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String f_upload(File file, String url, String fileName) throws Exception {

        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestProperty("Connection", "Keep-alive");
        con.setRequestProperty("Charset", "utf-8");

        // 设置边界
        String BOUNDARY = "----------" + System.currentTimeMillis();
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

        // 输出头
        StringBuilder sb = new StringBuilder();
        sb.append("--");
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition:form-data;name=\"media\";filename=\"" + fileName + "\"\r\n");


        sb.append("Content-Type:application/octet-stream\r\n\r\n");
        byte[] head = sb.toString().getBytes("utf-8");
        OutputStream out = new DataOutputStream(con.getOutputStream());
        out.write(head);

        // 输出体
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes = 0;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();

        // 输出尾巴
        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();

        out.write(foot);
        out.flush();
        out.close();

        // 读取微信服务器返回的数据
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        return buffer.toString();
    }


    /**
     * post请求 请求参数为普通key-value方式
     *
     * @param url     请求地址
     * @param map     请求参数
     * @param charset 编码格式
     * @return
     */
    public static String doPost(String url, Map<String, String> map, String charset) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            //设置参数
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
                httpPost.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                //关闭连接
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * post请求 请求参数为json数据
     *
     * @param url
     * @param jsonData
     * @return
     */
    public static String doPost(String url, String jsonData) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            // 解决中文乱码问题
            StringEntity stringEntity = new StringEntity(jsonData, "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(stringEntity);
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "UTF-8");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                //关闭连接
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 下载资源
     * @param url
     * @param path
     * @param type 扩展名
     * @return
     */
    public static String downResource(String url, String path,String type) {
        CloseableHttpClient httpClient = null;
        HttpGet httpGet = null;
        InputStream result = null;
        try {
            httpClient = HttpClients.createDefault();
            httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = resEntity.getContent();
                    //获取文件后缀
                    String fileName=ApplicationUtils.getUUIDFormat()+type;
                    File file=new File(path);
                    if(file.exists()){
                       file.mkdirs();
                    }
                    file=new File(file,fileName);
                    FileUtils.copyInputStreamToFile(result,file);
                    return fileName;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                //关闭连接
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将map参数转换为url形式的
     * 例如:username:simple -> ?username=simple
     *
     * @param map
     * @return
     */
    public static String paramConvert(Map<String, String> map) {
        String queryParam = "?";
        if (map != null && map.size() > 0) {
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                queryParam += key + "=" + map.get(key) + "&";
            }
            //把最后一个&去掉
            queryParam = queryParam.substring(0, queryParam.lastIndexOf("&"));
            return queryParam;
        } else {
            return "";
        }
    }


    /**
     * servlet获取body内容的读取 因为request.getParameter("key")
     * 是以key-value形式获取，而JSON数据是无实际的KEY的 ，当然我们也可以传递参数将Value设置我们的JSON串 此方法仅仅作为 读取
     * body 内容
     *
     * @return
     * @throws IOException
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        String returnStr = null;
        InputStream in = request.getInputStream();
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        //增加缓冲功能
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        bufferedReader.close();
        // 转成字符串
        returnStr = stringBuffer.toString();
        return returnStr;
    }


    public static void main(String[] args) throws Exception {
        String result = downResource("http://mmbiz.qpic.cn/mmbiz_jpg/w9GMnwKCInWujz2YllEM0QicbhL8ok3MrMsyYGUrLo48NodEWVgpxkOicfAkKU6KI7SiaI1Wllb3eRAr9LOlBLUGA/0?wx_fmt=jpeg",
                "/Users/simple/work", ".jpeg");
        System.out.println(result);
    }
}
