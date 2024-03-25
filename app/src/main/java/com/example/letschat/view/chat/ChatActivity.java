package com.example.letschat.view.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

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

import java.util.Arrays;

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
        setSupportActionBar(binding.materialToolbar);
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        getSupportActionBar().setTitle(otherUser.getUserName());
        getSupportActionBar().setHomeButtonEnabled(true);
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());

        // TODO: setProfileImage in toolbar
        // Glide.with(this).load(profileImage).into();

        getOrCreateChatroomModel();
        setupMessageRecyclerView();

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

        messageAdapter = new RecyclerMessageAdapter(options, getApplicationContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
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

        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
        MessageModel messageModel = new MessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(messageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            binding.edtMessage.setText("");
                        }
                    }
                });
    }
}