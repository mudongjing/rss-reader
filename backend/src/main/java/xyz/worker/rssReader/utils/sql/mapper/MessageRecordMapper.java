package xyz.worker.rssReader.utils.sql.mapper;

import tk.mybatis.mapper.common.BaseMapper;
import xyz.worker.rssReader.pojo.MessageRecord;

public interface MessageRecordMapper {
    void deleteMessageTable(String tableName);
    int createMessageTable(String tableName);
    int insertMessage(MessageRecord messageRecord);
    int updateMessageIsRead(MessageRecord messageRecord);
}
