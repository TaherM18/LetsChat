package com.example.letschat.view.contact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerContactsAdapter;
import com.example.letschat.databinding.ActivityContactsBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class ContactsActivity extends BaseActivity {

    private ActivityContactsBinding binding;
    private FirebaseUser firebaseUser;
    private List<UserModel> userModelList = new LinkedList<>();
    private List<String> userPhoneNumbers = new LinkedList<>();
    private RecyclerContactsAdapter contactsAdapter;
    private static final int REQUEST_CONTACTS_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts);
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        // Check contacts permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_CONTACTS_PERMISSION);
        } else {
            // Permission already granted, proceed with loading contacts
            try {
                getUserPhoneNumbers(); // Get user's phone contacts
            }
            catch (IllegalArgumentException e) {
                Toast.makeText(this, "IllegalArgumentException:\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // FUNCTIONS ===================================================================================

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

    private void getContactList() {
        firestore.collection(FirebaseUtil.usersCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        AndroidUtil.setProgressBar(binding.progressBar, false);

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
                            if (userModelList.size() > 0) {
                                binding.layoutInvite.setVisibility(View.GONE);
                                setUpRecyclerView();
                            }
                            else {
                                binding.layoutInvite.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(ContactsActivity.this, "Failed to retrieve user contacts", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setUpRecyclerView() {
        contactsAdapter = new RecyclerContactsAdapter(ContactsActivity.this, userModelList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        binding.recyclerView.addItemDecoration(itemDecoration);
        binding.recyclerView.setAdapter(contactsAdapter);
    }

    // OVERRIDES ===================================================================================

    // Override onRequestPermissionsResult to handle the contacts permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load contacts
                getUserPhoneNumbers();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show();
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        REQUEST_CONTACTS_PERMISSION);
            }
        }
    }
}