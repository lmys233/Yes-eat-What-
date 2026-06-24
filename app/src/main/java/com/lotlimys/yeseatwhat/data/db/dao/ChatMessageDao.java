package com.lotlimys.yeseatwhat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lotlimys.yeseatwhat.data.db.entity.MsgEntity;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert
    long insert(MsgEntity message);

    @Query("SELECT * FROM chat_message WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    List<MsgEntity> getByConversationId(long conversationId);

    @Query("DELETE FROM chat_message WHERE conversation_id = :conversationId")
    void deleteByConversationId(long conversationId);
}
