package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatSessionVO {
    @JsonProperty("sessionId")
    private Long id;
    private Long memoryId;
    private String sessionTitle;
    private String lastMessage;
    private Integer messageCount;
    // 加在字段上方，控制日期序列化格式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    // 包含该会话下的所有消息记录
    private List<ChatMessageVO> messages;
}
