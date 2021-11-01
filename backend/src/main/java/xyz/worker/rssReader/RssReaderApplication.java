package xyz.worker.rssReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 作为主函数启动，但找不到spring相关类函数
 */
@SpringBootApplication
@MapperScan(basePackages = {"xyz.worker.rssReader.utils.sql.mapper"})
public class RssReaderApplication {
    public static void main(String[] args) {
        SpringApplication.run(RssReaderApplication.class, args);
    }
}
