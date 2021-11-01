package xyz.worker.rssReader.utils.sql.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.worker.rssReader.pojo.MessageRecord;
import xyz.worker.rssReader.pojo.DefineTableName;
import xyz.worker.rssReader.utils.UtilsOfString;
import xyz.worker.rssReader.utils.sql.mapper.MessageRecordMapper;
import xyz.worker.rssReader.utils.sql.service.MessageRecordService;

import javax.annotation.Resource;

@Service
public class MessageRecordServiceImpl implements MessageRecordService {
    @Autowired
    private DefineTableName defineTableName;
    @Resource
    private MessageRecordMapper messageRecordMapper;
    @Autowired
    private UtilsOfString utilsOfString;

    @Override
    public void deleteTable(String name) {
        messageRecordMapper.deleteMessageTable(name);
    }

    @Override
    public Boolean createTable(String tableName) {
        deleteTable(tableName);
        int messageTable = messageRecordMapper.createMessageTable(tableName);
        if(messageTable>=0) return true;
        return false;
    }

    @Override
    public int insertMessage(String content, String name) {
        MessageRecord messageRecord=new MessageRecord(name,content);
        int i = messageRecordMapper.insertMessage(messageRecord);
        if(i>0) {
            Integer messageId = messageRecord.getMessageId();
            return messageId;
        }
        return -1;
    }

    @Override
    public Boolean updateIsRead(Integer id, String name) {
        MessageRecord messageRecord=new MessageRecord(name,id);
        int i = messageRecordMapper.updateMessageIsRead(messageRecord);
        if(i>0) return true;
        return false;
    }
}
