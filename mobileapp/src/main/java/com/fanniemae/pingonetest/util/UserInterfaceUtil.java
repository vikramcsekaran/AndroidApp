package com.fanniemae.pingonetest.util;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.fanniemae.pingonetest.R;

/*
 * This class will contain methods that any activity/fragment
 * can use.
 */
public class UserInterfaceUtil {

    public static void promptMessage(Context context, TextView textView, String text){
        displaySlider(context, textView, text);
        Handler handler = new Handler();
        handler.postDelayed((Runnable) () -> {
            hideSlider(textView);
        }, 3000);
    }

    public static void handleNetworkChange(Boolean networkOnline, Context context, TextView textView){
        if(networkOnline){
            hideSlider(textView);
        }else{
            displaySlider(context, textView, context.getString(R.string.main_screen_notification_no_network));
        }
    }

    private static void hideSlider(TextView textView){
        if(textView.getVisibility()==View.GONE){
            return;
        }
        textView.setVisibility(View.GONE);
    }

    private static void displaySlider(Context context, TextView textView, String text){
        if(textView.getVisibility()==View.VISIBLE){
            return;
        }
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        textView.startAnimation(animation);
    }

}
