package com.example.letschat.utils;

import androidx.annotation.NonNull;

import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseUtil {
    public static String collectionName = "users";
    public static String usersCollection = "users";
    public static String chatroomsCollection = "chatrooms";

    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
    public static DocumentReference currentUserDocument() {
        return FirebaseFirestore.getInstance().collection(collectionName).document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection(usersCollection);
    }

    public static CollectionReference allChatroomCollectionReference() {
        return FirebaseFirestore.getInstance().collection(chatroomsCollection);
    }

    public static boolean isLoggedIn() {
        if (currentUserId() != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public static StorageReference getProfileImgStoragereference() {
        return FirebaseStorage.getInstance().getReference().child("profile_images")
                .child(currentUserId());
    }

    public static DocumentReference getChatroomReference(String chatroomId) {
        return FirebaseFirestore.getInstance().collection(chatroomsCollection).document(chatroomId);
    }

    public static String getChatroomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1+"_"+userId2;
        }
        else {
            return userId2+"_"+userId1;
        }
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId) {
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds) {
        if (userIds.get(0).equals(currentUserId())) {
            return allUserCollectionReference().document(userIds.get(1));
        }
        else {
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static String formatTimestamp(Timestamp timestamp) {
        // Convert the Timestamp to a Date
        Date date = timestamp.toDate();

        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Format the Date to a String in the desired format and return
        return sdf.format(date);
    }
}
