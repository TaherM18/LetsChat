package com.example.letschat.view.contact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerSearchUserAdapter;
import com.example.letschat.databinding.ActivitySearchContactBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.BaseActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class SearchContactActivity extends BaseActivity {

    private ActivitySearchContactBinding binding;
    private RecyclerSearchUserAdapter searchUserAdapter;
    private List<UserModel> userModelList = new LinkedList<>();
    private List<String> userPhoneNumbers = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INITIALISATION ==========================================================================

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_contact);
        setSupportActionBar(binding.materialToolbar);

        firestore = FirebaseFirestore.getInstance();

        try {
            getUserPhoneNumbers(); // Get user's phone contacts
        }
        catch (IllegalArgumentException e) {
            Toast.makeText(this, "IllegalArgumentException:\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        binding.edtSearch.setFocusableInTouchMode(false);

        // EVENT LISTENERS =========================================================================

        binding.imgBtnSearch.setOnClickListener(v -> {
            String searchTerm = binding.edtSearch.getText().toString();
            if (searchTerm.isEmpty()) {
                binding.edtSearch.setError("Enter search term");
                return;
            }
            setupSearchRecyclerView(searchTerm);
        });
    }

    // FUNCTIONS ===================================================================================

    private void getContactList() {
        firestore.collection(FirebaseUtil.usersCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        AndroidUtil.setProgressBar(binding.progressBar, false);
                        binding.edtSearch.setFocusableInTouchMode(true);
                        binding.edtSearch.requestFocus();

                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                                if (userPhoneNumbers.contains(userModel.getPhone())) {
                                    if (FirebaseUtil.currentUserId().equals(userModel.getUserId())) {
                                        userModel.setUserName(userModel.getUserName() + " (You)");
                                    }
                                    userModelList.add(userModel);
                                }
                            }
                        } else {
                            Toast.makeText(SearchContactActivity.this, "Failed to retrieve user contacts",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getUserPhoneNumbers() throws IllegalArgumentException {
        AndroidUtil.setProgressBar(binding.progressBar, true);

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    userPhoneNumbers.add(phoneNumber);
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "Failed to retrieve contacts", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception in retrieving contacts:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            getContactList();
        }
    }

    private void setupSearchRecyclerView(String searchTerm) {
        List<UserModel> searchList = new LinkedList<>();

        for (UserModel model : userModelList) {
            // Case-insensitive search matching partial strings
            if (model.getUserName().toLowerCase().contains(searchTerm.toLowerCase())
                    || model.getPhone().contains(searchTerm)) {
                searchList.add(model);
            }
        }

        Toast.makeText(this, searchList.size()+" results", Toast.LENGTH_SHORT).show();

        searchUserAdapter = new RecyclerSearchUserAdapter(SearchContactActivity.this, searchList);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        binding.recyclerView.addItemDecoration(itemDecoration);

        binding.recyclerView.setAdapter(searchUserAdapter);

        binding.edtSearch.setText("");
    }

}