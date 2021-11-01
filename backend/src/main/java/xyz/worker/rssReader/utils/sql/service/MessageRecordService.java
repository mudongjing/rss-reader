package xyz.worker.rssReader.utils.sql.service;

public interface MessageRecordService {
    void deleteTable(String name);
    Boolean createTable(String name);
    int insertMessage(String content,String name);
    Boolean updateIsRead(Integer id,String name);
}
