package com.bbangle.bbangle.config;
import com.bbangle.bbangle.common.service.RequestTimeInterceptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final RequestTimeInterceptor requestTimeInterceptor;
  private final OctetStreamReadMsgConverter octetStreamReadMsgConverter;

  @Autowired
  public WebConfig(
      OctetStreamReadMsgConverter octetStreamReadMsgConverter,
      RequestTimeInterceptor requestTimeInterceptor) {
    this.octetStreamReadMsgConverter = octetStreamReadMsgConverter;
    this.requestTimeInterceptor = requestTimeInterceptor;
  }


  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(octetStreamReadMsgConverter);
  }

  // 불필요한 swagger 로그 찍히는 부분 설정
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestTimeInterceptor)
        .addPathPatterns("/**") 
        .excludePathPatterns(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/favicon.ico",
            "/error"
        );
  }

}
