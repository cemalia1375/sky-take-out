package com.sky.mapper;

import com.sky.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    /**
     * 插入 AI 的回复消息到数据库
     * @param chatMessage
     */
    @Insert("insert into chat_messages (session_id, role, content, create_time) " +
            "values (#{sessionId}, #{role}, #{content}, #{createTime})")
     void insert(ChatMessage chatMessage);

    /**
     * 获取最近的十条信息
     * @param sessionId
     * @param i
     * @return
     */
    /**
     * 获取指定会话最近的 N 条消息
     * 注意：这里先按时间倒序排，取最新的 limit 条
     */
    @Select("select * from chat_messages " +
            "where session_id = #{sessionId} " +
            "order by create_time desc limit #{limit}")
    List<ChatMessage> getLatestMessages(@Param("sessionId")Long sessionId, @Param("limit")int i);

    /**
     * 根据会话ID获取所有消息，按时间正序排列
     * @param sessionId
     * @return
     */
    @Select("select id, session_id as sessionId, role, content, create_time as createTime " +
            "from chat_messages where session_id = #{sessionId} order by create_time asc")
    List<ChatMessage> getBySessionIdOrderByTime(Long sessionId);
}
