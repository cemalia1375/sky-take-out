package com.sky.controller;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController; // 记得导入这个


@RestController // 关键：告诉 Spring 这是一个 Web 控制器并由 Spring 管理
public class AssistantTest {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @GetMapping(value = "/ai/test", produces = "text/plain;charset=UTF-8")
    public String test(String prompt) {
        // 这里的 prompt 是访问时传的参数，例如：localhost:8080/ai/test?prompt=你好
        return chatLanguageModel.generate(prompt);
    }
}