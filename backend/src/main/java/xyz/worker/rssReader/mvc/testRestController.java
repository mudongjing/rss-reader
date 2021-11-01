package xyz.worker.rssReader.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.worker.rssReader.utils.redis.utils.StoreMessageRedis;
import xyz.worker.rssReader.utils.sql.service.StoreMessageService;

import java.util.Map;

@CrossOrigin
@RestController
public class testRestController {
    @Autowired
    private StoreMessageService storeMessageService;
    @Autowired
    private StoreMessageRedis storeMessageRedis;
    @PostMapping("test/store/add")
    public Boolean storeAdd(@RequestParam("content") String content,@RequestParam("url") String url){
        return storeMessageRedis.add(content,url);
    }
    @GetMapping("test/store/delete/{id}")
    public Boolean storeDelete(@PathVariable String id){
        return storeMessageRedis.delete(id);
    }
    @GetMapping("test/store/show")
    public Map<Object,Object> show(){
        return storeMessageRedis.show();
    }
}
