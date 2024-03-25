package com.example.letschat.view.contact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerContactsAdapter;
import com.example.letschat.databinding.ActivityContactsBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private ActivityContactsBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private List<UserModel> userModelList = new LinkedList<>();
    private RecyclerContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts);
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            AndroidUtil.setProgressBar(binding.progressBar, true);
            getContactList();
        }
    }

    private void getContactList() {
        firestore.collection(FirebaseUtil.collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                AndroidUtil.setProgressBar(binding.progressBar, false);
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();

                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);

                        if (FirebaseUtil.currentUserId().equals(userModel.getUserId())) {
                            userModel.setUserName(userModel.getUserName() + " (You)");
                        }
                        userModelList.add(userModel);
                    }

                    Toast.makeText(ContactsActivity.this, "list size: " + userModelList.size(), Toast.LENGTH_SHORT).show();

//                    if (userModelList.size() == 0) {
//                        binding.layoutInvite.setVisibility(View.VISIBLE);
//                        return;
//                    }
                    contactsAdapter = new RecyclerContactsAdapter(ContactsActivity.this, userModelList);
                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
                    binding.recyclerView.setAdapter(contactsAdapter);
                }
            }
        });
    }
}