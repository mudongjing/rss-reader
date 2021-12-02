package xyz.worker.rssReader.pojo;

import org.springframework.stereotype.Component;

@Component
public class PointDB {
    public Integer MESSAGEDB=1; // 存放各rss对应当前消息的数据库
    public Integer ISREAD=2; // 记录对应rss消息中未读的消息集合
    public Integer LATEST=3; // 各rss对应的上次记录的最新的消息记录，用于之后做判断
    public Integer LOGO=4; // 负责存储rss源对应的Logo的url地址
    public Integer TITLE=5; // 存储rss源对应的标题名
    public Integer LEVEL=6; // rss对应的级别
    public Integer INITNUMBER=7; // 原子整数的初始数值设置
    public Integer FILTER=8; // 存放过滤器
}
