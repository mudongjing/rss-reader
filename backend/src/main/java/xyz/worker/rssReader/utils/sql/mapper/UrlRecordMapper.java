package xyz.worker.rssReader.utils.sql.mapper;

import tk.mybatis.mapper.common.BaseMapper;
import xyz.worker.rssReader.pojo.UrlRecord;

import java.util.List;

public interface UrlRecordMapper extends BaseMapper<UrlRecord> {
    int addUrl(UrlRecord urlRecord);
    void dropUrlTable();
    void createUrlTable();
    String selectForUrl(String url);
}
