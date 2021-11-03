package xyz.worker.rssReader.utils.redis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.worker.rssReader.pojo.*;
import xyz.worker.rssReader.utils.RssReaderUtil;
import xyz.worker.rssReader.utils.UtilsOfString;
import xyz.worker.rssReader.utils.redis.RedisConfigFactory;
import xyz.worker.rssReader.utils.sql.service.MessageRecordService;
import xyz.worker.rssReader.utils.sql.service.StoreMessageService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageRecordRedis {
    private AtomicInteger atomicInteger=new AtomicInteger(0);
    @Autowired
    private DefineTableName defineTableName;
    @Autowired
    private PointDB pointDB;
    @Autowired
    private MessageRecordService messageRecordService;
    @Autowired
    private StoreMessageService storeMessageService;
    @Autowired
    private RedisConfigFactory redisConfigFactory;
    @Autowired
    private UtilsOfString utilsOfString;
    @Autowired
    private StoreMessageRedis storeMessageRedis;
    @Autowired
    private RssReaderUtil rssReaderUtil;

    /**
     * 由负责rss更新的线程调用，方法中需要确定插入的消息是有效的，不能是已有的记录
     * @param contents
     * @param url
     * @return
     */
    public Boolean insert(List<RssPojo> contents, String url){
        String tableName=utilsOfString.removeHttpOfUrl(url);
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);
        RedisTemplate<String,Object> isReadTemplate=redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD);
        RedisTemplate<String, Object> redisTemplateLatest = redisConfigFactory.getRedisTemplateByDb(pointDB.LATEST);
        RssPojo latest=new RssPojo();
        if(redisTemplateLatest.hasKey(tableName)){
            latest=(RssPojo) redisTemplateLatest.opsForValue().get(tableName);
        }
        int i;
        RssPojo newres = contents.get(0);
        if(newres.getTitle().equals(latest.getTitle())) {
            return true;
        }
        redisTemplateLatest.opsForValue().set(tableName,newres);
        for (RssPojo content:contents) {
            if(! content.getTitle().equals(latest.getTitle())){
                String s=JSONObject.toJSONString(content);
                i = messageRecordService.insertMessage(s, tableName);
                if(i!=-1){
                    MessageRecord messageRecord=new MessageRecord(tableName,i,s,false);
                    String s1 = JSONObject.toJSONString(messageRecord);
                    redisTemplate.opsForHash().put(tableName,i+"",s1);
                    isReadTemplate.opsForSet().add(tableName,i);
                }else return false;
            }else return true;
            i=-1;
        }
        return true;
    }

    public void dropNowMess(String tableName){
        List<RedisTemplate<String,Object>> redisTemplateList=new ArrayList<>();
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB));
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD));
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.LATEST));
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.LEVEL));
        redisTemplateList.forEach(redis-> {
            redis.delete(tableName);
        });
    }

    /**
     * 对于消息从未读到已读，向各个数据库中更新相关的记录
     * @param id 消息在其中的消息表中的键值
     * @param tableName
     * @return
     */
    public Boolean update(Integer id,String tableName){
        RedisTemplate<String, Object> redisTemplateMess = redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);
        String messageRecordStr =(String)redisTemplateMess.opsForHash().get(tableName, id+"");
        MessageRecord messageRecord = JSONObject.parseObject(messageRecordStr, MessageRecord.class);
        int messageId=messageRecord.getMessageId();
        if(messageRecordService.updateIsRead(messageId, tableName)){
            RedisTemplate<String, Object> redisTemplateRead = redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD);
            redisTemplateRead.opsForSet().remove(tableName,id);
            return true;
        }
        return false;
    }

    /**
     * 负责处理那些别列为收藏的消息
     * 提取对应tableName消息表的对应id的记录，将其添加到收藏表中
     * @param id
     * @param tableName
     * @return
     */
    public Boolean Store(Integer id,String tableName){
        RedisTemplate<String, Object> redisTemplateMess = redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);
        MessageRecord messageRecord =(MessageRecord)redisTemplateMess.opsForHash().get(tableName, id);
        return storeMessageRedis.add(messageRecord.getMessageContent(), tableName);
    }

    /**
     * 由客户端使用，负责返回用户希望获取的rss消息
     * isAll为true，则返回所有消息，包括已读的
     * 反之，则只返回未读的消息
     * @param tableName
     * @param isAll
     * @return
     */
    public List<String> show(String tableName,Boolean isAll){
        Comparator<Integer> comparator1 = new Comparator<Integer>() {
            public int compare(Integer obj1, Integer obj2) { return obj2.intValue()-obj1.intValue(); }};
        Map<Integer,String> treeMap=new TreeMap<Integer, String>(comparator1);
        RedisTemplate<String, Object> redisTemplateByDb = redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);

        Map<Object,Object> mapShow=null;
        if(isAll){  mapShow=redisTemplateByDb.opsForHash().entries(tableName);
        }else{
            RedisTemplate<String, Object> redisTemplateIsRead = redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD);
            Set<Object> members = redisTemplateIsRead.opsForSet().members(tableName);
            mapShow=new HashMap<>();
            for (Object o:members) {
                String id=(Integer) o+"";
                Object o1 = redisTemplateByDb.opsForHash().get(tableName, id);
                mapShow.put(id,o1);
            }
        }
        mapShow.forEach((key,value)->{ treeMap.put(Integer.parseInt((String)key),JSONObject.toJSONString(value)); });
        List<String> list=new ArrayList<>();
        treeMap.forEach((key,value)->{ list.add(value); });
        return list;
    }

    public void getAndInsertMessageFromUrl(UrlRecord value,Boolean first){
        String url=value.getUrlContent();
        String tableName = utilsOfString.removeHttpOfUrl(url);
        int level=value.getUrlLevel();
        RedisTemplate<String,Object> redisTemplateLevepackage xyz.worker.rssReader.utils.redis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.worker.rssReader.pojo.*;
import xyz.worker.rssReader.utils.RssReaderUtil;
import xyz.worker.rssReader.utils.UtilsOfString;
import xyz.worker.rssReader.utils.redis.RedisConfigFactory;
import xyz.worker.rssReader.utils.sql.service.MessageRecordService;
import xyz.worker.rssReader.utils.sql.service.StoreMessageService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageRecordRedis {
    private AtomicInteger atomicInteger=new AtomicInteger(0);
    @Autowired
    private DefineTableName defineTableName;
    @Autowired
    private PointDB pointDB;
    @Autowired
    private MessageRecordService messageRecordService;
    @Autowired
    private StoreMessageService storeMessageService;
    @Autowired
    private RedisConfigFactory redisConfigFactory;
    @Autowired
    private UtilsOfString utilsOfString;
    @Autowired
    private StoreMessageRedis storeMessageRedis;
    @Autowired
    private RssReaderUtil rssReaderUtil;

    /**
     * 由负责rss更新的线程调用，方法中需要确定插入的消息是有效的，不能是已有的记录
     * @param contents
     * @param url
     * @return
     */
    public Boolean insert(List<RssPojo> contents, String url){
        String tableName=utilsOfString.removeHttpOfUrl(url);
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);
        RedisTemplate<String,Object> isReadTemplate=redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD);
        RedisTemplate<String, Object> redisTemplateLatest = redisConfigFactory.getRedisTemplateByDb(pointDB.LATEST);
        RssPojo latest=new RssPojo();
        if(redisTemplateLatest.hasKey(tableName)){
            latest=(RssPojo) redisTemplateLatest.opsForValue().get(tableName);
        }
        int i;
        RssPojo newres = contents.get(0);
        if(newres.getTitle().equals(latest.getTitle())) {
            return true;
        }
        redisTemplateLatest.opsForValue().set(tableName,newres);
        for (RssPojo content:contents) {
            if(! content.getTitle().equals(latest.getTitle())){
                String s=JSONObject.toJSONString(content);
                i = messageRecordService.insertMessage(s, tableName);
                if(i!=-1){
                    MessageRecord messageRecord=new MessageRecord(tableName,i,s,false);
                    String s1 = JSONObject.toJSONString(messageRecord);
                    redisTemplate.opsForHash().put(tableName,i+"",s1);
                    isReadTemplate.opsForSet().add(tableName,i);
                }else return false;
            }else return true;
            i=-1;
        }
        return true;
    }

    public void dropNowMess(String tableName){
        List<RedisTemplate<String,Object>> redisTemplateList=new ArrayList<>();
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB));
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD));
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.LATEST));
        redisTemplateList.add((RedisTemplate<String,Object>)redisConfigFactory.getRedisTemplateByDb(pointDB.LEVEL));
        redisTemplateList.forEach(redis-> {
            redis.delete(tableName);
        });
    }

    /**
     * 对于消息从未读到已读，向各个数据库中更新相关的记录
     * @param id 消息在其中的消息表中的键值
     * @param tableName
     * @return
     */
    public Boolean update(Integer id,String tableName){
        RedisTemplate<String, Object> redisTemplateMess = redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);
        String messageRecordStr =(String)redisTemplateMess.opsForHash().get(tableName, id+"");
        MessageRecord messageRecord = JSONObject.parseObject(messageRecordStr, MessageRecord.class);
        int messageId=messageRecord.getMessageId();
        if(messageRecordService.updateIsRead(messageId, tableName)){
            RedisTemplate<String, Object> redisTemplateRead = redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD);
            redisTemplateRead.opsForSet().remove(tableName,id);
            return true;
        }
        return false;
    }

    /**
     * 负责处理那些别列为收藏的消息
     * 提取对应tableName消息表的对应id的记录，将其添加到收藏表中
     * @param id
     * @param tableName
     * @return
     */
    public Boolean Store(Integer id,String tableName){
        RedisTemplate<String, Object> redisTemplateMess = redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);
        MessageRecord messageRecord =(MessageRecord)redisTemplateMess.opsForHash().get(tableName, id);
        return storeMessageRedis.add(messageRecord.getMessageContent(), tableName);
    }

    /**
     * 由客户端使用，负责返回用户希望获取的rss消息
     * isAll为true，则返回所有消息，包括已读的
     * 反之，则只返回未读的消息
     * @param tableName
     * @param isAll
     * @return
     */
    public List<String> show(String tableName,Boolean isAll){
        Comparator<Integer> comparator1 = new Comparator<Integer>() {
            public int compare(Integer obj1, Integer obj2) { return obj2.intValue()-obj1.intValue(); }};
        Map<Integer,String> treeMap=new TreeMap<Integer, String>(comparator1);
        RedisTemplate<String, Object> redisTemplateByDb = redisConfigFactory.getRedisTemplateByDb(pointDB.MESSAGEDB);

        Map<Object,Object> mapShow=null;
        if(isAll){  mapShow=redisTemplateByDb.opsForHash().entries(tableName);
        }else{
            RedisTemplate<String, Object> redisTemplateIsRead = redisConfigFactory.getRedisTemplateByDb(pointDB.ISREAD);
            Set<Object> members = redisTemplateIsRead.opsForSet().members(tableName);
            mapShow=new HashMap<>();
            for (Object o:members) {
                String id=(Integer) o+"";
                Object o1 = redisTemplateByDb.opsForHash().get(tableName, id);
                mapShow.put(id,o1);
            }
        }
        mapShow.forEach((key,value)->{ treeMap.put(Integer.parseInt((String)key),JSONObject.toJSONString(value)); });
        List<String> list=new ArrayList<>();
        treeMap.forEach((key,value)->{ list.add(value); });
        return list;
    }

    public void getAndInsertMessageFromUrl(UrlRecord value,Boolean first){
        String url=value.getUrlContent();
        String tableName = utilsOfString.removeHttpOfUrl(url);
        int level=value.getUrlLevel();
        RedisTemplate<String,Object> redisTemplateLevel=redisConfigFactory.getRedisTemplateByDb(pointDB.LEVEL);
        if(first){
            List<RssPojo> rssFromUrl = rssReaderUtil.getRssFromUrl(url);
            redisTemplateLevel.opsForValue().set(tableName,level);
            insert(rssFromUrl,url);
        }else{
            Integer levelNow = (Integer)redisTemplateLevel.opsForValue().get(tableName);
            if(levelNow==0){
                List<RssPojo> rssFromUrl = rssReaderUtil.getRssFromUrl(url);
                redisTemplateLevel.opsForValue().set(tableName,level);
                insert(rssFromUrl,url);
            }else{
                redisTemplateLevel.opsForValue().set(tableName,levelNow-1);
            }
        }
    }
}
l=redisConfigFactory.getRedisTemplateByDb(pointDB.LEVEL);
        List<RssPojo> rssFromUrl = rssReaderUtil.getRssFromUrl(url);
        if(first){
            redisTemplateLevel.opsForValue().set(tableName,level);
            insert(rssFromUrl,url);
        }else{
            Integer levelNow = (Integer)redisTemplateLevel.opsForValue().get(tableName);
            if(levelNow==0){
                redisTemplateLevel.opsForValue().set(tableName,level);
                insert(rssFromUrl,url);
            }else{
                redisTemplateLevel.opsForValue().set(tableName,levelNow-1);
            }
        }
    }
}
