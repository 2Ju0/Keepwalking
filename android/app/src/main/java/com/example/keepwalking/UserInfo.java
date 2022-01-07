package com.example.keepwalking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserInfo extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    ImageView profile, logout, withdraw;
    TextView KakaoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        KakaoName = findViewById(R.id.KakaoName);
        profile = findViewById(R.id.profile);
        logout = findViewById(R.id.logout);
        withdraw = findViewById(R.id.withdraw);

        // ***** 카카오 프로필 *****
        String url = ((GlobalApplication) getApplication()).getKakaoProfile();
        if (url != null) {
            Glide.with(this).load(url).circleCrop().into(profile);
        }

        // ***** 카카오 닉네임 *****
        KakaoName.setText(((GlobalApplication) getApplication()).getKakaoName() + " 님");

        /************* 하단바 *************/
        BottomNavigationView bottomNav = findViewById(R.id.bottom_menu4);

        // item selection part
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        final Intent intent = new Intent(UserInfo.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        return true;

                    case R.id.step:
                        final Intent intent2 = new Intent(UserInfo.this, StepCountChart.class);
                        startActivity(intent2);
                        finish();
                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        return true;

                    case R.id.calendar:
                        final Intent intent3 = new Intent(UserInfo.this, CalendarActivity.class);
                        startActivity(intent3);

                        finish();
                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        return true;
                }
                return false;
            }
        });
        /************* 하단바 *************/

        /*** logout ***/
        logout.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(UserInfo.this, LoginActivity.class);
            startActivity(intent);
        });

        /*** withdraw ***/
        logout.setOnClickListener(v -> {
            databaseReference.child("EMAIL").child(((GlobalApplication) getApplication()).getBasicEmail()).removeValue();

            Toast.makeText(getApplicationContext(), "회원탈퇴 되었습니다.", Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(UserInfo.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}