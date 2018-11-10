package me.hiepdoan.jrqueue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {
  public static final Integer DEFAULT_EXPIRATION_DURATION = 24;


  @Value("${spring.redis.host}")
  private String redisServerHost;

  @Value("${spring.redis.port}")
  private Integer redisServerPort;

  @Value("${spring.redis.password}")
  private String redisPassword;

  @Autowired
  @Qualifier("redisObjectMapper")
  private ObjectMapper objectMapper;


  @PostConstruct
  public void init() {
    RedisConnectionFactory connectionFactory = redisConnectionFactory();
    //check if the connection to Redis server is setup properly
    if (connectionFactory == null
        || connectionFactory.getConnection() == null
        || connectionFactory.getConnection().ping() == null) {
      log.error("Redis server is not available");
    }

    log.info(String.format("Redis server is enabled at %s:%d", redisServerHost, redisServerPort));
  }

  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {

    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisServerHost, redisServerPort);
    configuration.setPassword(RedisPassword.of(redisPassword));
    return new LettuceConnectionFactory(configuration);
  }

  @Bean
  @Primary
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
    redisTemplate.setKeySerializer(new StringRedisSerializer());

    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }


  private RedisSerializationContext.SerializationPair getKeySerializer() {
    return RedisSerializationContext.SerializationPair.fromSerializer(
        new StringRedisSerializer());
  }

  private RedisSerializationContext.SerializationPair getValueSerializer() {
    return RedisSerializationContext.SerializationPair.fromSerializer(
        new GenericJackson2JsonRedisSerializer(objectMapper));
  }
}
