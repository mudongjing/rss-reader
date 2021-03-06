package xyz.worker.rssReader.utils.redis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.worker.rssReader.filter.MyFilter;
import xyz.worker.rssReader.filter.RequestMess;
import xyz.worker.rssReader.pojo.DefineTableName;
import xyz.worker.rssReader.pojo.PointDB;
import xyz.worker.rssReader.pojo.UrlRecord;
import xyz.worker.rssReader.pojo.tmp.UrlTitleAndLogo;
import xyz.worker.rssReader.utils.UtilsOfString;
import xyz.worker.rssReader.utils.redis.RedisConfigFactory;
import xyz.worker.rssReader.utils.sql.service.MessageRecordService;
import xyz.worker.rssReader.utils.sql.service.UrlRecordService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UrlRecordRedis {
    private static final Logger logger=LoggerFactory.getLogger(UrlRecordRedis.class);
    private AtomicInteger atomicInteger;
    @Autowired
    private DefineTableName defineTableName;
    @Autowired
    private PointDB pointDB;
    @Autowired
    private UrlRecordService urlRecordService;
    @Autowired
    private RedisConfigFactory redisConfigFactory;
    @Autowired
    private UtilsOfString utilsOfString;
    @Autowired
    private MessageRecordService messageRecordService;
    @Autowired
    private MessageRecordRedis messageRecordRedis;
    @Autowired
    private MyFilter myFilter;
    @PostConstruct
    public void init(){
        RedisTemplate<String, Object> redisTemplateInitnumber = redisConfigFactory.getRedisTemplateByDb(pointDB.INITNUMBER);
        if (redisTemplateInitnumber.hasKey(defineTableName.INIT_NUMBER)){
            Integer initNumber =(Integer)redisTemplateInitnumber.opsForValue().get(defineTableName.INIT_NUMBER);
            atomicInteger=new AtomicInteger(initNumber);
        }else{
            atomicInteger=new AtomicInteger(0);
            redisTemplateInitnumber.opsForValue().set(defineTableName.INIT_NUMBER,0);
        }
    }

    public String getUrlFromId(String id){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        UrlRecord o = (UrlRecord)redisTemplate.opsForHash().get(defineTableName.URL_TABLE, id);
        return o.getUrlContent();
    }
    /**
     * ??????rss?????????http?????????????????????????????????????????????level???????????????????????????????????????????????????????????????
     * ????????????rss?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param url
     * @param level
     * @return
     */
//    public Boolean add(String url,Integer level){
//        if(level==null) level=1;
//        //???????????????
//        int add = urlRecordService.add(url, level);
//        if(add!=-1){
//            RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
//            RedisTemplate<String, Object> redisTemplateInitNumber = redisConfigFactory.getRedisTemplateByDb(pointDB.INITNUMBER);
//            UrlRecord urlRecord=new UrlRecord(add,url,level);
//            // redis ??????
//            Integer initNumber=atomicInteger.incrementAndGet();
//            redisTemplate.opsForHash().put(defineTableName.URL_TABLE,initNumber+"",urlRecord);
//            redisTemplateInitNumber.opsForValue().set(defineTableName.INIT_NUMBER,initNumber);
//            String tableName= utilsOfString.removeHttpOfUrl(url);
//            if(messageRecordService.createTable(tableName)){
//                messageRecordRedis.getAndInsertMessageFromUrl(urlRecord,true);
//                return true;
//            }
//        }
//        return false;
//    }
    public RequestMess add(String url,Integer level,String title){
        if(level==null) level=1;
        // ???????????????
        boolean hasUrl = myFilter.judgeHas(utilsOfString.removeHttpOfUrl(url));
        if (hasUrl) {
            System.out.println("????????????url "+url+" ?????????");
            // ????????????
            if(urlRecordService.search(url)!=null) return new RequestMess(false,url+" ?????????");
        }
        //???????????????
        int add = urlRecordService.add(url, level);
        if(add!=-1){
            // ?????????????????????
            RequestMess requestMess = myFilter.addEle(url, false);
            String mess=null;
            if (requestMess.getSuccss().booleanValue()==false) mess="??????????????????"+requestMess.getMess();
            // ???????????????
            RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
            RedisTemplate<String, Object> redisTemplateInitNumber = redisConfigFactory.getRedisTemplateByDb(pointDB.INITNUMBER);
            UrlRecord urlRecord=new UrlRecord(add,url,level);
            // redis ??????
            Integer initNumber=atomicInteger.incrementAndGet();
            redisTemplate.opsForHash().put(defineTableName.URL_TABLE,initNumber+"",urlRecord);
            redisTemplateInitNumber.opsForValue().set(defineTableName.INIT_NUMBER,initNumber);
            String tableName= utilsOfString.removeHttpOfUrl(url);
            if(messageRecordService.createTable(tableName)){
                messageRecordRedis.getAndInsertMessageFromUrl(urlRecord,true,title);
                return new RequestMess(true,mess);
            }
        }
        return new RequestMess(false,"?????????????????????");
    }
    public Boolean delete(String id){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        RedisTemplate<String, Object> titleRedis = redisConfigFactory.getRedisTemplateByDb(pointDB.TITLE);
        UrlRecord value = (UrlRecord)redisTemplate.opsForHash().get(defineTableName.URL_TABLE,id);
        if(value==null) return false;
        Boolean delete = urlRecordService.delete(value.getUrlId());
        String tableName=utilsOfString.removeHttpOfUrl(value.getUrlContent());
        messageRecordService.deleteTable(tableName);
        if(delete){
            myFilter.delEle(tableName);
            redisTemplate.opsForHash().delete(defineTableName.URL_TABLE,id);
            messageRecordRedis.dropNowMess(tableName);
            titleRedis.delete(tableName);
            return true;
        }
        return false;
    }

    public Map<Object, Object> show(){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(defineTableName.URL_TABLE);
        return entries;
    }
    public Set<Object> showAllId(){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        Set<Object> keys = redisTemplate.opsForHash().keys(defineTableName.URL_TABLE);
        return keys;
    }

    /********************************/

    public String getLogo(String id){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplateByDb(pointDB.LOGO);
        String urlFromId = getUrlFromId(id);
        return (String)redisTemplate.opsForValue().get(utilsOfString.removeHttpOfUrl(urlFromId));
    }
    public String getTitle(String id){
        RedisTemplate<String, Object> redisTemplateByDb = redisConfigFactory.getRedisTemplateByDb(pointDB.TITLE);
        String urlFromId = getUrlFromId(id);
        return (String) redisTemplateByDb.opsForValue().get(utilsOfString.removeHttpOfUrl(urlFromId));
    }
    public UrlTitleAndLogo getTitleAndLogo(String id){
        RedisTemplate<String,Object> redisTemplate=redisConfigFactory.getRedisTemplate();
        UrlRecord o = (UrlRecord)redisTemplate.opsForHash().get(defineTableName.URL_TABLE, id);
        RedisTemplate<String,Object> redisTemplateLogo=redisConfigFactory.getRedisTemplateByDb(pointDB.LOGO);
        RedisTemplate<String, Object> redisTemplateTitle = redisConfigFactory.getRedisTemplateByDb(pointDB.TITLE);
        String s=utilsOfString.removeHttpOfUrl(o.getUrlContent());
        String logo=(String)redisTemplateLogo.opsForValue().get(s);
        String title=(String)redisTemplateTitle.opsForValue().get(s);
        return new UrlTitleAndLogo(title,logo);
    }
}
