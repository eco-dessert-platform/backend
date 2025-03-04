package com.bbangle.bbangle.config;

import java.util.List;

import com.bbangle.bbangle.common.service.RequestTimeInterceptor;
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

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestTimeInterceptor);
  }

}
