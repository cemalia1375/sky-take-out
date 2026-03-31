package com.sky.mapper;

import com.sky.entity.ChatSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    /**
     * 新增聊天会话
     * 添加 @Options 注解来获取自增主键
     * @param chatSession
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into chat_sessions (admin_id, memory_id, session_title, last_message, create_time, update_time, is_deleted) " +
            "values (#{adminId}, #{memoryId}, #{sessionTitle}, #{lastMessage}, #{createTime}, #{updateTime}, #{isDeleted})")
    void insert(ChatSession chatSession);

    /**
     * 更新memory_id
     * @param id
     * @param memoryId
     */
    @Update("update chat_sessions set memory_id = #{memoryId} where id = #{id}")
    void updateMemoryId(@Param("id") Long id, @Param("memoryId") Long memoryId);

    /**
     * 根据管理员ID查询会话列表
     * @param adminId
     * @return
     */
    @Select("select s.*, " +
            "(select count(*) from chat_messages m where m.session_id = s.id) as message_count " + // 改为下划线
            "from chat_sessions s " +
            "where s.admin_id = #{adminId} and s.is_deleted = 0 " +
            "order by s.update_time desc")
    List<ChatSession> getByAdminId(Long adminId);

    /**
     * 根据sessionId获取memoryId获取上下文信息
     * @param sessionId
     * @return
     */
    @Select("select * from chat_sessions where id = #{id}")
    ChatSession getById(Long sessionId);

    /**
     * 更新会话表的最后一条消息和更新时间
     * @param session
     */
    @Update("update chat_sessions set last_message = #{lastMessage}, update_time = #{updateTime} " +
            "where id = #{id}")
    void update(ChatSession session);

    /**
     * 根据memory_id 查询
     * @param memoryId
     * @return
     */
    @Select("select * from chat_sessions where memory_id = #{memoryId}")
    ChatSession getByMemoryId(@Param("memoryId") Long memoryId);

    /**
     * 根据id删除聊天会话
     * @param realId
     */
    @Delete("delete from chat_sessions where id = #{id}")
    void deleteById(Long realId);
}
