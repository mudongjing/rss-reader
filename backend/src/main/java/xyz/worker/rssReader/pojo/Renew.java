package xyz.worker.rssReader.pojo;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Renew {
    // 默认5分钟遍历一次
    private Integer renewGap=60000;
}
