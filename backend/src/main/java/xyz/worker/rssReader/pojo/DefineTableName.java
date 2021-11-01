package xyz.worker.rssReader.pojo;

import org.springframework.stereotype.Component;

@Component
public class DefineTableName {
    @Deprecated
    public String MESSAGE_TABLE="MessageTable-";
    public String URL_TABLE="url_table";
    public String STORE_TABLE="store_table";
}
