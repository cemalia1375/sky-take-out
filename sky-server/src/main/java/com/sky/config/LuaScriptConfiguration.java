package com.sky.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class LuaScriptConfiguration{

    @Bean
    public DefaultRedisScript<Long> batchStockLuaScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();

        script.setScriptText(
                "for i=1,#KEYS do " +
                        "   local stock = redis.call('get', KEYS[i]) " +
                        "   if (not stock) then return 0 end " +
                        "   stock = tonumber(stock) " +
                        "   if (not stock) then return 0 end " +
                        "   local num = tonumber(ARGV[i]) " +
                        "   if (not num) or num <= 0 then return 0 end " +
                        "   if stock < num then return 0 end " +
                        "end " +
                        "for i=1,#KEYS do " +
                        "   redis.call('decrby', KEYS[i], ARGV[i]) " +
                        "end " +
                        "return 1"
        );

        script.setResultType(Long.class);
        return script;
    }
        //1:扣减成功
        //2:库存不足
        //3:没库存key

    //用于回滚（增加库存）的Bean
    @Bean
    public DefaultRedisScript<Long> batchRollbackLuaScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
                "for i=1,#KEYS do " +
                        "   redis.call('incrby', KEYS[i], ARGV[i]) " +
                        "end " +
                        "return 1"
        );
        script.setResultType(Long.class);
        return script;
    }
}