package com.example.letschat.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerMessageAdapter;
import com.example.letschat.databinding.ActivityChatbotBinding;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.MessageModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.chat.ChatActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends BaseActivity {

    private ActivityChatbotBinding binding;
    private ChatroomModel chatroomModel;
    private String chatroomId = "", lastMessageId = "";
    private RecyclerMessageAdapter messageAdapter;
    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INITIALIZATION ==========================================================================

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chatbot);
        chatroomId = "AI_"+FirebaseUtil.currentUserId();
        client = new OkHttpClient();
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        // SETUP ===================================================================================

        getOrCreateChatroomModel();

        setupMessageRecyclerView();

        // EVENT LISTENERS =========================================================================

        binding.imgBtnMicSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.edtMessage.getText().toString().trim();
                if (message.isEmpty()) {
                    return;
                }
                MessageModel messageModel = new MessageModel(message, MessageModel.MessageType.TEXT);
                sendMessageToUser(messageModel);
                callAPI(message);
            }
        });
    }

    // FUNCTIONS ===================================================================================

    private void getOrCreateChatroomModel() {
        FirebaseUtil.getRoboChatroomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    chatroomModel = task.getResult().toObject(ChatroomModel.class);

                    if (chatroomModel == null) {
                        // initial chat
                        chatroomModel = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(FirebaseUtil.currentUserId(), "AI"),
                                Timestamp.now(),
                                ""
                        );
                        FirebaseUtil.getRoboChatroomReference(chatroomId).set(chatroomModel);
                    }

                    FirebaseUtil.currentUserDocument().update("chatroomId", chatroomId);
                }
            }
        });
    }

    private void setupMessageRecyclerView() {
        Query query = FirebaseUtil.getRoboChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class).build();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        messageAdapter = new RecyclerMessageAdapter(options, ChatbotActivity.this, chatroomId);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(messageAdapter);
        messageAdapter.startListening();

        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                binding.recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageToUser(MessageModel messageModel) {
        chatroomModel.setLastMessageTimestamp(messageModel.getTimestamp());
        chatroomModel.setLastMessageSenderId(messageModel.getSenderId());
        chatroomModel.setLastMessage(messageModel.getMessage());
        messageModel.setRead(true);

        FirebaseUtil.getRoboChatroomReference(chatroomId).set(chatroomModel);

        FirebaseUtil.getRoboChatroomMessageReference(chatroomId).add(messageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            binding.edtMessage.setText("");

                            lastMessageId = task.getResult().getId();
                            FirebaseUtil.getRoboChatroomMessageReference(chatroomId).document(lastMessageId)
                                    .update("messageId", lastMessageId);
                            if ( !messageModel.getSenderId().equals("AI") ) {
                                lastMessageId = "";
                            }
                        }
                    }
                });
    }

    private void callAPI(String question) {
        MessageModel messageModel = new MessageModel("Loading...", MessageModel.MessageType.LOADING);
        messageModel.setSenderId("AI");
        sendMessageToUser(messageModel);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo-instruct");
            jsonBody.put("prompt", question);
            jsonBody.put("max_tokens", 4000);
            jsonBody.put("temperature", 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String message = "Failed to get response.\n" + e.getMessage();
                MessageModel messageModel = new MessageModel(message, MessageModel.MessageType.TEXT);
                messageModel.setSenderId("AI");
                sendMessageToUser(messageModel);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text").trim();

                        FirebaseUtil.getRoboChatroomMessageReference(chatroomId).document(lastMessageId)
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        MessageModel model = documentSnapshot.toObject(MessageModel.class);
                                        model.setMessage(result);
                                        model.setMessageType(MessageModel.MessageType.TEXT);
                                        model.setTimestamp(Timestamp.now());

                                        FirebaseUtil.getRoboChatroomMessageReference(chatroomId)
                                                .document(lastMessageId)
                                                .set(model);
                                    }
                                });

                    }
                    catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String message = "Failed to load response as OpenAI token limit of 4000 exceeded.";
                    MessageModel messageModel = new MessageModel(message, MessageModel.MessageType.TEXT);
                    messageModel.setSenderId("AI");
                    sendMessageToUser(messageModel);
                }
            }

        });
    }
}