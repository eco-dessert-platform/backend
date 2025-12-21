package com.bbangle.bbangle.fixturemonkey;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.ArbitraryUtils;
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import net.jqwik.api.Arbitraries;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FixtureMonkeyConfig {

    public static final FixtureMonkey fixtureMonkey;

    static {
        fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(new FailoverIntrospector(
                Arrays.asList(
                    FieldReflectionArbitraryIntrospector.INSTANCE,
                    BuilderArbitraryIntrospector.INSTANCE,
                    ConstructorPropertiesArbitraryIntrospector.INSTANCE
                ), false
            ))
            .plugin(new SimpleValueJqwikPlugin()
                .minContainerSize(1)
                .minNumberValue(1))
            .plugin(new JakartaValidationPlugin())
            .defaultNotNull(true)
            .pushAssignableTypeContainerPropertyGenerator(
                List.class,
                new ListContainerPropertyGenerator()
            )
            .pushAssignableTypeArbitraryIntrospector(
                Long.class,
                context -> new ArbitraryIntrospectorResult(
                    ArbitraryUtils.toCombinableArbitrary(
                        Arbitraries.longs().greaterOrEqual(0L)
                    )
                )
            )
            .pushAssignableTypeArbitraryIntrospector(
                BigDecimal.class,
                context -> new ArbitraryIntrospectorResult(
                    ArbitraryUtils.toCombinableArbitrary(
                        Arbitraries.bigDecimals()
                            .between(BigDecimal.valueOf(0), BigDecimal.valueOf(200))
                            .map(bd -> bd.setScale(2, RoundingMode.HALF_UP))
                    )
                )
            )
            .pushAssignableTypeArbitraryIntrospector(
                MultipartFile.class,
                context -> new ArbitraryIntrospectorResult(
                    ArbitraryUtils.toCombinableArbitrary(
                        Arbitraries.create(() -> new MockMultipartFile(
                            "리뷰 이미지",
                            "testImage.png",
                            MediaType.IMAGE_PNG_VALUE,
                            "testImage".getBytes()
                        ))
                    )
                )
            )
            .build();
    }
}
