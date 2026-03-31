package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatVO {
    private Long sessionId;
    private String botReply;  // AI助手的回复内容
    private Long timestamp;
    private String messageId;
}
