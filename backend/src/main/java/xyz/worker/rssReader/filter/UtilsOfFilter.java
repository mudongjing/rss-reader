package xyz.worker.rssReader.filter;

import org.springframework.stereotype.Component;

@Component
public class UtilsOfFilter {
    //哈希的方式就是对字符串中的元素逐个复制一个元素拼接在后面，就可以得到新的字符串计算哈希，
    // 通过这种方式，可以无限制的复制一轮再一轮，达到无穷

    /**
     *
     * @param ori 原始的字符串
     * @param alien 处理得到的字符串
     * @param index 上次处理的字符的索引位置
     * @return
     */
    public IndexAndAli  hashS(String ori,String alien,Integer index){
        int length = ori.length();
        IndexAndAli indexAndAli;
        if(alien==null){
            indexAndAli = new IndexAndAli(ori, length-1);
        }else{
            if (index==length-1) index=0;else index+=1;
            alien=alien+ori.charAt(index);
            indexAndAli = new IndexAndAli(alien, index);
        }
        return indexAndAli;
    }
}
