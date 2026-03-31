package com.sky.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 大模型调用服务接口
 */
public interface AiService {
    /**
     * 调用大模型获取回复
     * @param sessionId 会话ID（用于从数据库加载历史记录实现记忆）
     * @param userMessage 用户当前输入
     * @return AI的回复内容
     */
    String getReply(Long sessionId, String userMessage);

    // 新增：流式获取回复的方法声明
    void streamGetReply(Long sessionId, String userMessage, SseEmitter emitter);
}
