package xyz.worker.rssReader.mvc;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.worker.rssReader.utils.UtilsOfString;
import xyz.worker.rssReader.utils.redis.utils.MessageRecordRedis;
import xyz.worker.rssReader.utils.redis.utils.UrlRecordRedis;

import java.util.List;

@CrossOrigin
@RestController
public class MessageController {
    @Autowired
    private MessageRecordRedis messageRecordRedis;
    @Autowired
    private UrlRecordRedis urlRecordRedis;
    @Autowired
    private UtilsOfString utilsOfString;
    /**
     * 客户端发送rss源对应的哈希键值，获取对应的完整消息
     * @param id
     * @return
     */
    @GetMapping("message/show/{id}/{isAll}")
    public String show(@PathVariable String id,@PathVariable Boolean isAll){
        String urlFromId = urlRecordRedis.getUrlFromId(id);
        String s = utilsOfString.removeHttpOfUrl(urlFromId);
        List<String> show = messageRecordRedis.show(s, isAll);
        String res= JSONObject.toJSONString(show);
        return res;
    }

    /**
     * 收藏一条消息
     * @param tableId
     * @param messageId
     * @return
     */
    @GetMapping("store/{tableId}/{messageId}")
    public Boolean store(@PathVariable String tableId,@PathVariable Integer messageId){
        String urlFromId = urlRecordRedis.getUrlFromId(tableId);
        return messageRecordRedis.Store(messageId,utilsOfString.removeHttpOfUrl(urlFromId));
    }

    /**
     * 对已读消息做处理
     * @param tableId
     * @param messageId
     */
    @GetMapping("message/isread/{tableId}/{messageId}")
    public void isRead(@PathVariable String tableId,@PathVariable Integer messageId){
        String urlFromId = urlRecordRedis.getUrlFromId(tableId);
        messageRecordRedis.update(messageId,utilsOfString.removeHttpOfUrl(urlFromId));
    }
}
