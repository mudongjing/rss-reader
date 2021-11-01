package xyz.worker.rssReader.utils.sql.mapper;

import tk.mybatis.mapper.common.BaseMapper;
import xyz.worker.rssReader.pojo.StoreMessage;

public interface StoreMessageMapper extends BaseMapper<StoreMessage> {
    int insertWithId(StoreMessage storeMessage);
}
