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
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
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

    //流式调用不能直接返回 String
    //需要使用 streamCall 并通过 SseEmitter 推送数据
    @Override
    public void streamGetReply(Long sessionId, String userMessage, SseEmitter emitter) {
        // 【调试点 1】确认入口处的 ID
        log.info("进入 streamGetReply, 接收到的 sessionId 为: {}", sessionId);

        try {
            Generation gen = new Generation();
            List<Message> msgList = new ArrayList<>();

            // 1. 身份定义
            msgList.add(Message.builder().role(Role.SYSTEM.getValue())
                    .content("你是苍穹外卖的智能助手，专门负责解答外卖业务相关问题。").build());

            // 2. 加载历史 (复用你原来的逻辑)
            List<ChatMessage> history = chatMessageMapper.getLatestMessages(sessionId, 10);
            Collections.reverse(history);
            for (ChatMessage m : history) {
                msgList.add(Message.builder().role(m.getRole()).content(m.getContent()).build());
            }

            // 3. 当前消息
            msgList.add(Message.builder().role(Role.USER.getValue()).content(userMessage).build());

            GenerationParam param = GenerationParam.builder()
                    .apiKey(properties.getApiKey())
                    .model(properties.getModelName())
                    .messages(msgList)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .incrementalOutput(true) // 关键：开启增量输出，这样每次只返回新蹦出来的字
                    .build();

            // 4. 调用流式接口
            Flowable<GenerationResult> result = new Generation().streamCall(param);

            // 使用 StringBuilder 承载完整回复
            StringBuilder fullReply = new StringBuilder();

            result.subscribe(
                    data -> {
                        String content = data.getOutput().getChoices().get(0).getMessage().getContent();
                        if (content != null && !content.isEmpty()) {
                            fullReply.append(content);
                            try {
                                //  关键修改：直接发送纯文本（不要 event 包装）
                                emitter.send(content);
                            } catch (IOException e) {
                                log.error("发送消息失败: {}", e.getMessage());
                            }
                        }
                    },
                    error -> {
                        log.error("流式调用异常: {}", error.getMessage());
                        try {
                            //  错误也用纯文本（可选加前缀）
                            emitter.send("ERROR: " + error.getMessage());
                        } catch (IOException e) {
                            log.error("发送错误消息失败", e);
                        }
                        emitter.completeWithError(error);
                    },
                    () -> {
                        String finalContent = fullReply.toString();
                        log.info("流结束。准备保存 ID: {}, 内容长度: {}", sessionId, finalContent.length());

                        if (sessionId != null && !finalContent.isEmpty()) {
                            saveAiMessage(sessionId, finalContent);
                        }

                        try {
                            //  结束标记也改为纯文本
                            emitter.send("[DONE]");
                        } catch (IOException e) {
                            log.error("发送结束标记失败", e);
                        }

                        emitter.complete();
                    }
            );

        } catch (Exception e) {
            log.error("流式启动失败: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }

    // 私有辅助方法：保存消息
    private void saveAiMessage(Long sessionId, String content) {
        log.info("准备保存AI消息，sessionId为: {}", sessionId);
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(content);
        aiMsg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(aiMsg);
    }

}
