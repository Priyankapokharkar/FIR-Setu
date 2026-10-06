package com.example.lawayerapp.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.lawayerapp.R;
import com.example.lawayerapp.databinding.ActivitySplashBinding;
import com.example.lawayerapp.utils.SharedPrefsUtil;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Play Lottie only once, then navigate
        binding.lottieView.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
            @Override public void onAnimationStart(android.animation.Animator a) {}
            @Override public void onAnimationEnd(android.animation.Animator a) { navigateNext(); }
            @Override public void onAnimationCancel(android.animation.Animator a) {}
            @Override public void onAnimationRepeat(android.animation.Animator a) {}
        });

        // Fallback: navigate after 3 s even if animation is empty / short
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, SPLASH_DURATION_MS);
    }

    private void navigateNext() {
        // Check if user already has a saved session
        String token = SharedPrefsUtil.getToken(this);
        String role  = SharedPrefsUtil.getRole(this);

        Intent intent;
        if (token != null && !token.isEmpty()) {
            // Resume session — route by role
            if ("constable".equalsIgnoreCase(role)) {
                intent = new Intent(this, ConstableDashboardActivity.class);
            } else {
                intent = new Intent(this, UserDashboardActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Slide-in animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
