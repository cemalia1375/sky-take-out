package com.sky.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
public class HttpClientTest {

    @Autowired
    private RedisProperties redisProperties;

    /**
     * 测试通过httpclient发送GET方式请求
     */
    //@Test
    public void testGET() throws Exception{
        //创建http对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //创建请求对象
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");

        //发送请求,接受响应结果
        CloseableHttpResponse response = httpClient.execute(httpGet);

        //获取服务端返回的状态码
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println(("服务器返回的状态码为："+statusCode));

        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
        System.out.println(("服务器返回的数据为："+body));

        //关闭资源
        response.close();
        httpClient.close();

    }

    /**
     * 测试通过httpclient发送POST方式请求
     */
    //@Test
    public void testPost() throws Exception{
        //创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //创建请求对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username","admin");
        jsonObject.put("password","123456");

        StringEntity entity = new StringEntity(jsonObject.toString());
        //指定请求的编码方式
        entity.setContentType("utf-8");
        //指定数据格式
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        //发送请求
        CloseableHttpResponse response = httpClient.execute(httpPost);

        //解析返回结果
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("响应码为："+statusCode);

        HttpEntity entity1 = response.getEntity();
        String body = EntityUtils.toString(entity1);
        System.out.println("相应数据为:"+body);

        //关闭资源
        response.close();
        httpClient.close();

    }
}
