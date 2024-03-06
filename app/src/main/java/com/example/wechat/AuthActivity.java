package com.example.wechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class AuthActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button btnAuth;
    TextView textview1,textview2,textview3;
    Button button1;
    Spinner spinner;
    String[] codes = {"+91 India","+1 America"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        btnAuth = findViewById(R.id.btnAuth);
        textview2=findViewById(R.id.textView2);
        spinner = findViewById(R.id.spinner1);
        textview3 = findViewById(R.id.textView3);
        spinner.setOnItemSelectedListener(this);

        String text="WeChat will need to verify your account. What's your number?";
        SpannableString ss = new SpannableString(text);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#03A9F4"));
        ss.setSpan(foregroundColorSpan,41,60, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textview2.setText(ss);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,codes);
        spinner.setAdapter(adapter);

        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivity(i);
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        textview3.setText(spinner.getSelectedItem().toString().split(" ")[0]);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

}