package xyz.worker.rssReader.utils.sql.service;

public interface StoreMessageService {
    int add(String content,String url);
    Boolean delete(Integer id);
}
