package com.reactnativejitsimeet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;

import org.jitsi.meet.sdk.BaseReactView;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetFragment;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;
import org.jitsi.meet.sdk.ListenerUtils;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

public class RNJitsiMeetView extends BaseReactView<JitsiMeetViewListener> implements RNOngoingConferenceTracker.OngoingConferenceListener {

    private volatile String url;


    private static Bundle mergeProps(@Nullable Bundle a, @Nullable Bundle b) {
        Bundle result = new Bundle();
        if (a == null) {
            if (b != null) {
                result.putAll(b);
            }

            return result;
        } else if (b == null) {
            result.putAll(a);
            return result;
        } else {
            result.putAll(a);
            Iterator var3 = b.keySet().iterator();

            while(var3.hasNext()) {
                String key = (String)var3.next();
                Object bValue = b.get(key);
                Object aValue = a.get(key);
                String valueType = bValue.getClass().getSimpleName();
                if (valueType.contentEquals("Boolean")) {
                    result.putBoolean(key, (Boolean)bValue);
                } else if (valueType.contentEquals("String")) {
                    result.putString(key, (String)bValue);
                } else {
                    if (!valueType.contentEquals("Bundle")) {
                        throw new RuntimeException("Unsupported type: " + valueType);
                    }

                    result.putBundle(key, mergeProps((Bundle)aValue, (Bundle)bValue));
                }
            }

            return result;
        }
    }

    public RNJitsiMeetView(@NonNull Context context) {
        super(context);
        RNOngoingConferenceTracker.getInstance().addListener(this);
    }



    public void dispose() {
        RNOngoingConferenceTracker.getInstance().removeListener(this);
        super.dispose();
    }

    public void enterPictureInPicture() {


    }

    public void join(@Nullable RNJitsiMeetConferenceOptions options) {
        this.setProps(options.asProps());
    }

    public void leave() {
        this.setProps(new Bundle());
        this.leave();
    }

    private void setProps(@NonNull Bundle newProps) {
        // Merge the default options with the newly provided ones.
        Bundle props = mergeProps(new Bundle(), newProps);

        // XXX The setProps() method is supposed to be imperative i.e.
        // a second invocation with one and the same URL is expected to join
        // the respective conference again if the first invocation was followed
        // by leaving the conference. However, React and, respectively,
        // appProperties/initialProperties are declarative expressions i.e. one
        // and the same URL will not trigger an automatic re-render in the
        // JavaScript source code. The workaround implemented bellow introduces
        // "imperativeness" in React Component props by defining a unique value
        // per setProps() invocation.
        props.putLong("timestamp", System.currentTimeMillis());

        createReactRootView("App", props);
    }

    public void onCurrentConferenceChanged(String conferenceUrl) {
        this.url = conferenceUrl;
    }

    /** @deprecated */
    @Deprecated
    protected void onExternalAPIEvent(String name, ReadableMap data) {
       // this.onExternalAPIEvent(LISTENER_METHODS, name, data);
    }

    protected void onDetachedFromWindow() {
        this.dispose();
        super.onDetachedFromWindow();
    }
}