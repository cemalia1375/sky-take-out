package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "langchain4j.dashscope.chat-model")
public class DashScopeProperties {
    private String apiKey;
    private String modelName;
}
