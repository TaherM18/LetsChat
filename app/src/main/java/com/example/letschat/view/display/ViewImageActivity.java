package com.example.letschat.view.display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.letschat.R;
import com.example.letschat.common.Common;
import com.example.letschat.databinding.ActivityViewImageBinding;

public class ViewImageActivity extends AppCompatActivity {

    private ActivityViewImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_image);

        // Set Custom ActionBar
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.zoomageView.setImageBitmap(Common.IMAGE_BITMAP);
        binding.zoomageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}