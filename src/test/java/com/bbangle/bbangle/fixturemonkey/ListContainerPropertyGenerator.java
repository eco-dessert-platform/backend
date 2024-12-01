package com.bbangle.bbangle.fixturemonkey;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

/**
 * 자식 엔티티 List 사이즈는 1 ~ 3으로 생성한다
 */
public class ListContainerPropertyGenerator implements ContainerPropertyGenerator {

    @Override
    public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
        // 원본 property 가져오기
        Property property = context.getProperty();

        // List의 제네릭 타입 가져오기
        List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
        if (elementTypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "List must have 1 generics type for element. " +
                            "propertyType: " + property.getType() +
                            ", elementTypes: " + elementTypes
            );
        }

        // List의 요소 타입
        AnnotatedType elementType = elementTypes.get(0);

        // 1에서 3 사이의 랜덤한 크기 결정
        ArbitraryContainerInfo containerInfo = new ArbitraryContainerInfo(1, 3);
        int size = containerInfo.getRandomSize();

        // 결정된 크기만큼 요소 프로퍼티 생성
        List<Property> elementProperties = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            elementProperties.add(
                    new ElementProperty(
                            property,
                            elementType,
                            i,  // sequence
                            i   // index
                    )
            );
        }

        // 최종 ContainerProperty 반환
        return new ContainerProperty(elementProperties, containerInfo);
    }
}
