package com.sky.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.sky.entity.ChatMessage;
import com.sky.mapper.ChatMessageMapper;
import com.sky.properties.DashScopeProperties;
import com.sky.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Autowired
    private DashScopeProperties properties;
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public String getReply(Long sessionId, String userMessage) {
        try {
            Generation gen = new Generation();

            // 1. 构建消息列表（实现“记忆”的核心）
            List<Message> msgList = new ArrayList<>();

            // 系统提示词（可选，定义AI的身份）
            msgList.add(Message.builder().role(Role.SYSTEM.getValue())
                    .content("你是苍穹外卖的智能助手，专门负责解答外卖业务相关问题。").build());

            // 2. 加载历史消息（建议只加载最近10条，防止Token溢出）
            List<ChatMessage> history = chatMessageMapper.getLatestMessages(sessionId, 10);
            ////顺序反转过来:将 [新->旧] 翻转为 [旧->新]
            Collections.reverse(history);
            for (ChatMessage m : history) {
                msgList.add(Message.builder()
                        .role(m.getRole()) // 'user' 或 'assistant'
                        .content(m.getContent())
                        .build());
            }

            // 3. 加入当前用户消息
            msgList.add(Message.builder().role(Role.USER.getValue())
                    .content(userMessage).build());

            // 4. 配置参数并调用
            GenerationParam param = GenerationParam.builder()
                    .apiKey(properties.getApiKey())
                    .model(properties.getModelName())
                    .messages(msgList)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE) // 建议使用MESSAGE格式
                    .build();

            GenerationResult result = gen.call(param);

            // 5. 解析并返回AI文本
            return result.getOutput().getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            log.error("调用DashScope出错: {}", e.getMessage());
            return "抱歉，我现在思绪有点乱，请稍后再试。";
        }
    }
}
