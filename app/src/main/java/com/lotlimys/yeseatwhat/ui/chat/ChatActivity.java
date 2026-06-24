package com.lotlimys.yeseatwhat.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.entity.MsgEntity;
import com.lotlimys.yeseatwhat.data.repository.ChatRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ChatRepository chatRepository;

    private RecyclerView rvConversations;
    private RecyclerView rvMessages;
    private EditText etInput;
    private TextView tvEmptyHint;

    private ConversationAdapter conversationAdapter;
    private MessageAdapter messageAdapter;

    private List<Conversation> conversations = new ArrayList<>();
    private List<MsgEntity> currentMessages = new ArrayList<>();
    private int currentConversationIndex = -1;
    private int conversationIdCounter = 0;
    private boolean isWaiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRepository = new ChatRepository(this);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        rvConversations = findViewById(R.id.rv_conversations);
        rvMessages = findViewById(R.id.rv_messages);
        etInput = findViewById(R.id.et_input);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);

        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        conversationAdapter = new ConversationAdapter();
        rvConversations.setAdapter(conversationAdapter);

        messageAdapter = new MessageAdapter();
        rvMessages.setAdapter(messageAdapter);

        findViewById(R.id.btn_new_chat).setOnClickListener(v -> createNewConversation());
        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());

        // Sidebar expand/collapse
        View sidebar = findViewById(R.id.ll_sidebar);
        View btnExpand = findViewById(R.id.btn_expand_sidebar);
        View btnCollapse = findViewById(R.id.btn_collapse_sidebar);

        btnExpand.setOnClickListener(v -> {
            sidebar.setVisibility(View.VISIBLE);
            btnExpand.setVisibility(View.GONE);
        });

        btnCollapse.setOnClickListener(v -> {
            sidebar.setVisibility(View.GONE);
            btnExpand.setVisibility(View.VISIBLE);
        });
    }

    private void createNewConversation() {
        conversationIdCounter++;
        Conversation conv = new Conversation(conversationIdCounter, "新对话 " + conversationIdCounter);
        conversations.add(conv);
        conversationAdapter.notifyItemInserted(conversations.size() - 1);

        currentConversationIndex = conversations.size() - 1;
        currentMessages.clear();
        messageAdapter.notifyDataSetChanged();
        tvEmptyHint.setVisibility(View.VISIBLE);
        rvMessages.setVisibility(View.GONE);
        rvConversations.smoothScrollToPosition(conversations.size() - 1);
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty() || isWaiting) return;

        if (currentConversationIndex < 0) {
            createNewConversation();
        }

        tvEmptyHint.setVisibility(View.GONE);
        rvMessages.setVisibility(View.VISIBLE);

        Conversation conv = conversations.get(currentConversationIndex);
        long convId = conv.getId();

        // Add user message to UI immediately
        MsgEntity userMsg = new MsgEntity(convId, text, true, System.currentTimeMillis());
        currentMessages.add(userMsg);
        int userIndex = currentMessages.size() - 1;
        messageAdapter.notifyItemInserted(userIndex);
        rvMessages.smoothScrollToPosition(userIndex);
        etInput.setText("");

        // Add a temporary "thinking" message
        MsgEntity thinkingMsg = new MsgEntity(convId, "思考中...", false, System.currentTimeMillis());
        currentMessages.add(thinkingMsg);
        int thinkingIndex = currentMessages.size() - 1;
        messageAdapter.notifyItemInserted(thinkingIndex);
        rvMessages.smoothScrollToPosition(thinkingIndex);

        isWaiting = true;
        findViewById(R.id.btn_send).setEnabled(false);

        // Update conversation title
        if (conv.getTitle().startsWith("新对话")) {
            conv.setTitle(text.length() > 12 ? text.substring(0, 12) + "..." : text);
            conversationAdapter.notifyItemChanged(currentConversationIndex);
        }

        chatRepository.sendMessage(convId, text, new ChatRepository.Callback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    // Replace thinking message with actual reply
                    currentMessages.remove(thinkingIndex);
                    messageAdapter.notifyItemRemoved(thinkingIndex);

                    MsgEntity aiMsg = new MsgEntity(convId, reply, false, System.currentTimeMillis());
                    currentMessages.add(aiMsg);
                    messageAdapter.notifyItemInserted(currentMessages.size() - 1);
                    rvMessages.smoothScrollToPosition(currentMessages.size() - 1);

                    isWaiting = false;
                    findViewById(R.id.btn_send).setEnabled(true);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Replace thinking with error message
                    currentMessages.remove(thinkingIndex);
                    messageAdapter.notifyItemRemoved(thinkingIndex);

                    MsgEntity errMsg = new MsgEntity(convId, "错误: " + error, false, System.currentTimeMillis());
                    currentMessages.add(errMsg);
                    messageAdapter.notifyItemInserted(currentMessages.size() - 1);

                    isWaiting = false;
                    findViewById(R.id.btn_send).setEnabled(true);
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ===== Models =====

    private static class Conversation {
        private final long id;
        private String title;

        Conversation(long id, String title) {
            this.id = id;
            this.title = title;
        }

        long getId() { return id; }
        String getTitle() { return title; }
        void setTitle(String title) { this.title = title; }
    }

    // ===== Conversation Adapter =====

    private class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_conversation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Conversation conv = conversations.get(position);
            holder.tvTitle.setText(conv.getTitle());
            holder.itemView.setSelected(position == currentConversationIndex);
            holder.itemView.setOnClickListener(v -> {
                int prev = currentConversationIndex;
                currentConversationIndex = position;
                if (prev >= 0) notifyItemChanged(prev);
                notifyItemChanged(position);

                // Load history from DB
                loadConversationMessages(conv.getId());
            });
        }

        @Override
        public int getItemCount() { return conversations.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;

            ViewHolder(View view) {
                super(view);
                tvTitle = view.findViewById(R.id.tv_conversation_title);
            }
        }
    }

    private void loadConversationMessages(long convId) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            List<MsgEntity> history = chatRepository.getHistory(convId);
            runOnUiThread(() -> {
                currentMessages.clear();
                currentMessages.addAll(history);
                messageAdapter.notifyDataSetChanged();

                if (currentMessages.isEmpty()) {
                    tvEmptyHint.setVisibility(View.VISIBLE);
                    rvMessages.setVisibility(View.GONE);
                } else {
                    tvEmptyHint.setVisibility(View.GONE);
                    rvMessages.setVisibility(View.VISIBLE);
                    rvMessages.smoothScrollToPosition(currentMessages.size() - 1);
                }
            });
        });
    }

    // ===== Message Adapter =====

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MsgEntity msg = currentMessages.get(position);
            holder.tvContent.setText(msg.getContent());

            if (msg.isUser()) {
                holder.container.setGravity(android.view.Gravity.END);
                holder.tvContent.setBackgroundResource(R.drawable.bg_chat_message_user);
                holder.tvContent.setTextColor(0xFFFFFFFF);
            } else {
                holder.container.setGravity(android.view.Gravity.START);
                holder.tvContent.setBackgroundResource(R.drawable.bg_chat_message_ai);
                holder.tvContent.setTextColor(0xFF333333);
            }
        }

        @Override
        public int getItemCount() { return currentMessages.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout container;
            TextView tvContent;

            ViewHolder(View view) {
                super(view);
                container = view.findViewById(R.id.ll_message_container);
                tvContent = view.findViewById(R.id.tv_message_content);
            }
        }
    }
}
