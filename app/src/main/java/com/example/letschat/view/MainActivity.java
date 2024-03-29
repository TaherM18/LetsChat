package com.example.letschat.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.letschat.R;
import com.example.letschat.adapter.RecyclerChatsAdapter;
import com.example.letschat.adapter.ViewPagerStateAdapter;
import com.example.letschat.databinding.ActivityMainBinding;
import com.example.letschat.menu.CallsFragment;
import com.example.letschat.menu.ChatsFragment;
import com.example.letschat.menu.StoriesFragment;
import com.example.letschat.model.ChatModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.contact.ContactsActivity;
import com.example.letschat.view.contact.SearchContactActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPagerStateAdapter viewPagerStateAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.materialToolbar);
        viewPagerStateAdapter = new ViewPagerStateAdapter(MainActivity.this);

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

        getFCMToken();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
         binding.viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback);
         tabLayoutMediator.detach();
    }

    private void getFCMToken() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedId = item.getItemId();
        if (selectedId == R.id.menu_camera) {
            return true;
        } else if (selectedId == R.id.menu_search) {
            startActivity(new Intent(getApplicationContext(), SearchContactActivity.class));
            return true;
        } else if (selectedId == R.id.menu_group) {
            return true;
        } else if (selectedId == R.id.menu_broadcast) {
            return true;
        } else if (selectedId == R.id.menu_web) {
            return true;
        } else if (selectedId == R.id.menu_starred_msg) {
            return true;
        } else if (selectedId == R.id.menu_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
                        break;
                    case 1:
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.photo_camera_24));
                        break;
                    case 2:
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.phone_24));
                        break;
                }
                binding.fabAction.show();
            }
        }, 400);
    }
}