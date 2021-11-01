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
@NoArgsConstructor
@AllArgsConstructor
@Table(name="store_message")
public class StoreMessage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;
    private String messageContent;
    private String messageUrl;
    public StoreMessage(String messageContent,String messageUrl){
        this.messageContent=messageContent;
        this.messageUrl=messageUrl;
    }
//    public StoreMessage(Integer messageId,String messageContent,String messageUrl){
//        this.messageId=messageId;
//        this.messageContent=messageContent;
//        this.messageUrl=messageUrl;
//    }
}
