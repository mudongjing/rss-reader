package xyz.worker.rssReader.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.worker.rssReader.pojo.PointDB;
import xyz.worker.rssReader.pojo.UrlRecord;
import xyz.worker.rssReader.utils.UtilsOfString;
import xyz.worker.rssReader.utils.redis.RedisConfigFactory;
import xyz.worker.rssReader.utils.sql.service.UrlRecordService;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.lang.Math.abs;

// 简化升级布隆过滤器
// 由于我们的信息元不至于数量很大，但不排除极端案例，我们不采用原有布隆过滤器在一个相对固定的区域通过不同哈希算法获得几个位置来完成一个元素的认定
// 并且布隆过滤器本身很难实现删除元素的操作
// 我们定义一个比特数组列表，和一个位图集合

/**
 * 比特数组链表用于记录一个元素哈希后的相关信息，主要是，利用可无限不重复的哈希方法，每次哈希一次获得的值取模分配到一个位置，
 * 放在一个比特数组中，自后再哈希，得到的位置到下一个比特数组中
 * 由此可以想象一个元素对应的信息成为一个高楼上各个层中的房间号
 * -------
 * 上述是简单的一个分配做法，本质与布隆过滤器没什么区别，但是我们不强调一个元素一定要有对应个数的位置数量，
 * 也就是有的元素可能做了5次哈希，而有的的元素却做了15次哈希。
 * 关键在于我们需要一个位图记录链表中比特数组中位置的特殊情况，就像前面看作高楼一样，从新按照一定顺序自然可以把二维的信息转变为一维的，
 * 每个比特数组的位置对应位图中的一个位置。
 * 比如一个元素做完了5次哈希后，最后停留的位置，将在位图对应的位置上标注为1，待之后删除时，再标注为0.
 * -------
 * 如何处理哈希结果。
 * 一个元素第一次的哈希结果对应的位置，防止完成后，根据奇偶性分类，各元素最终结束哈希计算的位置必须停留在对应奇偶性的层数上（还是看作高楼）。
 * 同时要求停止哈希计算的条件是，当前的结果是对应开始的奇偶性，同时前面两个哈希、后面一次的哈希结果是相反的。
 * -----------
 * 现在，我们的一个元素到达了一个位置，并且可以停留，且发现这里是一个停留点，如何确认当前的元素就是当初的元素，
 * 同之前的那个位图，把高楼的房间号一维话，只不过我们把每层的房间数量扩大，当初的元素计算自己的后一次哈希的位置取模的范围更大，
 * 同时加上前几次位置的结果，再取模，记录在对应的位置上。
 * 如果当前元素同样的操作能够到达这个位置，大概率可以认为一样。
 * 如果之前每层楼房间有256个，这个扩大的规模有256个。加之当前的元素无论中间的过程元素是否完全一样，至少达成对应的条件，需要前面3次的奇偶性符合，
 * 第4次位置完全一样，后面一次的计算也要一样，就是2^3*256*256，大概1/50多万的概率。
 * 这只是5次就达到条件，其它10几次，概率就更加微小。
 * -----
 * 另外，一个位置只能做一个元素的停留点，如果后加入的元素元达到对应的条件，或者相关的计算结果在第二个位图中存在冲突，也是不能停留的。
 * （因此，对于不同奇偶性的元素，最后一次计算也要通过手段保持自己具有对应的奇偶性，一定程度的避免冲突）
 * 停留失败，就需要继续计算下一次的停留位置，一定程度，也提高了后面确定的概率。
 * --------
 * 最后，还需要一个位图就是记录位置的纯洁性，在比特数组中，如果一个为题被记为1，表示有元素经历过，是我们判断的一个依据，
 * 如果，之后删除了某个元素，我们希望，尽可能地把它经历过的位置也归零，这个位图就是在位置再次为元素经历是记为1，之后就不好删除了，但是，
 * 为0的位置之后，还是容易删除的。
 * 比如，我们确定了一个元素要删除，却发现了它，并且它经历的位置中有的位置还是记为0的，那么就表示没有被其它元素利用，可以放心删除。
 * ------
 * 最后，我们需要的空间是，一个比特数组链表，对应的，一个记录停留点的位图，记录纯洁性，最后一次计算结果的位图。
 * 相当于4个位图。相当于用4个bit去记录一个元素。
 */

@Component
public class MyFilter {
    @Autowired
    private UtilsOfFilter utilsOfFilter;
    @Autowired
    private RedisConfigFactory redisConfigFactory;
    @Autowired
    private PointDB pointDB;
    @Autowired
    private UrlRecordService urlRecordService;
    @Autowired
    private UtilsOfString utilsOfString;

    static List<BitSet> building=new ArrayList<>();
    static BitSet stop=new  BitSet(Elelen.getCAPCITY());
    static BitSet pure=new BitSet(Elelen.getCAPCITY());
    static BitSet last=new BitSet(Elelen.getLASTCAPCITY());

    private String FILTERLIST="building";
    private String STOP="stop";
    private String PURE="pure";
    private String LAST="last";
    private String SUCC="success";

    @PostConstruct
    public void init(){
        RedisTemplate<String, Object> filterRedis = redisConfigFactory.getRedisTemplateByDb(pointDB.FILTER);
        if (building.size()==0){
//            filterRedis = redisConfigFactory.getRedisTemplateByDb(pointDB.FILTER);
//            if (filterRedis.hasKey(FILTERLIST) && filterRedis.hasKey(PURE) && filterRedis.hasKey(STOP) && filterRedis.hasKey(LAST) ){
//                JSONObject.toJavaObject(,List.class)
//                building = (List<BitSet>)filterRedis.opsForValue().get(FILTERLIST);
//                pure=(BitSet) filterRedis.opsForValue().get(PURE);
//                stop=(BitSet) filterRedis.opsForValue().get(STOP);
//                last=(BitSet) filterRedis.opsForValue().get(LAST);
//            }else{
                for (int i=5;i>0;i--)building.add(new BitSet(Elelen.getELEMS()));
//            }
        }
        List<UrlRecord> urlRecords = urlRecordService.show();
        String [] urls=new String[urlRecords.size()];
        int index=0;
        for (UrlRecord record:urlRecords){
            urls[index++]=utilsOfString.removeHttpOfUrl(record.getUrlContent());
        }
        batchAddEle(urls);
    }

    private String LIMIT="元素加入后产生的超出结果超出指定层数---------，"+Elelen.getLAYERS();
    private String CONFLICT="加入的元素与已有元素产生冲突 ************";
    // 计算二维数组一维化的位置
    private int calcuLE(int lay,int e){ return lay*Elelen.getELEMS()+e;  }

    // 如果超出限制就false
    private boolean setMap(int lay,int e){
        if (lay>=Elelen.getLAYERS()) return false;
        else{
            while(building.size()<lay+1){ building.add(new BitSet(Elelen.getELEMS()));  }
            BitSet rooms = building.get(lay);
            int oneDi = calcuLE(lay, e);//对应一维的位置
            if (rooms.get(e)==false) { rooms.set(e);//纯洁性不做处理
            }else{ pure.set(oneDi);//被污染
            }
        }
        return true;
    }

    //根据给定的奇偶性改变total的数值，正负1
    private int getTotal(boolean parity,List<Integer> res){
        int total=0;// 计算前几次位置的和
        for (Integer i:res) total+=i;
        Boolean aBoolean = judgeParity(total);
        if (aBoolean.booleanValue()!=parity) { if ((total+1)>=Elelen.getELEMS()) total= (total-1);    else total= total+1;        }
        return total % Elelen.getLASTELEMS();
    }

    // 如果存在冲突就返回false，不做处理，直接认为相同元素
    private  boolean setStop(int lay,int e,List<Integer> res,boolean parity){//计算对应位置是否存在已有的元素
        int total=getTotal(parity,res);
        int oneDi=lay*Elelen.getLASTELEMS()+total;
        if (!last.get(oneDi)) {
            stop.set(calcuLE(lay,e));
            last.set(oneDi);
            return true;
        }else return false;//不做处理，之后会作为不同经历点处理
    }

    public void batchAddEle(String [] ss){
        for(String s:ss) {
            RequestMess requestMess = addEle(s, true);
            if (requestMess.getSuccss().booleanValue()==true){ System.out.println("过滤器加载 "+s+" 成功");
            }else{ System.out.println("过滤器加载 "+s+" 失败 ："+requestMess.getMess()); }
        }
    }
    private int bernstein(String data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < data.length(); i++)
        hash = (hash ^ data.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >>7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }
    private int getHashOfStr(IndexAndAli indexAndAli){
        return abs(bernstein(indexAndAli.getS())) % Elelen.getELEMS();
    }
    // 将一个字符串元素添加到过滤器中
    public RequestMess addEle(String s,boolean batch){
        if (judgeHas(s)) return new RequestMess(false,CONFLICT);
        IndexAndAli indexAndAli = utilsOfFilter.hashS(s, null, null);
        // 之后的判断奇偶性，true为偶，false为奇

        List<Integer> res=new ArrayList<>();
        int v=getHashOfStr(indexAndAli);
        boolean parity = judgeParity(v);
        int lay=0;
        // 设置位图，纯洁性和经历点
        boolean b = setMap(lay++, v);
        if (!b) { return new RequestMess(false,LIMIT); }
        res.add(v);

        Queue<Boolean> pairs=new LinkedList<>();
        pairs.offer(parity);
        Boolean nParity;
        // 最终的目标是到达停留条件，完成停留操作
        // 并把当前的各个位图发送到redis中，如果是批量操作则不做这个操作
        while(true){
            boolean aDouble = isALL(pairs, parity);
            indexAndAli = utilsOfFilter.hashS(s, indexAndAli.getS(), indexAndAli.getIndex());
            v=getHashOfStr(indexAndAli);

            b = setMap(lay, v);
            if (!b) { return new RequestMess(false,LIMIT); }
            res.add(v);
            nParity = judgeParity(v);
//            System.out.println(s+" 奇偶 ： "+nParity +" 初始 ： "+parity);
            // 前两次亿满足条件，看当前是否符合，如果是，再看后一次
            Integer v1=null;
            Boolean mParity=null;
            Boolean mb=null;
            if (aDouble){
                // 当前也符合，看后一个是否也符合
                if (nParity.booleanValue()==parity){
                    // 新对象，避免覆盖前面还未处理的对象
                    indexAndAli = utilsOfFilter.hashS(s, indexAndAli.getS(), indexAndAli.getIndex());
                    v1=getHashOfStr(indexAndAli);
                    mParity=judgeParity(v1);
//                    System.out.println(s+" 奇偶 ： "+mParity +" 初始 ： "+parity);
                    if (mParity.booleanValue()!=parity){
                        // 达到停留条件
                        mb = setStop(lay, v,res,parity);// 未出现冲突
                        if (mb) {
                            //if (!batch){ saveB();  }
                            succSave(s);
                            return new RequestMess(true,null);
                        }
                        else return new RequestMess(false,CONFLICT);// 出现冲突，失败
                    }
                }
            }
            lay++;
            // 未达成条件
            while(pairs.size()>=Elelen.getQUEUECAP()) pairs.poll();
            pairs.offer(nParity);
            if (mParity!=null){
                while(pairs.size()>=Elelen.getQUEUECAP()) pairs.poll();
                pairs.offer(mParity);
                boolean b1 = setMap(lay++, v1);
                if (!b1) return new RequestMess(false,LIMIT);
                res.add(v1);
            }
        }
    }
    private void saveB(){

        RedisTemplate<String, Object> filterRedis = redisConfigFactory.getRedisTemplateByDb(pointDB.FILTER);
        filterRedis.opsForValue().set(FILTERLIST,JSONObject.toJSONString(building));
        filterRedis.opsForValue().set(STOP,JSONObject.toJSONString(stop));
        filterRedis.opsForValue().set(PURE,JSONObject.toJSONString(pure));
        filterRedis.opsForValue().set(LAST,JSONObject.toJSONString(last));
    }
    private void succSave(String s){
        RedisTemplate<String, Object> filterRedis = redisConfigFactory.getRedisTemplateByDb(pointDB.FILTER);
        filterRedis.opsForSet().add(SUCC,s);
    }
    private boolean wasSucc(String s){
        RedisTemplate<String, Object> filterRedis = redisConfigFactory.getRedisTemplateByDb(pointDB.FILTER);
        return filterRedis.opsForSet().isMember(SUCC,s);
    }
    private boolean isALL(Queue<Boolean> queue,boolean is){
        if (queue.size()==Elelen.getQUEUECAP()){
            for (Boolean q:queue){ if (q.booleanValue()==is) return false; }
            return true;
        }
        return false;
    }

    // 查找这个字符串，并删除它对应的停留点等信息
    //先找自己的停留点
    // 顺便如果自己的经历点还是纯洁的，则表明可以删除
    // 逐个计算
    public void delEle(String s){
        if (!wasSucc(s)) return ;
        IndexAndAli indexAndAli = utilsOfFilter.hashS(s, null, null);
        List<Integer> res=new ArrayList<>();
        int v=getHashOfStr(indexAndAli);
        boolean parity = judgeParity(v);
        int lay=0;
        res.add(v);
        delExperience(lay,v);
        Queue<Boolean> pairs=new LinkedList<>();
        pairs.offer(parity);
        Boolean nParity;
        while(true){
            boolean aDouble = isALL(pairs, parity);
            indexAndAli = utilsOfFilter.hashS(s, indexAndAli.getS(), indexAndAli.getIndex());
            v=getHashOfStr(indexAndAli);
            res.add(v);
            nParity = judgeParity(v);
            Integer v1=null;
            Boolean mParity=null;
            if (aDouble){
                // 当前也符合，看后一个是否也符合
                if (nParity.booleanValue()==parity){
                    // 新对象，避免覆盖前面还未处理的对象
                    indexAndAli = utilsOfFilter.hashS(s, indexAndAli.getS(), indexAndAli.getIndex());
                    v1=getHashOfStr(indexAndAli);
                    mParity=judgeParity(v1);
                    if (mParity.booleanValue()!=parity){// 达到停留条件,直接去判断最后的数值
                        int total = getTotal(parity, res);
                        int oneDi=lay*Elelen.getLASTELEMS()+total;
                        if (last.get(oneDi) && stop.get(calcuLE(lay,v))) {
                            delLastPoint(lay,v,oneDi);
                            saveB();
                        }
                    }
                }
            }
            lay++;
            // 没有达到停留条件
            while(pairs.size()>=Elelen.getQUEUECAP()) pairs.poll();
            pairs.offer(nParity);
            if (mParity!=null){
                while(pairs.size()>=Elelen.getQUEUECAP()) pairs.poll();
                pairs.offer(mParity);
                res.add(v1);
                delExperience(lay++,v1);
            }
        }
    }
    private void delExperience(int lay,int e){
        int i=calcuLE(lay,e);
        if (!pure.get(i)){
            BitSet build = building.get(lay);
            build.clear(e);
        }
    }
    private void delLastPoint(int lay,int v,int oneDi){
        delExperience(lay,v);
        stop.clear(calcuLE(lay,v));
        last.clear(oneDi);
    }

    private boolean hasEx(int lay,int v){
        if (lay>=Elelen.getLAYERS()) return false;
        BitSet build = building.get(lay);
        if (build.get(v)) return true;
        else return false;
    }
    private boolean isLast(int lay,int v,int oneDi){
        if (stop.get(calcuLE(lay,v))){
            if (last.get(oneDi)) return true;
        }
        return false;
    }
    // 判断一个字符串是否是已有元素
    public boolean judgeHas(String s){
        IndexAndAli indexAndAli = utilsOfFilter.hashS(s, null, null);
        List<Integer> res=new ArrayList<>();
        int v=getHashOfStr(indexAndAli);
        boolean parity = judgeParity(v);
        int lay=0;
        res.add(v);
        if(!hasEx(lay,v)) return false;
        Queue<Boolean> pairs=new LinkedList<>();
        pairs.offer(parity);
        Boolean nParity;
        while(true){
            boolean aDouble = isALL(pairs, parity);
            indexAndAli = utilsOfFilter.hashS(s, indexAndAli.getS(), indexAndAli.getIndex());
            v=getHashOfStr(indexAndAli);
            res.add(v);
            nParity = judgeParity(v);
            Integer v1=null;
            Boolean mParity=null;
            if (aDouble){
                // 当前也符合，看后一个是否也符合
                if (nParity.booleanValue()==parity){
                    // 新对象，避免覆盖前面还未处理的对象
                    indexAndAli = utilsOfFilter.hashS(s, indexAndAli.getS(), indexAndAli.getIndex());
                    v1=getHashOfStr(indexAndAli);
                    mParity=judgeParity(v1);
                    if (mParity.booleanValue()!=parity){// 达到停留条件,直接去判断最后的数值
                        int total = getTotal(parity, res);
                        int oneDi=lay*Elelen.getLASTELEMS()+total;
                        if (isLast(lay,v,oneDi)) return true;
                        else return false;
                    }
                }
            }
            lay++;
            // 没有达到停留条件
            while(pairs.size()>=Elelen.getQUEUECAP()) pairs.poll();
            pairs.offer(nParity);
            if (mParity!=null){
                while(pairs.size()>=Elelen.getQUEUECAP()) pairs.poll();
                pairs.offer(mParity);
                res.add(v1);
                delExperience(lay++,v1);
            }
        }
    }

    private Boolean judgeParity(Integer value){
        int i=value.intValue();
        if ((i & 1)==1) return false;else return true;
    }
}
