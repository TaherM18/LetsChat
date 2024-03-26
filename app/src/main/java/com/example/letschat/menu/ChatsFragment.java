package com.example.letschat.menu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerChatsAdapter;
import com.example.letschat.adapter.RecyclerSearchUserAdapter;
import com.example.letschat.model.ChatModel;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.util.LinkedList;
import java.util.List;
import androidx.recyclerview.widget.DividerItemDecoration;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerChatsView;
    private List<ChatModel> chatModelList = new LinkedList<ChatModel>();
    private RecyclerChatsAdapter chatsAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerChatsView = view.findViewById(R.id.recyclerChatsView);
        recyclerChatsView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupChatsRecyclerView();

        //getChatModelList();
        return view;
    }


    private void setupChatsRecyclerView() {
        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class).build();

        chatsAdapter = new RecyclerChatsAdapter(options, getContext());

        recyclerChatsView.setLayoutManager( new LinearLayoutManager(getContext()) );

        // Create a DividerItemDecoration instance
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        // Set the divider to RecyclerView
        recyclerChatsView.addItemDecoration(dividerItemDecoration);

        // Set adapter to RecyclerView
        recyclerChatsView.setAdapter(chatsAdapter);
        chatsAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chatsAdapter != null) {
            chatsAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (chatsAdapter != null) {
            chatsAdapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chatsAdapter != null) {
            chatsAdapter.notifyDataSetChanged();
        }
    }
}