package com.example.letschat.view.chat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerMessageAdapter;
import com.example.letschat.databinding.ActivityChatBinding;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.MessageModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FileUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.BaseActivity;
import com.example.letschat.view.SettingsActivity;
import com.example.letschat.view.UsageActivity;
import com.example.letschat.view.auth.ProfileAuthActivity;
import com.example.letschat.view.contact.SearchContactActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private UserModel otherUser;
    private ChatroomModel chatroomModel;
    private RecyclerMessageAdapter messageAdapter;
    private double latitude, longitude;
    private String chatroomId, contactName = "", contactPhoneNumber = "", contactPhotoUri = "", messageId = "",
            googleMapsUrl = "https://www.google.co.in/maps/search/";
    private FusedLocationProviderClient mFusedLocationClient;
    protected LocationManager locationManager;
    private ActivityResultLauncher<String> requestReadContactsPermissionLauncher,
            documentPickerLauncher, audioPickerLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher, cameraLauncher, wallpaperLauncher;
    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private Uri capturedImageUri;
    private static final int REQUEST_CONTACT_PERMISSION = 1, REQUEST_LOCATION_PERMISSION = 44;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INITIALIZATION ==========================================================================

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // SET CUSTOM TOOLBAR ======================================================================

        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setTitle(otherUser.getUserName());
        getSupportActionBar().setHomeButtonEnabled(true);
        // TODO: setProfileImage in toolbar

        // SETUP ===================================================================================

        getOrCreateChatroomModel();

        setMessageReadStatus();

        setupMessageRecyclerView();

        // ACTIVITY RESULT LAUNCHERS ===============================================================

        // Initialize the image picker launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                // Handle the selected image URI
                                binding.cardAttach.setVisibility(View.GONE);

                                // Handle the selected image URI
                                uploadToFirebaseStorage(data.getData(), MessageModel.MessageType.IMAGE);
                            } else {
                                Toast.makeText(ChatActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        wallpaperLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                // Handle the selected image URI
                                uploadWallpaperToFirebaseStorage(data.getData());
                            } else {
                                Toast.makeText(ChatActivity.this, "No wallpaper Selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Initialize the requestPermissionLauncher
        requestReadContactsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchContactPicker();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Initialize the contact picker launcher
        contactPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri contactUri = data.getData();
                            if (contactUri != null) {
                                retrieveContactInfo(contactUri);
                                binding.cardAttach.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        // Initialize the document picker launcher
        documentPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            // The result is a URI pointing to the selected document
                            String documentName = FileUtil.getFileName(ChatActivity.this, result);

                            // upload document to Firebase Storage
                            uploadToFirebaseStorage(result, MessageModel.MessageType.DOCUMENT);

                            binding.cardAttach.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(ChatActivity.this, "No document selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Initialize the ActivityResultLauncher for camera capture
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                // Handle the selected image URI
                                binding.cardAttach.setVisibility(View.GONE);
                                // Image captured successfully, upload it to Firebase
                                uploadToFirebaseStorage(data.getData(), MessageModel.MessageType.IMAGE);
                            } else {
                                Toast.makeText(ChatActivity.this, "No Image Captured", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Initialize the audio picker launcher
        audioPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            // The result is a URI pointing to the selected document
                            String documentName = FileUtil.getFileName(ChatActivity.this, result);

                            // upload document to Firebase Storage
                            uploadToFirebaseStorage(result, MessageModel.MessageType.AUDIO);

                            binding.cardAttach.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(ChatActivity.this, "No document selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        // EVENT LISTENERS =========================================================================


        FirebaseUtil.allUserCollectionReference().document(otherUser.getUserId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (snapshot != null && snapshot.exists()) {
                            UserModel userModel = snapshot.toObject(UserModel.class);
                            if (userModel.getChatroomId() != null && userModel.getChatroomId().equals(chatroomId)) {
                                getSupportActionBar().setSubtitle("Active");
                            } else if (userModel.isOnline()) {
                                getSupportActionBar().setSubtitle("Online");
                            } else {
                                getSupportActionBar().setSubtitle("Offline");
                            }
                        }
                    }
                });

        binding.imgBtnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cardAttach.getVisibility() == View.GONE) {
                    // card visibility is gone
                    binding.cardAttach.setVisibility(View.VISIBLE);
                } else {
                    // card visibility is visible
                    binding.cardAttach.setVisibility(View.GONE);
                }
            }
        });

        binding.civCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        binding.civLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        binding.civGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGallery();
                binding.cardAttach.setVisibility(View.GONE);
            }
        });

        binding.civContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and request contact permission if not granted
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestReadContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
                } else {
                    // Permission already granted, launch contact picker
                    launchContactPicker();
                }
            }
        });

        binding.civDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDocumentPicker();
            }
        });

        binding.civAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAudioPicker();
            }
        });

        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.edtMessage.getText().toString().length() > 0) {
                    binding.imgBtnMicSend.setImageResource(R.drawable.send_24);
                } else {
                    binding.imgBtnMicSend.setImageResource(R.drawable.mic_24);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.imgBtnMicSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.edtMessage.getText().toString().trim();
                if (message.isEmpty()) {
                    return;
                }
                sendMessageToUser(new MessageModel(message, MessageModel.MessageType.TEXT));
            }
        });
    }

    // FUNCTIONS ===================================================================================

    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    chatroomModel = task.getResult().toObject(ChatroomModel.class);

                    if (chatroomModel == null) {
                        // initial chat
                        chatroomModel = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                                Timestamp.now(),
                                ""
                        );
                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                    }

                    if (chatroomModel.getChatWallpaperUrl() != null) {
                        AndroidUtil.setLayoutBackgroundFromUri(ChatActivity.this,
                                binding.chatBackgroundLayout, Uri.parse(chatroomModel.getChatWallpaperUrl()));
                    }

                    FirebaseUtil.currentUserDocument().update("chatroomId", chatroomId);
                }
            }
        });
    }

    private void setMessageReadStatus() {
        FirebaseUtil.getChatroomMessageReference(chatroomId).whereEqualTo("senderId", otherUser.getUserId())
                .whereEqualTo("read", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Create a batch to perform batched updates
                            WriteBatch batch = firestore.batch();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the document ID
                                DocumentReference docRef = FirebaseUtil.getChatroomMessageReference(chatroomId)
                                        .document(document.getId());
                                // Update the 'isRead' field to true
                                batch.update(docRef, "read", true);
                            }
                            // Commit the batch
                            batch.commit().addOnSuccessListener(aVoid -> {
                                Log.d("Chat", "Batch update successful");

                            }).addOnFailureListener(e -> {
                                Log.w("Chat", "Error updating documents in batch", e);
                            });
                        } else {
                            Log.d("Chat", "Error getting documents:\n", task.getException());
                        }
                    }
                });
    }


    private void setupMessageRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class).build();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        messageAdapter = new RecyclerMessageAdapter(options, ChatActivity.this, chatroomId);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(messageAdapter);
        messageAdapter.startListening();

        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                binding.recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageToUser(MessageModel messageModel) {
        chatroomModel.setLastMessageTimestamp(messageModel.getTimestamp());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(messageModel.getMessage());
        if (otherUser.getChatroomId() != null && otherUser.getChatroomId().equals(chatroomId)) {
            messageModel.setRead(true);
        }

        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(messageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        AndroidUtil.setProgressBar(binding.progressBar, false);

                        if (task.isSuccessful()) {
                            binding.edtMessage.setText("");

                            messageId = task.getResult().getId();
                            FirebaseUtil.getChatroomMessageReference(chatroomId).document(messageId)
                                    .update("messageId", messageId);

                            if (messageModel.getMessageType().equals(MessageModel.MessageType.TEXT)
                                    || messageModel.getMessageType().equals(MessageModel.MessageType.CONTACT)
                                    || messageModel.getMessageType().equals(MessageModel.MessageType.LOCATION)) {
                                sendNotification(messageModel.getMessage());
                                messageId = "";
                            }
                        }
                    }
                });
    }


    private void sendNotification(String message) {
        // current userName, message, currentUserId, otherUserToken
        FirebaseUtil.currentUserDocument().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    UserModel currentUser = task.getResult().toObject(UserModel.class);
                    try {
                        JSONObject jsonObject = new JSONObject();

                        JSONObject notificationObject = new JSONObject();
                        notificationObject.put("title", currentUser.getUserName());
                        notificationObject.put("body", message);

                        JSONObject dataObject = new JSONObject();
                        dataObject.put("userId", currentUser.getUserId());

                        jsonObject.put("notification", notificationObject);
                        jsonObject.put("data", dataObject);
                        jsonObject.put("to", otherUser.getFcmToken());

                        callAPI(jsonObject);
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this,
                                        "Failed to send notification:\n" + e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });
    }

    private void callAPI(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https:fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAA8ESPmKw:APA91bEORDRgKjGUD76_18JwjcBj11mQ3De36bsAb15h5-qIB0i6HiL9Qa9fPM1Fd26bYLhGWVxzcbAY_3nVGEHEzFQOPxuOQPx9GvuAbjZH8DuhKVPW0yuDsVZu-68jowP2KuxHftAr")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(ChatActivity.this, "Failed to make API call:\n" + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        // check if permissions are given
        if (checkPermissions()) {
            // check if location is enabled
            if (isLocationEnabled()) {
                binding.cardAttach.setVisibility(View.GONE);
                AndroidUtil.setProgressBar(binding.progressBar, true);

                mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // set latitude and longitude from location object
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    //
                                    setLocationMessage();
                                } else {
                                    AndroidUtil.setProgressBar(binding.progressBar, false);
                                    Toast.makeText(ChatActivity.this, "Location is null. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AndroidUtil.setProgressBar(binding.progressBar, false);
                                Toast.makeText(ChatActivity.this, "Failed to get Location", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(ChatActivity.this, "Please turn on your location.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available, request for permissions
            requestPermissions();
        }
    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }


    // method to check if location is enabled
    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void retrieveContactInfo(Uri contactUri) {
        // Query the contact content provider to retrieve the contact's information
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                // Retrieve the contact's name
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                // Retrieve the contact's profile photo URI (if available)
                contactPhotoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI));
                // Retrieve the contact's phone number (if available)
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))},
                            null);
                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        contactPhoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneCursor.close();
                        // Handle the contact information (e.g., send it to the recipient)
                        setContactMessage();
                    } else {
                        Toast.makeText(this, "contactPhoneNumber not Found", Toast.LENGTH_SHORT).show();
                    }
                }
                cursor.close();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "IllegalArgumentException:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("ChatActivity", e.getMessage());
            }

        }
    }

    private void uploadToFirebaseStorage(Uri fileUri, MessageModel.MessageType messageType) {
        // AndroidUtil.setProgressBar(binding.progressBar, true);

        sendMessageToUser(new MessageModel("Loading...", MessageModel.MessageType.LOADING));

        String fileName = FileUtil.getFileName(ChatActivity.this, fileUri);

        // Create a storage reference to "chat_media/<chatroomId>" folder
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("chat_media")
                .child(chatroomId)
                .child(fileName);

        storageRef.putFile(fileUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        // AndroidUtil.setProgressBar(binding.progressBar, false);
                        if (task.isSuccessful()) {
                            // Image Upload Completed  Successfully
                            // AndroidUtil.showToast(getApplicationContext(), "File uploaded");
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Store the download URL in a Firestore document
                                    switch (messageType) {
                                        case DOCUMENT:
                                            setDocumentMessage(uri);
                                            break;
                                        case IMAGE:
                                            setImageMessage(uri);
                                            break;
                                        case AUDIO:
                                            setAudioMessage(uri);
                                            break;
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void uploadWallpaperToFirebaseStorage(Uri fileUri) {
        AndroidUtil.setProgressBar(binding.progressBar, true);

        // Create a storage reference to wallpaper
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("chat_media")
                .child(chatroomId)
                .child("wallpaper.png");

        storageRef.putFile(fileUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        AndroidUtil.setProgressBar(binding.progressBar, false);
                        if (task.isSuccessful()) {
                            // Image Upload Completed  Successfully
                            AndroidUtil.showToast(getApplicationContext(), "Wallpaper set");
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Store the download URL in a Firestore document
                                    AndroidUtil.setLayoutBackgroundFromUri(ChatActivity.this, binding.chatBackgroundLayout, uri);
                                    FirebaseUtil.getChatroomReference(chatroomId).update("chatWallpaperUrl", uri.toString());
                                }
                            });
                        }
                    }
                });
    }

    // SET CUSTOM MESSAGE ==========================================================================

    private void setLocationMessage() {
        String source = latitude + "," + longitude;

        sendMessageToUser(new MessageModel(googleMapsUrl+source, MessageModel.MessageType.LOCATION));
    }

    private void setContactMessage() {
        String contact = contactName + ":\n" + contactPhoneNumber;
        MessageModel messageModel = new MessageModel(contact, MessageModel.MessageType.CONTACT);

        sendMessageToUser(messageModel);
    }

    private void setImageMessage(Uri fileUri) {
        String fileName = FileUtil.getFileName(ChatActivity.this, fileUri);
        String[] splittedString = fileName.split("\\.");
        String fileExtension = splittedString[splittedString.length - 1].toLowerCase();

        FirebaseUtil.getChatroomMessageReference(chatroomId)
                .document(messageId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        MessageModel messageModel = documentSnapshot.toObject(MessageModel.class);
                        messageModel.setMessageType(MessageModel.MessageType.IMAGE);
                        messageModel.setMessage("sent an image");
                        messageModel.setFileUrl(fileUri.toString());
                        messageModel.setFileType(fileExtension);
                        messageModel.setTimestamp(Timestamp.now());

                        FirebaseUtil.getChatroomMessageReference(chatroomId)
                                .document(messageId).set(messageModel);

                        sendNotification(messageModel.getMessage());

                        chatroomModel.setLastMessageTimestamp(messageModel.getTimestamp());
                        chatroomModel.setLastMessageSenderId(messageModel.getSenderId());
                        chatroomModel.setLastMessage(messageModel.getMessage());
                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                    }
                });
    }

    private void setDocumentMessage(Uri fileUri) {
        String fileName = FileUtil.getFileName(ChatActivity.this, fileUri);
        String[] splittedString = fileName.split("\\.");
        String fileExtension = splittedString[splittedString.length - 1].toLowerCase();

        FirebaseUtil.getChatroomMessageReference(chatroomId)
                .document(messageId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        MessageModel messageModel = documentSnapshot.toObject(MessageModel.class);
                        messageModel.setMessageType(MessageModel.MessageType.DOCUMENT);
                        messageModel.setMessage(fileName);
                        messageModel.setFileUrl(fileUri.toString());
                        messageModel.setFileType(fileExtension);
                        messageModel.setTimestamp(Timestamp.now());

                        FirebaseUtil.getChatroomMessageReference(chatroomId)
                                .document(messageId).set(messageModel);

                        sendNotification(messageModel.getMessage());

                        chatroomModel.setLastMessageTimestamp(messageModel.getTimestamp());
                        chatroomModel.setLastMessageSenderId(messageModel.getSenderId());
                        chatroomModel.setLastMessage(messageModel.getMessage());
                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                    }
                });
    }

    private void setAudioMessage(Uri fileUri) {
        String fileName = FileUtil.getFileName(ChatActivity.this, fileUri);
        String[] splittedString = fileName.split("\\.");
        String fileExtension = splittedString[splittedString.length - 1].toLowerCase();

        FirebaseUtil.getChatroomMessageReference(chatroomId)
                .document(messageId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        MessageModel messageModel = documentSnapshot.toObject(MessageModel.class);
                        messageModel.setMessageType(MessageModel.MessageType.AUDIO);
                        messageModel.setMessage("sent an audio");
                        messageModel.setFileUrl(fileUri.toString());
                        messageModel.setFileType(fileExtension);
                        messageModel.setTimestamp(Timestamp.now());

                        FirebaseUtil.getChatroomMessageReference(chatroomId)
                                .document(messageId).set(messageModel);

                        sendNotification(messageModel.getMessage());

                        chatroomModel.setLastMessageTimestamp(messageModel.getTimestamp());
                        chatroomModel.setLastMessageSenderId(messageModel.getSenderId());
                        chatroomModel.setLastMessage(messageModel.getMessage());
                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                    }
                });
    }


    // LAUNCHER FUNCTIONS ==========================================================================

    private void launchGallery() {
        ImagePicker.with(this)
                .crop()
                .galleryOnly()    //User can only select image from Gallery
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        galleryLauncher.launch(intent);
                        return null;
                    }
                });
    }

    private void launchWallpaperPicker() {
        ImagePicker.with(this)
                .crop(9f, 16f)
                .galleryOnly()    //User can only select image from Gallery
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        wallpaperLauncher.launch(intent);
                        return null;
                    }
                });
    }

    private void launchContactPicker() {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this);
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactPickerLauncher.launch(intent, options);
    }

    private void launchDocumentPicker() {
        // Launch the document picker activity
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this);
        documentPickerLauncher.launch("*/*", options); // Accept all file types
    }

    private void launchCamera() {
        ImagePicker.with(this)
                .crop()
                .cameraOnly()    //User can only capture image using Camera
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        cameraLauncher.launch(intent);
                        return null;
                    }
                });
    }

    private void launchAudioPicker() {
        // Launch the audio picker activity
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this);
        audioPickerLauncher.launch("audio/*", options); // Accept audio file types
    }

    // OVERRIDES ===================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedId = item.getItemId();
        if (selectedId == R.id.menu_call) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + otherUser.getPhone()));
            startActivity(callIntent);
            return true;
        } else if (selectedId == R.id.menu_wallpaper) {
            launchWallpaperPicker();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker();
            } else {
                Toast.makeText(this, "Contacts Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUtil.currentUserDocument().update("chatroomId", null);
    }
}