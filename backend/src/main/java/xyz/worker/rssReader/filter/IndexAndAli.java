package xyz.worker.rssReader.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexAndAli {
    private String s; // 哈希过程中使用的字符串
    private Integer index; // 原数组中，计算到的字符位置
}
