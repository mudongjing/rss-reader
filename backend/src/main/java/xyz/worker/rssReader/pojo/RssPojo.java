package xyz.worker.rssReader.pojo;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
stringList.add("标题："+entry.getTitle()+"--作者："+entry.getAuthor()+"__时间："+entry.getPublishedDate());
        if(entry.getCategories().size()!=0) {
        stringList.add("======================================类别："+entry.getCategories().get(0).getName());
        }
        if(entry.getUri()!=null) stringList.add("原文地址："+entry.getUri());
        if(entry.getLink()!=null && !entry.getLink().equals(entry.getUri())){
        stringList.add("地址："+entry.getLink());
        }
        if(entry.getContents().size()!=0 && entry.getContents().get(0).getValue().length()!=0) {
        stringList.add("介绍："+entry.getContents().get(0).getValue());
        }
        else if(entry.getDescription().getValue()!=null) {
        stringList.add("简介："+entry.getDescription().getValue());
        }
        if(entry.getComments()!=null && !entry.getComments().equals("null"))
        {
        stringList.add("评论："+entry.getComments());
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RssPojo {
    private String title="";
    private String author="";
    private Date publishDate=new Date(25373168000L);
    private String category="";
    private String articleUri="";
    private String articleLink="";
    private String articleContent="";
    private String articleDescription="";
    private String comments="";
    public void clear(){
        this.title=null;
        this.author=null;
        this.publishDate=null;
        this.category=null;
        this.articleUri=null;
        this.articleLink=null;
        this.articleContent=null;
        this.articleDescription=null;
        this.comments=null;
    }
}
