package com.example.letschat.view.contact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerContactsAdapter;
import com.example.letschat.databinding.ActivityContactsBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private ActivityContactsBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private List<UserModel> userModelList;
    private RecyclerContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            getContactList();
        }
    }

    private void getContactList() {
        firestore.collection(FirebaseUtil.collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);

                        if (FirebaseUtil.currentUserId() != userModel.getUserId()) {
                            userModelList.add(userModel);
                        }
                    }

                    if (userModelList.size() > 0) {
                        contactsAdapter = new RecyclerContactsAdapter(ContactsActivity.this, userModelList);
                        binding.recyclerView.setAdapter(contactsAdapter);
                    }
                    else {
                        binding.layoutInvite.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}