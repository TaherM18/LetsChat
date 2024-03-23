package com.example.letschat.utils;

import androidx.annotation.NonNull;

import com.example.letschat.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {
    public static String collectionName = "users";
    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
    public static DocumentReference currentUserDocument() {
        return FirebaseFirestore.getInstance().collection(collectionName).document(currentUserId());
    }

    public static boolean isLoggedIn() {
        if (currentUserId() != null) {
            return true;
        }
        else {
            return false;
        }
    }

//    public static UserModel getUserData() {
//        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);
//        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);
//                if (task.isSuccessful()) {
//                    UserModel userModel = task.getResult().toObject(UserModel.class);
//                    if (userModel != null) {
//                        return userModel;
//                    }
//                }
//                return null;
//            }
//        });
//    }
}
