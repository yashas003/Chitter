package com.blogspot.yashas003.chitter.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.blogspot.yashas003.chitter.R;
import com.blogspot.yashas003.chitter.Utils.CountryData;

public class PhNoActivity extends AppCompatActivity {
    Toolbar toolbar;
    Spinner spinner;
    EditText editText;
    CardView sendOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph_no);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        toolbar = findViewById(R.id.phNoToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        editText = findViewById(R.id.editTextPhone);

        sendOTP = findViewById(R.id.sendOtp);
        sendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                String number = editText.getText().toString().trim();
                String Number = "+" + code + number;

                if (number.isEmpty() || number.length() < 10) {
                    editText.setError("Required valid phone number :(");
                    editText.requestFocus();
                    return;
                }

                Intent intent = new Intent(PhNoActivity.this, OtpActivity.class);
                intent.putExtra("PhoneNumber", Number);
                startActivity(intent);
                finish();
            }
        });
    }
}
