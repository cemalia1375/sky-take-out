package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ChatDTO;
import com.sky.entity.ChatMessage;
import com.sky.entity.ChatSession;
import com.sky.exception.BaseException;
import com.sky.mapper.ChatMessageMapper;
import com.sky.mapper.ChatSessionMapper;
import com.sky.service.AiService;
import com.sky.service.ChatSessionService;
import com.sky.vo.ChatSessionCreateVO;
import com.sky.vo.ChatSessionVO;
import com.sky.vo.ChatVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private AiService aiService; // 假设你封装了 DashScope 的调用逻辑

    /**
     * 创建新对话
     * @return
     */
    public ChatSessionCreateVO createNewSession(){
        Long adminId = BaseContext.getCurrentId();
        log.info("创建新对话，管理员id:{}",adminId);
        // 修正：这里应该生成一个唯一的 memoryId，而不是调用方法自己
        // 建议使用时间戳或 UUID 简易替代，或者使用项目已有的 IdWorker
        Long memoryId = System.currentTimeMillis();
        ChatSession chatSession = ChatSession
                .builder()
                .adminId(adminId)
                .memoryId(memoryId)
                .sessionTitle("新对话")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .lastMessage("你好，我是苍穹小厨，有什么可以帮你？")
                .isDeleted(0)
                .build();
        chatSessionMapper.insert(chatSession);

        //返回结果
        ChatSessionCreateVO result = new ChatSessionCreateVO();
        result.setSessionId(chatSession.getId());
        result.setMemoryId(memoryId);

        log.info("创建对话成功：sessionId：｛｝,memoryId：｛｝",chatSession.getId(),memoryId);
        return result;
    }

    /**
     * 获取管理员会话列表
     * @return
     */
    @Override
    public List<ChatSessionVO> getSessionList() {
        // 1. 获取当前登录管理员ID
        Long adminId = BaseContext.getCurrentId();

        // 2. 查询数据库
        List<ChatSession> sessions = chatSessionMapper.getByAdminId(adminId);

        // 3. 转换为 VO 列表（可以使用 BeanUtils 拷贝）
        return sessions.stream().map(session -> {
            ChatSessionVO vo = new ChatSessionVO();
            BeanUtils.copyProperties(session, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 发送信息
     * @param chatDTO
     * @return
     */
    @Transactional
    public ChatVO send(ChatDTO chatDTO){
        Long sessionId = chatDTO.getSessionId();
        String userMsg = chatDTO.getMessage();

        // 1. 获取会话信息（拿到 memoryId 用于上下文记忆）
        ChatSession session = chatSessionMapper.getById(sessionId);
        if(session == null){
            throw new BaseException("会话不存在");
        }

        // 2. 保存用户发送的消息到数据库
        // 先存用户消息，再调 AI，最后存 AI 消息
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(userMsg)
                .createTime(LocalDateTime.now())
                .build();
        chatMessageMapper.insert(userMessage);

        // 3. 调用大模型 API 获取回复
        // 注意：这里传入 session.getMemoryId() 以便大模型记住之前的聊天
        String botReply = aiService.getReply(session.getMemoryId(), userMsg);

        // 4. 保存 AI 的回复消息到数据库
        ChatMessage botMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content(botReply)
                .createTime(LocalDateTime.now())
                .build();
        chatMessageMapper.insert(botMessage);

        // 5. 更新会话表的最后一条消息和更新时间
        session.setLastMessage(botReply);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.update(session);

        // 6. 封装并返回结果
        return ChatVO.builder()
                .sessionId(sessionId)
                .botReply(botReply)
                .timestamp(System.currentTimeMillis())
                .messageId("msg_" + UUID.randomUUID().toString().replace("-", ""))
                .build();
    }
}

