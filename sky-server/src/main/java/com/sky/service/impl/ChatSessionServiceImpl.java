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
import com.sky.vo.ChatMessageVO;
import com.sky.vo.ChatSessionCreateVO;
import com.sky.vo.ChatSessionVO;
import com.sky.vo.ChatVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

        // 1. 先构建并插入数据库，让 MyBatis 填充自增 ID
        ChatSession chatSession = ChatSession.builder()
                .adminId(adminId)
                // 关键修改：直接把 memoryId 也设为和后续 ID 一致（或者暂时占位）
                .memoryId(0L)
                .sessionTitle("新对话")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .lastMessage("你好，我是苍穹小厨，有什么可以帮你？")
                .isDeleted(0)
                .build();

        chatSessionMapper.insert(chatSession); // 执行完这行，chatSession.getId() 就有值了

        // 2. 修正：将真实的数据库 ID 赋值给 memoryId
        // 这样无论前端取哪个字段，拿到的都是那个能查到历史记录的“正确数字”
        Long realId = chatSession.getId();
        chatSession.setMemoryId(realId);
        chatSessionMapper.updateMemoryId(realId,realId); // 更新回数据库，确保一致性

        // 3. 返回结果：全都给它 realId
        ChatSessionCreateVO result = new ChatSessionCreateVO();
        result.setSessionId(realId);
        result.setMemoryId(realId);

        log.info("创建对话成功：数据库ID：{}", realId);
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
            return ChatSessionVO.builder()
                    .id(session.getId())
                    .memoryId(session.getMemoryId())
                    .sessionTitle(session.getSessionTitle())
                    .lastMessage(session.getLastMessage())
                    .createTime(session.getCreateTime())
                    .updateTime(session.getUpdateTime())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 发送信息（流式版本）
     * @param chatDTO
     * @return SseEmitter
     */
    public SseEmitter send(ChatDTO chatDTO) {
        Long sessionId = chatDTO.getSessionId();
        String userMsg = chatDTO.getMessage();

        // 1. 兼容性获取会话（因为前端可能传的是 memory_id）
        ChatSession session = chatSessionMapper.getById(sessionId);
        if (session == null) {
            session = chatSessionMapper.getByMemoryId(sessionId);
        }
        if (session == null) {
            throw new BaseException("会话不存在");
        }

        // 2. 保存用户发送的消息 (注意使用数据库真实 ID：session.getId())
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(session.getId())
                .role("user")
                .content(userMsg)
                .createTime(LocalDateTime.now())
                .build();
        chatMessageMapper.insert(userMessage);

        // 3. 创建 SSE 发射器
        SseEmitter emitter = new SseEmitter(0L); // 0表示永不超时

        // 4. 调用流式 AI 服务
        // 关键：传入 session.getId() 确保 AiService 拿到的不是 null
        aiService.streamGetReply(session.getId(), userMsg, emitter);

        return emitter;
    }

    /**
     * 获取会话历史
     * @param sessionId
     * @return
     */
    @Override
    public ChatSessionVO getHistoryBySessionId(Long sessionId) {
        log.info("开始查询历史记录，接收到的 sessionId 为: {}", sessionId);
        // 1. 查询会话基础信息
        ChatSession chatSession = chatSessionMapper.getById(sessionId);

        // 2. 如果没查到，说明传的是 memory_id，尝试用 memory_id 查
        if (chatSession == null) {
            log.info("主键未找到，尝试作为 memory_id 查询...");
            chatSession = chatSessionMapper.getByMemoryId(sessionId);
        }

        if (chatSession == null) {
            throw new BaseException("会话不存在");
        }

        // 3. 查询该会话下的所有聊天消息 (这里建议查询全部，或者增加分页)，必须使用 chatSession 对象中真实的主键 id
        // 注意：Mapper中原有的 getLatestMessages 是倒序排的，我们需要正序给前端展示

        // 关键修复点：这里不能直接传参数 sessionId (它是 memoryId)
        // 必须传 chatSession.getId()，这才是消息表里存的 session_id
        List<ChatMessage> chatMessages = chatMessageMapper.getBySessionIdOrderByTime(chatSession.getId());

        // 4. 将 Entity 转换为 VO
        List<ChatMessageVO> messageVOList = chatMessages.stream().map(message -> {
            // 增加判空逻辑：如果数据库里没时间，就给个当前时间，防止崩溃
            LocalDateTime createTime =  message.getCreateTime() != null ?
                                        message.getCreateTime() : LocalDateTime.now();

            return ChatMessageVO.builder()
                    .role(message.getRole())
                    .content(message.getContent())
                    // 将 LocalDateTime 转换为 13位时间戳
                    .timestamp(createTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();
        }).collect(Collectors.toList());

        // 4. 封装返回结果
        return ChatSessionVO.builder()
                .id(chatSession.getId())
                .sessionTitle(chatSession.getSessionTitle())
                .messages(messageVOList)
                .build();
    }

    /**
     * 删除会话记录
     * @param sessionId
     */
    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        log.info("开始删除会话: {}", sessionId);

        // 1. 查会话（兼容 memoryId）
        ChatSession session = chatSessionMapper.getById(sessionId);
        if (session == null) {
            session = chatSessionMapper.getByMemoryId(sessionId);
        }

        if (session == null) {
            throw new BaseException("会话不存在");
        }

        Long realId = session.getId();

        // 2. 删除聊天记录
        chatMessageMapper.deleteBySessionId(realId);

        // 3. 删除会话
        chatSessionMapper.deleteById(realId);

        log.info("删除成功 sessionId={}", realId);
    }
}

