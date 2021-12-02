package xyz.worker.rssReader.utils.sql.service;

import xyz.worker.rssReader.pojo.UrlRecord;

import java.util.List;

public interface UrlRecordService {
    int add(String url,Integer level);
    Boolean delete(Integer id);
    List<UrlRecord> show();
    String search(String s);
}
