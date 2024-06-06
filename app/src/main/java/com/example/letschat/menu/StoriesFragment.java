package com.example.letschat.menu;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.letschat.adapter.RecyclerStoryAdapter;
import com.example.letschat.databinding.FragmentStoriesBinding;
import com.example.letschat.model.StatusImage;
import com.example.letschat.model.StatusModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FileUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class StoriesFragment extends Fragment {

    private RecyclerStoryAdapter adapter;
    private List<StatusModel> statusModelList = new LinkedList<>();
    private List<StatusImage> statusImageList = new LinkedList<>();
    private FragmentStoriesBinding binding;
    private ActivityResultLauncher<String> galleryLauncher;

    public StoriesFragment() {
        // Required empty public constructor
    }


    public static StoriesFragment newInstance(String param1, String param2) {
        StoriesFragment fragment = new StoriesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // INITIALIZATION ==========================================================================

        // Inflate the layout for this fragment
        binding = FragmentStoriesBinding.inflate(inflater, container, false);

        // Access the root view of the binding
        View rootView = binding.getRoot();

        // Setup recycler view
        setupRecyclerView();

        // Initialize the image picker launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            // Handle the selected image URI
                            uploadToFirebaseStorage(result);
                        } else {
                            Toast.makeText(getContext(), "No Result", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // EVENT LISTENERS =============================================================================

        binding.statusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the launcher to open the gallery
                galleryLauncher.launch("image/*");
                getOrCreateStatusModel();
            }
        });

        return rootView;
    }

    // FUNCTIONS ===================================================================================

    private void getOrCreateStatusModel() {
        FirebaseUtil.getStatusDocumentReference().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        StatusModel statusModel = task.getResult().toObject(StatusModel.class);

                        FirebaseUtil.currentUserDocument().get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            UserModel userModel = task.getResult().toObject(UserModel.class);
                                            // initialize statusModel
                                            StatusModel status;
                                            if (statusModel != null) {
                                                statusImageList = statusModel.getStatusImageList();
                                                status = new StatusModel(
                                                        FirebaseUtil.currentUserId(),
                                                        userModel.getUserName(),
                                                        userModel.getProfileImage(),
                                                        statusModel.getLastUpdated(),
                                                        statusImageList
                                                );
                                            }
                                            else {
                                                status = new StatusModel(
                                                        FirebaseUtil.currentUserId(),
                                                        userModel.getUserName(),
                                                        userModel.getProfileImage(),
                                                        null,
                                                        statusImageList
                                                );
                                            }

                                            FirebaseUtil.getStatusDocumentReference().set(status);
                                        }
                                    }
                                });

                    }
                });
    }

    private void uploadToFirebaseStorage(Uri fileUri) {
        // show progress bar
        AndroidUtil.setProgressBar(binding.progressBar, true);

        String fileName = FileUtil.getFileName(getContext(), fileUri);

        // Create a storage reference to "profile_images" folder
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("status_images")
                .child(FirebaseUtil.currentUserId());

        storageRef.putFile(fileUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            // Image Upload Completed  Successfully
                            AndroidUtil.showToast(getContext(), "Image uploaded");
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Store the download URL in a Firestore document
                                    setStatusData(uri.toString());
                                }
                            });
                        }
                    }
                });
    }

    private void setStatusData(String url) {
        FirebaseUtil.getStatusDocumentReference().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        StatusModel statusModel = task.getResult().toObject(StatusModel.class);
                        statusModel.setLastStatusImage(url);
                        statusModel.setLastUpdated(Timestamp.now());

                        if (statusModel.getStatusImageList() == null) {
                            statusImageList.add(new StatusImage(url, Timestamp.now()));
                            statusModel.setStatusImageList(statusImageList);
                        } else {
                            statusModel.getStatusImageList()
                                    .add(new StatusImage(url, Timestamp.now()));
                        }
                        // Update Status
                        updateStatus(statusModel);
                    }
                });
    }

    private void updateStatus(StatusModel model) {
        FirebaseUtil.getStatusDocumentReference().set(model)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // hide progress bar
                        AndroidUtil.setProgressBar(binding.progressBar, false);
                        if (task.isSuccessful()) {
                            //
                            Toast.makeText(getContext(), "Status Added", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        // Calculate the timestamp for 24 hours ago
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursAgo = currentTime - (24 * 60 * 60 * 1000);

        // Query to fetch status stories updated within the last 24 hours
        Query query = FirebaseUtil.allStatusCollectionReference()
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .whereGreaterThan("lastUpdated", new Timestamp(new Date(twentyFourHoursAgo)));

        FirestoreRecyclerOptions<StatusModel> options = new FirestoreRecyclerOptions.Builder<StatusModel>()
                .setQuery(query, StatusModel.class).build();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        RecyclerStoryAdapter storyAdapter = new RecyclerStoryAdapter(options, getContext(), getParentFragmentManager());

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(storyAdapter);
        storyAdapter.startListening();

//        storyAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                binding.recyclerView.smoothScrollToPosition(0);
//            }
//        });
    }


    // OVERRIDES ===================================================================================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the binding when the view is destroyed
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}