package com.example.letschat.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.adapter.ViewPagerStateAdapter;
import com.example.letschat.databinding.ActivityMainBinding;
import com.example.letschat.model.MessageModel;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.utils.PermissionUtil;
import com.example.letschat.view.chat.ChatActivity;
import com.example.letschat.view.contact.ContactsActivity;
import com.example.letschat.view.contact.SearchContactActivity;
import com.example.letschat.view.profile.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPagerStateAdapter viewPagerStateAdapter;
    private ActivityResultLauncher<String> galleryLauncher;
    private static final int PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INITIALIZATION ==========================================================================

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.materialToolbar);
        viewPagerStateAdapter = new ViewPagerStateAdapter(MainActivity.this);

        // Request all declared permissions
        PermissionUtil.requestAllDeclaredPermissions(this, PERMISSION_REQUEST_CODE);

        FirebaseUtil.currentUserDocument().update("online", true);

        // Setup adapter for ViewPager2
        binding.viewPager2.setAdapter(viewPagerStateAdapter);

        // Setup mediator between TabLayout and ViewPager2
        tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Chats");
                                break;
                            case 1:
                                tab.setText("Status");
                                break;
                            case 2:
                                tab.setText("Calls");
                                break;
                            default:
                                break;
                        }
                    }
                });
        tabLayoutMediator.attach();

        // Initialize the image picker launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            // TODO: Handle the selected image URI
                            // uploadToFirebaseStorage(result, MessageModel.MessageType.IMAGE);
                        } else {
                            Toast.makeText(MainActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // EVENT LISTENERS =========================================================================

        // Floating Action Button onClick Listener
        binding.fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ContactsActivity.class));
            }
        });

        // ViewPager2 onPageChange Listener
        binding.viewPager2.registerOnPageChangeCallback(onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // This method will be called when a new page is selected
                changeFabIcon(position);
            }
        });

        setFCMToken();
    }

    // FUNCTIONS ===================================================================================

    private void setFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    FirebaseUtil.currentUserDocument().update("fcmToken", token);
                }
            }
        });
    }

    private void changeFabIcon(final int index) {
        binding.fabAction.hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (index) {
                    case 0:
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.chat_24));
                        binding.fabAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(MainActivity.this, ContactsActivity.class));
                            }
                        });
                        binding.fabAction.show();
                        break;
                    case 1:
//                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.photo_library_24));
//                        binding.fabAction.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {}
//                        });
                        break;
                    case 2:
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.phone_24));
                        binding.fabAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent();
                                i.setAction(Intent.ACTION_DIAL);
                                startActivity(i);
                            }
                        });
                        binding.fabAction.show();
                        break;
                }

            }
        }, 400);
    }

    // OVERRIDES ===================================================================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<Integer> grantResultList = new LinkedList<>();

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Handle the permissions result
            for (int i = 0; i < permissions.length; i++) {
                grantResultList.add(grantResults[i]);
            }
            if (grantResultList.contains(PackageManager.PERMISSION_DENIED)) {
                // PermissionUtil.requestAllDeclaredPermissions(MainActivity.this, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedId = item.getItemId();
        if (selectedId == R.id.menu_search) {
            startActivity(new Intent(getApplicationContext(), SearchContactActivity.class));
            return true;
        } else if (selectedId == R.id.menu_usage) {
            startActivity(new Intent(getApplicationContext(), UsageActivity.class));
            return true;
        } else if (selectedId == R.id.menu_profile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            return true;
        }
        else if (selectedId == R.id.menu_chatbot) {
            startActivity(new Intent(getApplicationContext(), ChatbotActivity.class));
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUtil.currentUserDocument().update("online", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.currentUserDocument().update("online", true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.currentUserDocument().update("online", false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUtil.currentUserDocument().update("online", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback);
        tabLayoutMediator.detach();
    }
}