package xyz.worker.rssReader.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecord {
    private String messageTable;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;
    private String messageContent;
    private Boolean isRead=false;
    public MessageRecord(String messageTable,String messageContent){
        this.messageTable=messageTable;
        this.messageContent=messageContent;
    }
    public MessageRecord(String messageTable,Integer messageId){
        this.messageTable=messageTable;
        this.messageId=messageId;
    }
}
