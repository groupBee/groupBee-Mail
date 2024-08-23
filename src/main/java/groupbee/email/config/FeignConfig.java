package groupbee.email.config;

import feign.Logger;
import feign.RequestInterceptor;
import groupbee.email.interceptor.SessionInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    // SessionInterceptor를 직접 빈으로 등록
    @Bean
    public SessionInterceptor sessionInterceptorBean() {
        return new SessionInterceptor();
    }

    // RequestInterceptor로도 사용 가능하게 하기 위해 SessionInterceptor 빈을 주입받아 리턴
    @Bean
    public RequestInterceptor sessionInterceptor(SessionInterceptor sessionInterceptorBean) {
        return sessionInterceptorBean;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            System.out.println("Request: " + template.url());
            template.headers().forEach((key, value) ->
                    System.out.println(key + ": " + value));
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
