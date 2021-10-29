package com.reactnativejitsimeet;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import timber.log.Timber;


public class RNJitsiMeetModule extends ReactContextBaseJavaModule implements JSEventSender {

    private String TAG ="JitsiMeetModule";
    private static final String CONST_JS_CONFERENCE_JOINED_EVENT_NAME = "CONFERENCE_JOINED_EVENT_NAME";
    private static final String CONST_JS_CONFERENCE_WILL_JOIN_EVENT_NAME = "CONFERENCE_WILL_JOIN_EVENT_NAME";
    private static final String CONST_JS_CONFERENCE_TERMINATED_EVENT_NAME = "CONFERENCE_TERMINATED_EVENT_NAME";
    private static final String CONST_JS_PARTICIPANT_LEFT_EVENT_NAME = "PARTICIPANT_LEFT_EVENT_NAME";


    public static final String JS_CONFERENCE_TERMINATED_EVENT_NAME = "conference_terminated";
    public static final String JS_CONFERENCE_WILL_JOIN_EVENT_NAME = "conference_will_join";
    public static final String JS_CONFERENCE_JOINED_EVENT_NAME = "conference_joined";
    public static final String JS_PARTICIPANT_LEFT_EVENT_NAME = "participant_left";



    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };


    public RNJitsiMeetModule(ReactApplicationContext reactContext) {
        super(reactContext);
        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                // Different features flags can be set
                // .setFeatureFlag("toolbox.enabled", false)
                .setFeatureFlag("pip.enabled", false)
                .setFeatureFlag("add-people.enabled", false)
                .setWelcomePageEnabled(false)
                .build();
        org.jitsi.meet.sdk.JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        registerForBroadcastMessages();
    }




    @ReactMethod
    public void  call(String room , ReadableMap userInfo , Promise promise) {

        JitsiMeetUserInfo _userInfo = new JitsiMeetUserInfo();
        if (userInfo != null) {
            if (userInfo.hasKey("displayName")) {
                _userInfo.setDisplayName(userInfo.getString("displayName"));
            }
            if (userInfo.hasKey("email")) {
                _userInfo.setEmail(userInfo.getString("email"));
            }
            if (userInfo.hasKey("avatar")) {
                String avatarURL = userInfo.getString("avatar");
                try {
                    _userInfo.setAvatar(new URL(avatarURL));
                } catch (MalformedURLException e) {
                }
            }
        }

        // Build options object for joining the conference. The SDK will merge the default
        // one we set earlier and this one when joining.
        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom(room)
                .setUserInfo(_userInfo)
                .setFeatureFlag("lobby-mode.enabled", true)
                .setFeatureFlag("add-people.enabled", true)
                .setFeatureFlag("invite.enabled", true)
                .setFeatureFlag("replace.participant",true)
                .setFeatureFlag("pip.enabled",false)
                // Settings for audio and video
                .setAudioMuted(true)
                .setVideoMuted(true)
                .build();
        // Launch the new activity with the given options. The launch() method takes care
        // of creating the required Intent and passing the options.
        JitsiMeetActivity.launch(getReactApplicationContext(), options);

        promise.resolve(true);


    }


    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            Log.d(TAG, "onBroadcastReceived:event == "+event.getType());


            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    WritableMap eventData = Arguments.createMap();
                    sendEventToJS(getReactApplicationContext(), JS_CONFERENCE_JOINED_EVENT_NAME, eventData);
                    break;
                case CONFERENCE_WILL_JOIN:
                    WritableMap eventDataWillJoin = Arguments.createMap();
                    sendEventToJS(getReactApplicationContext(), JS_CONFERENCE_WILL_JOIN_EVENT_NAME, eventDataWillJoin);
                    break;
                case CONFERENCE_TERMINATED:
                    WritableMap eventDataTerminated = Arguments.createMap();
                    sendEventToJS(getReactApplicationContext(), JS_CONFERENCE_TERMINATED_EVENT_NAME, eventDataTerminated);
                    break;
                case PARTICIPANT_LEFT:
                    WritableMap eventDataParticipantLeft = Arguments.createMap();
                    eventDataParticipantLeft.putString("participantId", String.valueOf(event.getData().get("participantId")));
                    sendEventToJS(getReactApplicationContext(), JS_PARTICIPANT_LEFT_EVENT_NAME, eventDataParticipantLeft);
                    break;
            }
        }
    }


    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    public void sendEventToJS(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(CONST_JS_CONFERENCE_JOINED_EVENT_NAME, JS_CONFERENCE_JOINED_EVENT_NAME);
        constants.put(CONST_JS_CONFERENCE_TERMINATED_EVENT_NAME, JS_CONFERENCE_TERMINATED_EVENT_NAME);
        constants.put(CONST_JS_CONFERENCE_WILL_JOIN_EVENT_NAME, JS_CONFERENCE_WILL_JOIN_EVENT_NAME);
        constants.put(CONST_JS_PARTICIPANT_LEFT_EVENT_NAME, JS_PARTICIPANT_LEFT_EVENT_NAME);
        return constants;
    }



    @NonNull
    @Override
    public String getName() {
        return "JitsiMeetModule";
    }



}
