package com.example.letschat.view.contact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerSearchUserAdapter;
import com.example.letschat.databinding.ActivitySearchContactBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SearchContactActivity extends AppCompatActivity {

    private ActivitySearchContactBinding binding;
    private RecyclerSearchUserAdapter searchUserAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_contact);
        setSupportActionBar(binding.materialToolbar);
        binding.edtSearch.requestFocus();

        binding.imgBtnSearch.setOnClickListener(v -> {
            String searchTerm = binding.edtSearch.getText().toString();
            if (searchTerm.isEmpty()) {
                binding.edtSearch.setError("Enter search term");
                return;
            }
            setupSearchRecyclerView(searchTerm);
        });
    }

    private void setupSearchRecyclerView(String searchTerm) {
        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("userName", searchTerm);

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        searchUserAdapter = new RecyclerSearchUserAdapter(options, getApplicationContext());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(searchUserAdapter);
        searchUserAdapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (searchUserAdapter != null) {
            searchUserAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (searchUserAdapter != null) {
            searchUserAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchUserAdapter != null) {
            searchUserAdapter.startListening();
        }
    }
}