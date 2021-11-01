package xyz.worker.rssReader.utils.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Configuration
public class RedisConfigFactory {
    //redis地址
    @Value("${redis.host}")
    private String host;

    //redis端口号
    @Value("${redis.port}")
    private int port;

    //redis密码
    @Value("${redis.password}")
    private String password;

    //默认数据库
    private int defaultDB;

    //多个数据库集合
    @Value("${redis.dbs}")
    private List<Integer> dbList;

    //RedisTemplate实例
    private static Map<Integer, RedisTemplate<String, Object>> redisTemplateMap = new HashMap<>();

    /**
     * 初始化连接池
     */
    @PostConstruct
    public void initRedisTemplate() {
        defaultDB = dbList.get(0);//设置默认数据库
        for (Integer db : dbList) {
            //存储多个RedisTemplate实例
            redisTemplateMap.put(db, redisTemplate(db));
        }
    }

    public LettuceConnectionFactory redisConnection(int db) {
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration();
        server.setHostName(host); // 指定地址
        server.setDatabase(db); // 指定数据库
        server.setPort(port); //指定端口
        server.setPassword(password); //指定密码
        LettuceConnectionFactory factory = new LettuceConnectionFactory(server);
        factory.afterPropertiesSet(); //刷新配置
        return factory;
    }

    //RedisTemplate模板
    public RedisTemplate<String, Object> redisTemplate(int db) {
        //为了开发方便，一般直接使用<String,Object>
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnection(db)); //设置连接
        //Json序列化配置
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(),ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //String的序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        //hash的key采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        //value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        //hash序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 指定数据库进行切换
     * @param db  数据库索引
     * @return
     */
    public RedisTemplate<String, Object> getRedisTemplateByDb(int db) {
        return redisTemplateMap.get(db);
    }

    /**
     * 使用默认数据库
     *
     * @return
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplateMap.get(defaultDB);
    }
}