package com.princemaurya.appzesttask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.princemaurya.appzesttask.adapters.OnboardingPagerAdapter;

public class OnboardingActivity extends AppCompatActivity implements OnboardingPagerAdapter.OnButtonClickListener {
    private static final String TAG = "OnboardingActivity";
    private ViewPager2 viewPager;
    private Button skipButton;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        skipButton = findViewById(R.id.skipButton);

        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this, this);
        viewPager.setAdapter(adapter);

        // Disable user swiping
        viewPager.setUserInputEnabled(false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                Log.d(TAG, "Page changed to: " + position);
            }
        });

        skipButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(OnboardingActivity.this, homepage.class));
            } else {
                startActivity(new Intent(OnboardingActivity.this, authenication_screen.class));
            }
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onButtonClick(int position) {
        Log.d(TAG, "Button clicked at position: " + position);
        if (position < 2) {
            // Move to next screen
            Log.d(TAG, "Moving to next page: " + (position + 1));
            viewPager.setCurrentItem(position + 1, true);
        } else {
            // On last screen, go to authentication
            Log.d(TAG, "Moving to authentication screen");
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(OnboardingActivity.this, homepage.class));
            } else {
                startActivity(new Intent(OnboardingActivity.this, authenication_screen.class));
            }
            finish();
        }
    }
}