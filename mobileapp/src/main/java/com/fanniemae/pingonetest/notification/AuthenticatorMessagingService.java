package com.fanniemae.pingonetest.notification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.fanniemae.pingonetest.AuthenticationActivity;
import com.fanniemae.pingonetest.R;
import com.fanniemae.pingonetest.managers.NotificationsManager;
import com.pingidentity.pingidsdkv2.PingOne;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class AuthenticatorMessagingService extends FirebaseMessagingService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        /*
         * saves the FCM Registration Token to SharedPreferences for logging reasons
         */
        saveFcmRegistrationToken(token);
        PingOne.setDeviceToken(this, token, pingOneSDKError -> {
        });
    }

    @Override
    public void onMessageReceived(final @NonNull RemoteMessage remoteMessage) {
        PingOne.processRemoteNotification(this, remoteMessage, (notificationObject, pingOneSDKError) -> {
            if(notificationObject!=null){
                Pair<String, String> content = parseTitleAndBodyFromRemoteMessage(remoteMessage);
                if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                    Intent intent = new Intent(AuthenticatorMessagingService.this, AuthenticationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle data = new Bundle();
                    data.putParcelable("PingOneNotificationObject", notificationObject);
                    intent.putExtras(data);
                    intent.putExtra("title", content.first);
                    intent.putExtra("body", content.second);
                    startActivity(intent);
                }else{
                    NotificationsManager notificationsManager = new NotificationsManager(AuthenticatorMessagingService.this);
                    notificationsManager.buildAndSendPendingIntentNotification(notificationObject, content);
                }
            }
        });
    }

    private Pair<String, String> parseTitleAndBodyFromRemoteMessage(RemoteMessage remoteMessage){
        String title = getResources().getString(R.string.notification_default_title);
        String body = getResources().getString(R.string.notification_default_subtitle);
        if (remoteMessage.getData().containsKey("aps")){
            try {
                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(remoteMessage.getData().get("aps")));
                title = ((JSONObject)jsonObject.get("alert")).get("title").toString();
                body =  ((JSONObject)jsonObject.get("alert")).get("body").toString();
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return new Pair<>(title, body);
    }

    private void saveFcmRegistrationToken(@NonNull String token){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AuthenticatorSharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FCM_TOKEN", token);
        editor.apply();
    }
}
