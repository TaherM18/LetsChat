package com.example.letschat.view.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerMessageAdapter;
import com.example.letschat.databinding.ActivityChatBinding;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.MessageModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

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

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private UserModel otherUser;
    private ChatroomModel chatroomModel;
    private String chatroomId;
    private RecyclerMessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());

        // Set Custom Toolbar
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setTitle(otherUser.getUserName());
        getSupportActionBar().setHomeButtonEnabled(true);

        // TODO: setProfileImage in toolbar
        // Glide.with(this).load(profileImage).into();

        getOrCreateChatroomModel();
        setupMessageRecyclerView();

        // Register the RecyclerView for the context menu
        registerForContextMenu(binding.recyclerView);

        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.edtMessage.getText().toString().length() > 0) {
                    binding.imgBtnMicSend.setImageResource(R.drawable.send_24);
                } else {
                    binding.imgBtnMicSend.setImageResource(R.drawable.mic_24);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.imgBtnMicSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.edtMessage.getText().toString().trim();
                if (message.isEmpty()) {
                    return;
                }
                sendMessageToUser(message);
            }
        });
    }

    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    chatroomModel = task.getResult().toObject(ChatroomModel.class);

                    if (chatroomModel == null) {
                        // initial chat
                        chatroomModel = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                                Timestamp.now(),
                                ""
                        );
                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                    }
                }
            }
        });
    }

    private void setupMessageRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class).build();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        messageAdapter = new RecyclerMessageAdapter(options, getApplicationContext(), chatroomId);

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

    private void sendMessageToUser(String message) {
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);

        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
        MessageModel messageModel = new MessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());
        messageModel.setReaction(-1);

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(messageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            binding.edtMessage.setText("");
                            sendNotification(message);

                            String newDocumentId = task.getResult().getId();
                            FirebaseUtil.getChatroomMessageReference(chatroomId).document(newDocumentId)
                                    .update("messageId", newDocumentId)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ChatActivity.this, "id updated", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void sendNotification(String message) {
        // current userName, message, currentUserId, otherUserToken
        FirebaseUtil.currentUserDocument().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    UserModel currentUser = task.getResult().toObject(UserModel.class);
                    try {
                        JSONObject jsonObject = new JSONObject();

                        JSONObject notificationObject = new JSONObject();
                        notificationObject.put("title", currentUser.getUserName());
                        notificationObject.put("body", message);

                        JSONObject dataObject = new JSONObject();
                        dataObject.put("userId", currentUser.getUserId());

                        jsonObject.put("notification", notificationObject);
                        jsonObject.put("data", dataObject);
                        jsonObject.put("to", otherUser.getFcmToken());

                        callAPI(jsonObject);
                    }
                    catch (Exception e) {
                        Toast.makeText(ChatActivity.this,
                                "Failed to send notification:\n"+e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });
    }

    private void callAPI(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https:fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAA8ESPmKw:APA91bEORDRgKjGUD76_18JwjcBj11mQ3De36bsAb15h5-qIB0i6HiL9Qa9fPM1Fd26bYLhGWVxzcbAY_3nVGEHEzFQOPxuOQPx9GvuAbjZH8DuhKVPW0yuDsVZu-68jowP2KuxHftAr")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(ChatActivity.this, "Failed to make API call:\n"+e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

//    @Override
//    public boolean onContextItemSelected(@NonNull MenuItem item) {
//        int selectedItemId = item.getItemId();
//        if (selectedItemId == R.id.menu_like) {
//
//        }
//        return super.onContextItemSelected(item);
//    }
}