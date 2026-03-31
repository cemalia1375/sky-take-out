package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;

    // 关联的会话ID
    private Long sessionId;

    // 角色：'user' (用户) 或 'assistant' (AI助手)
    // 这个字段非常重要，调用大模型时需要根据这个判断是谁说的
    private String role;

    // 消息的具体文本内容
    private String content;

    // 消息产生的时间
    private LocalDateTime createTime;
}
