package com.example.a1401061.eatnear;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class SplashPage extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 5000;
    private final Animation textIn = new AlphaAnimation(0.0f, 1.0f);
    private TextView titleText;
    private MediaPlayer loadSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_page);
        loadSound = MediaPlayer.create(this, R.raw.eating_a_baguette);
        titleText = (TextView) this.findViewById(R.id.titleText);
        textIn.setDuration(SPLASH_TIME_OUT);
        titleText.startAnimation(textIn);
        loadSound.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homePageIntent = new Intent(SplashPage.this, HomePage.class);
                startActivity(homePageIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}
