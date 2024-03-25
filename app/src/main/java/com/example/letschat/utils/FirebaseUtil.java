package com.example.letschat.utils;

import androidx.annotation.NonNull;

import com.example.letschat.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public static boolean isLoggedIn() {
        if (currentUserId() != null) {
            return true;
        }
        else {
            return false;
        }
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
}
