package com.example.letschat.menu;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.letschat.adapter.RecyclerCallsAdapter;
import com.example.letschat.databinding.FragmentCallsBinding;
import com.example.letschat.model.ContactModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.contact.ContactsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;


public class CallsFragment extends Fragment {

    private FragmentCallsBinding binding;
    private static final int REQUEST_CODE_READ_CALL_LOG = 2;
    private RecyclerView recyclerCallsView;
    private List<String> userPhoneNumbers = new LinkedList<>();
    private List<ContactModel> contactModelList = new LinkedList<>();
    private RecyclerCallsAdapter recyclerCallsAdapter;
    private FirebaseFirestore firestore;

    public CallsFragment() {
        // Required empty public constructor
        firestore = FirebaseFirestore.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCallsBinding.inflate(inflater, container, false);

        // Access the root view of the binding
        View rootView = binding.getRoot();

        getContactList();

        return rootView;
    }

    // FUNCTIONS ===================================================================================

    // Retrieve contacts from Firestore
    private void getContactList() {
        AndroidUtil.setProgressBar(binding.progressBar, true);

        firestore.collection(FirebaseUtil.usersCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                                userPhoneNumbers.add(userModel.getPhone());
                            }

                            getCallLog();
                        } else {
                            Toast.makeText(getContext(), "Failed to retrieve user contacts", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // Retrieve and Compare call log with users list
    private void getCallLog() {
        // Check read call log permission
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

            Cursor cursor = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI,
                    null, null, null, CallLog.Calls.DATE + " DESC");

            // Iterate through call log entries
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI));
                        String contactName = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                        String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                        long callDate = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)));
                        int callType = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));


                        // Check if phone number exists in contacts list
                        if (userPhoneNumbers.contains(phoneNumber)) {
                            ContactModel contact = new ContactModel(photoUri, contactName, phoneNumber, callDate, callType);
                            contactModelList.add(contact);
                        }
                    }

                } else {
                    Toast.makeText(getContext(), "No Contacts Found", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Exception in retrieving call logs:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            finally {
                if (cursor != null) {
                    cursor.close();
                }

                AndroidUtil.setProgressBar(binding.progressBar, false);
                setupRecyclerView();
            }
        }
        else {
            // Permission is not granted, request permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_CALL_LOG},
                    REQUEST_CODE_READ_CALL_LOG);
        }

    }


    private void setupRecyclerView() {
        recyclerCallsAdapter = new RecyclerCallsAdapter(getContext(), contactModelList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        binding.recyclerView.addItemDecoration(itemDecoration);
        binding.recyclerView.setAdapter(recyclerCallsAdapter);
    }

    // OVERRIDES ===================================================================================

    // Override onRequestPermissionsResult to handle the contacts permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load call logs
                getCallLog();
            } else {
                // Permission denied, show a message
                Toast.makeText(getContext(), "Call log permission denied", Toast.LENGTH_SHORT).show();
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CALL_LOG},
                        REQUEST_CODE_READ_CALL_LOG);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getContactList();
    }
}