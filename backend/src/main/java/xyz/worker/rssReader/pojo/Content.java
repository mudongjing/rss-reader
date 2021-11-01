package xyz.worker.rssReader.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content {
    private String title;
    private String author;
    private String category;
    private String articleAddress;
    private String address;
    private String description;
    private String content;
    private String comment;
}
