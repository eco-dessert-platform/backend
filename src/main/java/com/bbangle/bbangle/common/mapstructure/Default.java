package com.bbangle.bbangle.common.mapstructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
       MapStructure 로 매핑 시 기본 생성자로 지정해줄 수 있는 클래스
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.CLASS)
public @interface Default {

}
