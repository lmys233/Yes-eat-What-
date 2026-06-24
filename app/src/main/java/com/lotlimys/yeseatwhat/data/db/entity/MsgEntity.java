package com.lotlimys.yeseatwhat.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_message")
public class MsgEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "conversation_id")
    private long conversationId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "is_user")
    private boolean isUser;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public MsgEntity(long conversationId, String content, boolean isUser, long createdAt) {
        this.conversationId = conversationId;
        this.content = content;
        this.isUser = isUser;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getConversationId() { return conversationId; }
    public void setConversationId(long conversationId) { this.conversationId = conversationId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isUser() { return isUser; }
    public void setUser(boolean user) { isUser = user; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
