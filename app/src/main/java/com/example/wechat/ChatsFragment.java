package com.example.wechat;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wechat.adapter.RecyclerChatsAdapter;
import com.example.wechat.model.ChatModel;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    private RecyclerView recyclerChatsView;
    private List<ChatModel> chatModelList = new LinkedList<ChatModel>();

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerChatsView = view.findViewById(R.id.recyclerChatsView);
        recyclerChatsView.setLayoutManager(new LinearLayoutManager(getContext()));

        for (int i = 0; i < 20; i++) {
            chatModelList.add(new ChatModel("Taher Maimoon", "Hello!", "11:00 AM", 1));
        }

        RecyclerChatsAdapter chatsAdapter = new RecyclerChatsAdapter(getContext(), chatModelList);

//        DividerItemDecoration DIDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
//        recyclerChatsView.addItemDecoration(DIDecoration);

        recyclerChatsView.setAdapter(chatsAdapter);

        return view;

    }
}