package com.example.cabs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
public class EarnCoins extends AppCompatActivity implements RewardedVideoAdListener {
    private RewardedVideoAd mAd;
    private TextView mtext,Coin;
    private Button btn;
    AdView mAdView;
    private static final String POINTS = "Value";
    int Value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earn_coins);
        Coin = (TextView) findViewById(R.id.coin);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mtext = (TextView) findViewById(R.id.textView);
        btn = (Button) findViewById(R.id.btn_buy);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.blink);
        btn.startAnimation(animation);
        mtext.startAnimation(animation);
        MobileAds.initialize(getApplicationContext(),"ca-app-pub-3940256099942544~3347511713");
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(POINTS, 2);
        super.onSaveInstanceState(savedInstanceState);
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Value = savedInstanceState.getInt(POINTS);
    }
    private void loadRewardedVideoAd() {
        if(!mAd.isLoaded()){
            mAd.loadAd("ca-app-pub-3940256099942544/5224354917",new AdRequest.Builder().build());
        }
    }

    public void startVideoAd(View view) {
       /* if(mAd.isLoaded()){
            mAd.show();
        }*/
        {
            if (Value <= 21) {
                //if(coinCount <30) {
                new MaterialStyledDialog.Builder(EarnCoins.this)
                        .setTitle("Not Enough Coins")
                        .setDescription("Watch the Ad To Get lucky 2 coins")
                        .setIcon(R.drawable.ic_money)
                        .withIconAnimation(true)
                        .withDialogAnimation(true)
                        .withDarkerOverlay(true)
                        .setHeaderColor(R.color.colorPrimaryDark)
                        .setPositiveText("Get some coins")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mAd.show();
                            }
                        })
                        .show();
            } else {
                Value = Value - 20;
                Coin.setText(""+Value);
            }
        }
    }
    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
         loadRewardedVideoAd();
    }
    @Override
    public void onRewarded(RewardItem rewardItem) {
        Value = Value + 2;
        Coin.setText(""+Value);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    @Override
    protected void onPause() {
        mAd.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mAd.resume(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mAd.destroy(this);
        super.onDestroy();
    }
}
