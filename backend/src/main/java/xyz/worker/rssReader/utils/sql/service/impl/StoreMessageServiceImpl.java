package xyz.worker.rssReader.utils.sql.service.impl;

import org.springframework.stereotype.Service;
import xyz.worker.rssReader.pojo.StoreMessage;
import xyz.worker.rssReader.utils.sql.mapper.StoreMessageMapper;
import xyz.worker.rssReader.utils.sql.service.StoreMessageService;

import javax.annotation.Resource;

@Service
public class StoreMessageServiceImpl implements StoreMessageService {
    @Resource
    private StoreMessageMapper storeMessageMapper;
    @Override
    public int add(String content, String url) {
        StoreMessage storeMessage=new StoreMessage(content,url);
        int i = storeMessageMapper.insertWithId(storeMessage);
        if(i>0) {
            return storeMessage.getMessageId();
        }
        return -1;
    }

    @Override
    public Boolean delete(Integer id) {
        int i = storeMessageMapper.deleteByPrimaryKey(id);
        if(i>0) return true;
        return false;
    }
}
