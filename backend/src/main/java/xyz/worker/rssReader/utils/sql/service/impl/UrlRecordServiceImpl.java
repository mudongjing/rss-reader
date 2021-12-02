package xyz.worker.rssReader.utils.sql.service.impl;

import org.springframework.stereotype.Service;
import xyz.worker.rssReader.pojo.UrlRecord;
import xyz.worker.rssReader.utils.sql.mapper.UrlRecordMapper;
import xyz.worker.rssReader.utils.sql.service.UrlRecordService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UrlRecordServiceImpl implements UrlRecordService {
    @Resource
    private UrlRecordMapper urlRecordMapper;

    @Override
    public int add(String url,Integer level) {
        UrlRecord urlRecord=new UrlRecord(url,level);
        int insert = urlRecordMapper.addUrl(urlRecord);
        if(insert>0) {
            return urlRecord.getUrlId();
        }
        return -1;
    }

    @Override
    public Boolean delete(Integer id) {
        int i = urlRecordMapper.deleteByPrimaryKey(id);
        if(i>0) return true;
        return false;
    }

    @Override
    public List<UrlRecord> show() {
        List<UrlRecord> urlRecords = urlRecordMapper.selectAll();
        return urlRecords;
    }

    @Override
    public String search(String s) {
        return urlRecordMapper.selectForUrl(s);
    }
}
