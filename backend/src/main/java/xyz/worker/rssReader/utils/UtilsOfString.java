package xyz.worker.rssReader.utils;

import org.springframework.stereotype.Component;

@Component
public class UtilsOfString {
    public String removeHttpOfUrl(String url){
        if(url.contains("://")){
            String res=url.substring(url.indexOf("://")+3);
            res=res.replaceAll("[^0-9a-zA-Z]","");
            if(res.length()>32){
                res=res.substring(res.length()-32);
            }
            return res;
        }
        return url;
    }
}
