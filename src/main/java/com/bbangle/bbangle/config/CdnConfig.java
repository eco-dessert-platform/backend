package com.bbangle.bbangle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CdnConfig {

    private static String CLOUD_FRONT_URL;

    @Value("${cdn.domain}")
    public void setCloudFrontUrl(String cloudFrontUrl) {
        CLOUD_FRONT_URL = cloudFrontUrl;
    }

    public static String getCloudFrontUrl() {
        return CLOUD_FRONT_URL;
    }

}
