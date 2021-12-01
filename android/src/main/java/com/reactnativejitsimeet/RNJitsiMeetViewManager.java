package com.reactnativejitsimeet;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ReactStylesDiffMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.StateWrapper;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.module.annotations.ReactModule;

import org.jitsi.meet.sdk.BroadcastAction;
import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeetViewListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import  com.reactnativejitsimeet.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@ReactModule(name = RNJitsiMeetViewManager.REACT_CLASS)
public class RNJitsiMeetViewManager extends SimpleViewManager<RNJitsiMeetView>  implements LifecycleEventListener {
    public static final String REACT_CLASS = "RNJitsiMeetView";
    private static final String TAG = "RNJitsiMeetViewManager";
    private IRNJitsiMeetViewReference mJitsiMeetViewReference;
    private ReactApplicationContext mReactContext;
    Gson gson = new Gson();



    public RNJitsiMeetViewManager(ReactApplicationContext reactContext, IRNJitsiMeetViewReference jitsiMeetViewReference) {
        mJitsiMeetViewReference = jitsiMeetViewReference;
        mReactContext = reactContext;
        reactContext.addLifecycleEventListener(this);
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


    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(mReactContext.getCurrentActivity()).sendBroadcast(hangupBroadcastIntent);
    }


    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put("conferenceJoined", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onConferenceJoined")))
                .put("conferenceTerminated", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onConferenceTerminated")))
                .put("participantLeft", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onParticipantLeft")))
                .put("conferenceWillJoin", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onConferenceWillJoin")))
                .put("participantJoined", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onParticipantJoined")))
                .put("participantsInfoRetrieved", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onParticipantsInfoRetrieved")))
                .build();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                onBroadcastReceived(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void onBroadcastReceived(Intent intent) throws JSONException {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            Log.d(TAG, "onBroadcastReceived:event == "+event.getType());
            Log.d(TAG, "onBroadcastReceived:event == "+event.getData());

            if (mJitsiMeetViewReference != null && mJitsiMeetViewReference.getJitsiMeetView() != null) {
                switch (event.getType()) {
                    case CONFERENCE_JOINED:
                        WritableMap eventData = Arguments.createMap();
                        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                mJitsiMeetViewReference.getJitsiMeetView().getId(),
                                "conferenceJoined",
                                eventData);

                        // sendEventToJS(mReactContext, Constants.JS_CONFERENCE_JOINED_EVENT_NAME, eventData);

                        Intent retrieveBroadcastIntent = new Intent(Constants.RETRIEVE_INTENT);
                        retrieveBroadcastIntent.putExtra("requestId", "1212");
                        LocalBroadcastManager.getInstance(mReactContext).sendBroadcast(retrieveBroadcastIntent);
                        break;
                    case CONFERENCE_WILL_JOIN:
                        WritableMap eventDataWillJoin = Arguments.createMap();
                        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                mJitsiMeetViewReference.getJitsiMeetView().getId(),
                                "conferenceWillJoin",
                                eventDataWillJoin);
                        // WritableMap eventDataWillJoin = Arguments.createMap();
                        //  sendEventToJS(mReactContext, Constants.JS_CONFERENCE_WILL_JOIN_EVENT_NAME, eventDataWillJoin);
                        break;
                    case CONFERENCE_TERMINATED:
                        WritableMap eventDataTerminated = Arguments.createMap();
                        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                mJitsiMeetViewReference.getJitsiMeetView().getId(),
                                "conferenceTerminated",
                                eventDataTerminated);
                       // mJitsiMeetViewReference.setJitsiMeetView(null);
                        // WritableMap eventDataTerminated = Arguments.createMap();
                        // sendEventToJS(mReactContext, Constants.JS_CONFERENCE_TERMINATED_EVENT_NAME, eventDataTerminated);
                        break;
                    case PARTICIPANT_LEFT:
                        WritableMap eventDataParticipantLeft = Arguments.createMap();
                        eventDataParticipantLeft.putString("participantId", String.valueOf(event.getData().get("participantId")));
                        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                mJitsiMeetViewReference.getJitsiMeetView().getId(),
                                "participantLeft",
                                eventDataParticipantLeft);
                        // sendEventToJS(mReactContext, Constants.JS_PARTICIPANT_LEFT_EVENT_NAME, eventDataParticipantLeft);
                        break;
                    case PARTICIPANT_JOINED:
                        WritableMap eventDataParticipantJoined = Arguments.createMap();
                        eventDataParticipantJoined.putString("participantId", String.valueOf(event.getData().get("participantId")));
                        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                mJitsiMeetViewReference.getJitsiMeetView().getId(),
                                "participantJoined",
                                eventDataParticipantJoined);
                        break;
                    case PARTICIPANTS_INFO_RETRIEVED:
                        WritableMap eventDataParticipantsInfoRetrieved = Arguments.createMap();
                        WritableMap returnMap = Utils.convertJsonToMap(new JSONObject(event.getData()));
                        eventDataParticipantsInfoRetrieved.putMap("retrievedInfo", returnMap);
                        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                mJitsiMeetViewReference.getJitsiMeetView().getId(),
                                "participantsInfoRetrieved",
                                eventDataParticipantsInfoRetrieved);
                        // sendEventToJS(mReactContext, Constants.JS_RETRIEVE_PARTICIPANTS_INFO_EVENT_NAME, eventDataParticipantsInfoRetrieved);
                        break;
                }
            }


        }
    }


  /*  @Override
    public void sendEventToJS(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }*/

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

    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume: ");
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause: ");
        hangUp();
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostDestroy: ");
    }
}