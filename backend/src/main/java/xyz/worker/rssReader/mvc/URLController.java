package xyz.worker.rssReader.mvc;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import xyz.worker.rssReader.pojo.UrlRecord;
import xyz.worker.rssReader.pojo.tmp.UrlTitleAndLogo;
import xyz.worker.rssReader.utils.RssReaderUtil;
import xyz.worker.rssReader.utils.redis.utils.UrlRecordRedis;

import java.util.*;

@CrossOrigin
@RestController
public class URLController {
    @Autowired
    private UrlRecordRedis urlRecordRedis;

    @PostMapping("/url/add")
    public Boolean addUrl(@RequestParam("url") String url,@RequestParam("level") Integer level){
        return urlRecordRedis.add(url,level);
    }

    @GetMapping("/url/show")
    public String showList(){
        Map<Object, Object> show = urlRecordRedis.show();
        String res= JSONObject.toJSONString(show);
        return res;
    }

    @GetMapping("url/showAllId")
    public Set<Object> getAllId(){
        Set<Object> objects = urlRecordRedis.showAllId();
        objects.forEach(k->{k=(String)k;});
        return objects;
    }

    @PostMapping("url/getLogo-title")
    public List<UrlTitleAndLogo> getLogoAndTitle(@RequestParam Set<Object> rssId){
        List<UrlTitleAndLogo> res=new ArrayList<>();
        rssId.forEach(id->{
            UrlTitleAndLogo titleAndLogo = urlRecordRedis.getTitleAndLogo((String) id);
            titleAndLogo.setRedisId((String)id);
            res.add(titleAndLogo);
        });
        return  res;
    }

    @GetMapping("url/delete/{id}")
    public Boolean deleteUrl(@PathVariable("id") String id){
        return urlRecordRedis.delete(id);
    }
    @GetMapping("url/logo/{id}")
    public String getLogo(@PathVariable Integer id){
        return urlRecordRedis.getLogo(id+"");
    }
    @GetMapping("url/title/{id}")
    public String getTitle(@PathVariable Integer id){
        return urlRecordRedis.getTitle(id+"");
    }
}
