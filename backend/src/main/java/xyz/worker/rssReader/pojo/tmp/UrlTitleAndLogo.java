package xyz.worker.rssReader.pojo.tmp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlTitleAndLogo {
    private String title;
    private String logo;
    private String redisId;
    public UrlTitleAndLogo(String title,String logo){
        this.title=title;
        this.logo=logo;
    }
}
