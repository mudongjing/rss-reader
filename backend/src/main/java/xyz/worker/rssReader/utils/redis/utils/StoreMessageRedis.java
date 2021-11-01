package xyz.worker.rssReader.utils.redis.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.worker.rssReader.pojo.DefineTableName;
import xyz.worker.rssReader.pojo.StoreMessage;
import xyz.worker.rssReader.utils.redis.RedisConfigFactory;
import xyz.worker.rssReader.utils.sql.service.StoreMessageService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 先提交到mysql,返回成功再执行redis,否则直接返回错误，
 * redis中的数据均转换为json格式的字符串
 */

@Component
public class StoreMessageRedis {
    private AtomicInteger atomicInteger=new AtomicInteger(0);
    @Autowired
    private DefineTableName defineTableName;
    @Autowired
    private StoreMessageService storeMessageService;
    @Autowired
    private RedisConfigFactory redisConfigFactory;

    public Boolean add(String content,String url){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        int add = storeMessageService.add(content, url);
        if(add==-1) return false;
        else{
            StoreMessage storeMessage=new StoreMessage(add,content,url);
            // String s= JSONObject.toJSONString(storeMessage);
            redisTemplate.opsForHash().put(defineTableName.STORE_TABLE,atomicInteger.incrementAndGet()+"",storeMessage);
        }
        return true;
    }
    public Boolean delete(String id){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        StoreMessage value = (StoreMessage)redisTemplate.opsForHash().get(defineTableName.STORE_TABLE,id);
        if(value==null) return false;
        Boolean delete = storeMessageService.delete(value.getMessageId());
        if(delete){
            redisTemplate.opsForHash().delete(defineTableName.STORE_TABLE,id);
            return true;
        }
        return false;
    }
    public Map<Object, Object> show(){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(defineTableName.STORE_TABLE);
        return entries;
    }
}
