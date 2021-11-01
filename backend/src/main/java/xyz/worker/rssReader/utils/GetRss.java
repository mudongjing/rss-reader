package xyz.worker.rssReader.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.worker.rssReader.pojo.Renew;
import xyz.worker.rssReader.pojo.UrlRecord;
import xyz.worker.rssReader.utils.redis.utils.MessageRecordRedis;
import xyz.worker.rssReader.utils.redis.utils.UrlRecordRedis;
import xyz.worker.rssReader.utils.sql.mapper.UrlRecordMapper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;

@Component
public class GetRss {
    @Autowired
    private UrlRecordRedis urlRecordRedis;
    @Autowired
    private MessageRecordRedis messageRecordRedis;
    @Autowired
    private Renew renew;
    @Resource
    private UrlRecordMapper urlRecordMapper;
    @PostConstruct
    public void init(){
        dropAndCreateUrlTable();
        createGetRssTread();
    }

    public void createGetRssTread(){
        new Thread(()->{
            while (true){
                try {
                    Thread.sleep(renew.getRenewGap());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Map<Object, Object> show = urlRecordRedis.show();
                Collection<Object> values = show.values();
                //System.out.println("开始遍历rss");
                values.forEach(v->{
                    messageRecordRedis.getAndInsertMessageFromUrl((UrlRecord) v,false);
                });
            }
        }).start();
    }
    public void dropAndCreateUrlTable(){
        urlRecordMapper.dropUrlTable();
        urlRecordMapper.createUrlTable();
    }
}
