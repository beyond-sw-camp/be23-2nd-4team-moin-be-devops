package com.team4.moin.common.configs;

import com.team4.moin.Notification.service.NotificationSseService;
import com.team4.moin.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;



    @Bean
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("rtInventory")
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("emailInventory")
    public RedisConnectionFactory emailInventoryConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("emailInventory")
    public RedisTemplate<String, String> emailInventoryTemplate(@Qualifier("emailInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    //    연결 기본 객체
    @Bean
    @Qualifier("chatPubSub")
    public RedisConnectionFactory chatPubSubFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
//        redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음.
        configuration.setDatabase(2);
        return  new LettuceConnectionFactory(configuration);
    }

    //    publish 객체
//    위의 연결 객체를 publish할 때도 주입을 받고, String 형태로 publish 하겠다
    @Bean
    @Qualifier("chatPubSub")
//    일반적으로는 RedisTemplate<key데이터타입, value데이터타입>을 사용
//    StringRedisTemplate은 @Qualifier("chatPubSub") 이 이름의 RedisConnectionFactory를 받아서 만들어짐.
    public StringRedisTemplate stringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory){
        return new StringRedisTemplate(redisConnectionFactory);
    }


    //    subscribe 객체
//    얘도 connectionFactory 먼저 주입 받고, "chat"이라고 하는 채널, 토픽 또는 방에 메시지가 들어오면
//    messageListenerAdapter한테 던지겠다.
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
//            connectionFactory 주입받음(1)
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
//        subscribe의 역할 : chat이라고 하는 채널, 토픽에 messageListenerAdapter를 던지겠다.(2)
        container.addMessageListener(messageListenerAdapter, new ChannelTopic("chat"));
        container.addMessageListener(messageListenerAdapter, new ChannelTopic("chat-read"));
        return container;
    }

    //    redis에서 수신된 메시지를 처리하는 객체 생성
//    그 어댑터는 subscribe 메시지를 redisPubSubService라고하는 클래스의 onMessage메서드에 위임하겠다고 지정.
//    onMessage를 이제 만들어야함.
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService){
//        RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정.
//        subscribe가 되면 redisPubSubService 안의 onMessage 메서드가 호출된다.
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

    //    redis pub/sub관런
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(9);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, Object> ssePubSubRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean("notificationRedisMessageListenerContainer")
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer notificationRedisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
            @Qualifier("ssePubSub") MessageListenerAdapter notificationMessageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(notificationMessageListenerAdapter, new ChannelTopic("notification-channel"));
        return container;
    }

    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter notificationMessageListenerAdapter(NotificationSseService notificationSseService) {
        return new MessageListenerAdapter(notificationSseService, "onMessage");
    }
    // RedisConfig 포트원 결제 관련 레디스 추가
    @Bean
    @Qualifier("paymentInventory")
    public RedisConnectionFactory paymentConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(3); // 결제용 DB 분리
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("paymentInventory")
    public RedisTemplate<String, String> paymentTemplate(@Qualifier("paymentInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
    // 크루 찜하기
    @Bean
    @Qualifier("crewFavoriteInventory")
    public RedisConnectionFactory crewFavoriteInventoryConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(4);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("crewFavoriteInventory")
    public RedisTemplate<String, String> crewFavoriteInventoryTemplate(@Qualifier("crewFavoriteInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
    // 크루 조회수
    @Bean
    @Qualifier("crewViewInventory")
    public RedisConnectionFactory crewViewInventoryConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(5);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("crewViewInventory")
    public RedisTemplate<String, String> crewViewInventoryTemplate(@Qualifier("crewViewInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("crewCaching")
    public RedisConnectionFactory crewCacheInventoryConnection() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(6);
        return new LettuceConnectionFactory(configuration);
    }

}
