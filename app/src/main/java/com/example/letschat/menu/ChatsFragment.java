package com.example.letschat.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerChatsAdapter;
import com.example.letschat.databinding.FragmentChatsBinding;
import com.example.letschat.databinding.FragmentStoriesBinding;
import com.example.letschat.model.ChatModel;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.contact.ContactsActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;
import androidx.recyclerview.widget.DividerItemDecoration;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding binding;
    private RecyclerChatsAdapter chatsAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        // Access the root view of the binding
        View rootView = binding.getRoot();

        binding.recyclerChatsView.setLayoutManager( new LinearLayoutManager(getContext()) );

        // EVENT LISTENERS =========================================================================

        binding.btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(getContext(), ContactsActivity.class) );
            }
        });

        // SETUP ===================================================================================

        setupChatsRecyclerView();

        return rootView;
    }


    private void setupChatsRecyclerView() {
        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class).build();

        chatsAdapter = new RecyclerChatsAdapter(options, getContext());

        binding.recyclerChatsView.setLayoutManager( new LinearLayoutManager(getContext()) );

        // Create a DividerItemDecoration instance
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        // Set the divider to RecyclerView
        binding.recyclerChatsView.addItemDecoration(dividerItemDecoration);

        // Set adapter to RecyclerView
        binding.recyclerChatsView.setAdapter(chatsAdapter);
        chatsAdapter.startListening();

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if ( queryDocumentSnapshots.size() > 0 ) {
                    binding.layoutInvite.setVisibility(View.GONE);
                }
                else {
                    binding.layoutInvite.setVisibility(View.VISIBLE);
                }
            }
        });


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