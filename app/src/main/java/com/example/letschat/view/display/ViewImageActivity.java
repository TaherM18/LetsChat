package com.example.letschat.view.display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.example.letschat.R;
import com.example.letschat.common.Common;
import com.example.letschat.databinding.ActivityViewImageBinding;

public class ViewImageActivity extends AppCompatActivity {

    private ActivityViewImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_image);

        binding.zoomageView.setImageBitmap(Common.IMAGE_BITMAP);
    }
}