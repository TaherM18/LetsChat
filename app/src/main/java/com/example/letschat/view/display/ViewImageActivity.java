package com.example.letschat.view.display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.letschat.R;
import com.example.letschat.common.Common;
import com.example.letschat.databinding.ActivityViewImageBinding;
import com.example.letschat.view.profile.ProfileActivity;

public class ViewImageActivity extends AppCompatActivity {

    private ActivityViewImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_image);

        // Set Custom ActionBar
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("userName"));

        binding.zoomageView.setImageBitmap(Common.IMAGE_BITMAP);
        binding.zoomageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_image_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedOptionId = item.getItemId();
        if (selectedOptionId == R.id.menu_edit) {
            Intent intent = new Intent(ViewImageActivity.this, ProfileActivity.class);
            intent.putExtra("edit", true);
            startActivity(intent);
        }
        else if (selectedOptionId == R.id.menu_share) {

        }
        return super.onOptionsItemSelected(item);
    }
}