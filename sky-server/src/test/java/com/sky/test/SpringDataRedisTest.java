package com.sky.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import springfox.documentation.spring.web.DocumentationCache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DocumentationCache resourceGroupCache;

    //@Test
    public void testRedisTemplate(){
        System.out.println(redisTemplate);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        HashOperations hashOperations = redisTemplate.opsForHash();
        ListOperations listOperations = redisTemplate.opsForList();
        SetOperations setOperations = redisTemplate.opsForSet();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
    }

    //@Test
    public void testString(){
        // set get setex setnx
        redisTemplate.opsForValue().set("name","张三");
        String name = (String) redisTemplate.opsForValue().get("name");
        System.out.println(name);
        redisTemplate.opsForValue().set("code","1234",3,TimeUnit.MINUTES);
        redisTemplate.opsForValue().setIfAbsent("lock","1");
        redisTemplate.opsForValue().setIfAbsent("lock","2");
    }

    /**
     * 操作哈希类型的数据
     */
    //@Test
    public void testHset(){
        // hset hget hkeys hvals
        HashOperations hashOperations = redisTemplate.opsForHash();

        hashOperations.put("1","name","tom");
        hashOperations.put("1","age","20");

        String name = (String) hashOperations.get("1", "name");
        System.out.println(name);

        Set keys = hashOperations.keys("1");
        System.out.println(keys);

        List values = hashOperations.values("1");
        System.out.println(values);

        hashOperations.delete("1","age");
    }

    /**
     *  操作列表类型数据
     */
    //@Test
    public void testList(){
        // lpush lrange rpop llen
        ListOperations listOperations = redisTemplate.opsForList();

        listOperations.leftPushAll("mylist","a","b","c");// 插入多个数据
        listOperations.leftPush("mylist","d");

        List mylist=listOperations.range("mylist",0,-1);
        System.out.println(mylist);

        listOperations.rightPop("mylist");

        Long size = listOperations.size("mylist");
        System.out.println(size);
    }

    //@Test
    public void testSet(){
        //sadd smembers scard sinter sunion srem
        SetOperations setOperations = redisTemplate.opsForSet();

        setOperations.add("set1", "a", "b", "c", "d");
        setOperations.add("set2", "a", "b", "x", "y");

        Set members = setOperations.members("set1");
        System.out.println(members);

        Long size = setOperations.size("set1");
        System.out.println(size);

        Set intersect = setOperations.intersect("set1", "set2");
        System.out.println(intersect);

        Set union = setOperations.union("set1", "set2");
        System.out.println(union);

        setOperations.remove("set1", "a", "b");
    }

    //@Test
    public void testZSet(){
        //zadd zrange zincrby zrem
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        zSetOperations.add("zset1", "a", 10);
        zSetOperations.add("zset1", "b", 12);
        zSetOperations.add("zset1", "c", 9);

        Set zset1 = zSetOperations.range("zset1", 0, -1);
        System.out.println(zset1);

        zSetOperations.incrementScore("zset1", "c", 10);

        zSetOperations.remove("zset1", "a", "b");
    }

    /**
     * 通用命令操作
     */
    //@Test
    public void testCommon(){
        //keys exists type del
        Set keys = redisTemplate.keys("*");
        System.out.println(keys);

        Boolean name = redisTemplate.hasKey("name");
        Boolean set1 = redisTemplate.hasKey("set1");

        for (Object key : keys) {
            DataType type = redisTemplate.type(key);
            System.out.println(type.name());
        }

        redisTemplate.delete("mylist");
    }
}
