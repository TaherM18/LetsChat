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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseUtil {
    public static String usersCollection = "users";
    public static String chatroomsCollection = "chatrooms";
    public static String roboChatroomsCollection = "robo_chatrooms";
    public static String statusCollection = "status";

        public static String currentUserId() {
            return FirebaseAuth.getInstance().getUid();
        }
        public static DocumentReference currentUserDocument() {
            return FirebaseFirestore.getInstance().collection(usersCollection).document(currentUserId());
        }

        public static CollectionReference allUserCollectionReference() {
            return FirebaseFirestore.getInstance().collection(usersCollection);
        }

    public static CollectionReference allChatroomCollectionReference() {
        return FirebaseFirestore.getInstance().collection(chatroomsCollection);
    }

    public static CollectionReference allStatusCollectionReference() {
        return FirebaseFirestore.getInstance().collection(statusCollection);
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

    public static DocumentReference getRoboChatroomReference(String chatroomId) {
        return FirebaseFirestore.getInstance().collection("robo_chatrooms").document(chatroomId);
    }

    public static DocumentReference getStatusDocumentReference() {

        return FirebaseFirestore.getInstance().collection("status").document(currentUserId());
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

    public static CollectionReference getRoboChatroomMessageReference(String chatroomId) {
        return getRoboChatroomReference(chatroomId).collection("chats");
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

        // Get the current date and time
        Calendar currentDate = Calendar.getInstance();

        // Convert the timestamp date to Calendar
        Calendar timestampDate = Calendar.getInstance();
        timestampDate.setTime(date);

        // Check if the timestamp is from today
        if (currentDate.get(Calendar.YEAR) == timestampDate.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == timestampDate.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == timestampDate.get(Calendar.DAY_OF_MONTH)) {
            // If the timestamp is from today, format as time
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return timeFormat.format(date);
        } else {
            // If the timestamp is not from today, format as date
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a | dd/MM/yyyy", Locale.getDefault());
            return dateFormat.format(date);
        }
    }
}
