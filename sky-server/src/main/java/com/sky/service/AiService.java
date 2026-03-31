package com.sky.service;

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
}
