package com.lotlimys.yeseatwhat.data.repository;

import android.content.Context;

import com.lotlimys.yeseatwhat.ai.AIService;
import com.lotlimys.yeseatwhat.ai.DashScopeService;
import com.lotlimys.yeseatwhat.ai.OpenAIService;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.dao.ChatMessageDao;
import com.lotlimys.yeseatwhat.data.db.entity.MsgEntity;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {
    private final ChatMessageDao messageDao;
    private final AIService aiService;

    public ChatRepository(Context context) {
        this.messageDao = AppDatabase.getInstance(context).chatMessageDao();
        AppPreferences prefs = AppPreferences.getInstance(context);
        if (Constants.PROVIDER_QWEN.equals(prefs.getAiProvider())) {
            this.aiService = new DashScopeService(prefs);
        } else {
            this.aiService = new OpenAIService(prefs);
        }
    }

    public void sendMessage(long conversationId, String text, Callback callback) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            // Save user message
            MsgEntity userMsg = new MsgEntity(conversationId, text, true, System.currentTimeMillis());
            messageDao.insert(userMsg);

            // Load history
            List<MsgEntity> history = messageDao.getByConversationId(conversationId);

            // Convert to AI chat history format
            List<AIService.ChatMessage> aiHistory = new ArrayList<>();
            for (MsgEntity msg : history) {
                String role = msg.isUser() ? "user" : "assistant";
                aiHistory.add(new AIService.ChatMessage(role, msg.getContent()));
            }

            aiService.chat(aiHistory, new AIService.Callback<String>() {
                @Override
                public void onSuccess(String reply) {
                    AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                        MsgEntity aiMsg = new MsgEntity(conversationId, reply, false, System.currentTimeMillis());
                        messageDao.insert(aiMsg);
                        callback.onSuccess(reply);
                    });
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        });
    }

    public List<MsgEntity> getHistory(long conversationId) {
        return messageDao.getByConversationId(conversationId);
    }

    public void deleteConversation(long conversationId) {
        messageDao.deleteByConversationId(conversationId);
    }

    public interface Callback {
        void onSuccess(String reply);
        void onError(String error);
    }
}
