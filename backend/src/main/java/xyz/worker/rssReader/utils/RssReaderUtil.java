package xyz.worker.rssReader.utils;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.worker.rssReader.pojo.PointDB;
import xyz.worker.rssReader.pojo.RssPojo;
import xyz.worker.rssReader.utils.redis.RedisConfigFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Component
public class RssReaderUtil {
    @Autowired
    private RedisConfigFactory redisConfigFactory;
    @Autowired
    private PointDB pointDB;
    @Autowired
    private UtilsOfString utilsOfString;

//    @SneakyThrows
    public List<RssPojo> getRssFromUrl(String url){
        URLConnection conn= null;
        try {
            conn = new URL(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            List<RssPojo> stringList=new ArrayList<>();
            try { conn = new URL(url).openConnection(); } catch (IOException e) { e.printStackTrace(); }
            finally { if (conn==null)return stringList; }

            conn.setConnectTimeout(300000);
            conn.setReadTimeout(300000);
            SyndFeed feed = null;
            try { feed = new SyndFeedInput().build(new XmlReader(conn)); } catch (FeedException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
            finally { if (feed==null) return stringList; }

            String tableName=utilsOfString.removeHttpOfUrl(url);
            if(feed.getTitle()!=null){
                RedisTemplate<String,Object> redisTemplateTitle=redisConfigFactory.getRedisTemplateByDb(pointDB.TITLE);
                if(!redisTemplateTitle.hasKey(tableName)){
                    if(!feed.getTitle().replace(" ","").equals("") && feed.getTitle()!=null) redisTemplateTitle.opsForValue().set(tableName,feed.getTitle());
                    else redisTemplateTitle.opsForValue().set(tableName,utilsOfString.removeHttpOfUrl(url));
                }
            }
            if(feed.getImage()!=null){
                RedisTemplate<String,Object> redisTemplateLogo=redisConfigFactory.getRedisTemplateByDb(pointDB.LOGO);
                if(!redisTemplateLogo.hasKey(tableName)){ redisTemplateLogo.opsForValue().set(tableName,feed.getImage().getUrl()); }
            }

            List<SyndEntry> entries = feed.getEntries();
            for (SyndEntry entry: entries) {
                RssPojo rssPojo=new RssPojo();
                List<Module> modules = entry.getModules();
                rssPojo.setTitle(entry.getTitle());
                rssPojo.setAuthor(entry.getAuthor());
                rssPojo.setPublishDate(entry.getPublishedDate());

                if(entry.getCategories().size()!=0) { rssPojo.setCategory(entry.getCategories().get(0).getName()); }
                if(entry.getUri()!=null) rssPojo.setArticleUri(entry.getUri());
                if(entry.getLink()!=null && !entry.getLink().equals(entry.getUri())){ rssPojo.setArticleLink(entry.getLink()); }
                if(entry.getContents().size()!=0 && entry.getContents().get(0).getValue().length()!=0) { rssPojo.setArticleContent(entry.getContents().get(0).getValue()); }
                else if(entry.getDescription().getValue()!=null) { rssPojo.setArticleDescription(entry.getDescription().getValue()); }
                if(entry.getComments()!=null && !entry.getComments().equals("null")) { rssPojo.setComments(entry.getComments()); }
                stringList.add(rssPojo);
            }
            return stringList;
        }
    }
}
