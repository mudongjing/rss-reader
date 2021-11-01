package xyz.worker.rssReader.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="url_table")
public class UrlRecord implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer urlId;
    private String urlContent;
    private Integer urlLevel=1;
    public UrlRecord(String urlContent){
        this.urlContent=urlContent;
    }
    public UrlRecord(String urlContent,Integer urlLevel){
        this.urlContent=urlContent;
        this.urlLevel=urlLevel;
    }
}
