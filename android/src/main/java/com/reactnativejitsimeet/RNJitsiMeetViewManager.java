package com.reactnativejitsimeet;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ReactStylesDiffMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.StateWrapper;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.module.annotations.ReactModule;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import  com.reactnativejitsimeet.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@ReactModule(name = RNJitsiMeetViewManager.REACT_CLASS)
public class RNJitsiMeetViewManager extends SimpleViewManager<RNJitsiMeetView>  {
    public static final String REACT_CLASS = "RNJitsiMeetView";
    private static final String TAG = "RNJitsiMeetViewManager";
    private IRNJitsiMeetViewReference mJitsiMeetViewReference;
    private ReactApplicationContext mReactContext;



    public RNJitsiMeetViewManager(ReactApplicationContext reactContext, IRNJitsiMeetViewReference jitsiMeetViewReference) {
        mJitsiMeetViewReference = jitsiMeetViewReference;
        mReactContext = reactContext;
        registerForBroadcastMessages();
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }


    @Override
    public RNJitsiMeetView createViewInstance(ThemedReactContext context) {
        RNJitsiMeetView view = new RNJitsiMeetView(Objects.requireNonNull(context.getCurrentActivity()));
        mJitsiMeetViewReference.setJitsiMeetView(view);
        return view;
    }


    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put("conferenceJoined", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onConferenceJoined")))
                .put("conferenceTerminated", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onConferenceTerminated")))
                .put("participantLeft", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onParticipantLeft")))
                .put("conferenceWillJoin", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onConferenceWillJoin")))
                .build();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            Log.d(TAG, "onBroadcastReceived:event == "+event.getType());
            Log.d(TAG, "onBroadcastReceived:event == "+event.getData());



            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    WritableMap eventData = Arguments.createMap();
                    mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                            mJitsiMeetViewReference.getJitsiMeetView().getId(),
                            "conferenceJoined",
                            eventData);
                    break;
                case CONFERENCE_WILL_JOIN:
                    WritableMap eventDataWillJoin = Arguments.createMap();
                    mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                            mJitsiMeetViewReference.getJitsiMeetView().getId(),
                            "conferenceWillJoin",
                            eventDataWillJoin);
                    break;
                case CONFERENCE_TERMINATED:
                    WritableMap eventDataTerminated = Arguments.createMap();
                    mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                            mJitsiMeetViewReference.getJitsiMeetView().getId(),
                            "conferenceTerminated",
                            eventDataTerminated);
                    break;
                case PARTICIPANT_LEFT:
                    WritableMap eventDataParticipantLeft = Arguments.createMap();
                    eventDataParticipantLeft.putString("participantId", String.valueOf(event.getData().get("participantId")));
                    mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                            mJitsiMeetViewReference.getJitsiMeetView().getId(),
                            "participantLeft",
                            eventDataParticipantLeft);
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

        LocalBroadcastManager.getInstance(mReactContext).registerReceiver(broadcastReceiver, intentFilter);
    }

}