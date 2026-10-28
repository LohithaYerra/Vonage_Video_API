package com.example.myvideocallapp;

import android.Manifest;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 500;
    private FrameLayout publisherViewContainer;
    private FrameLayout subscriberViewContainer;
    private Publisher publisher;
    private Subscriber subscriber;
    private ArrayList<Subscriber> subscribers = new ArrayList<>();
    private Session session;
    private static final String TAG = "MainActivity";


    private Session.SessionListener sessionListener = new Session.SessionListener() { //Listener were Vonage connects the remote users.
        @Override
        public void onConnected(Session session) { //when a Vonage listens the connection then it will send to this method
            Log.d(TAG, "onConnected: Connected to session: " + session.getSessionId());

            //For creating local video publisher.
            publisher = new Publisher.Builder(MainActivity.this).build();
            Log.d(TAG, "Publisher View = " + publisher.getView());

            publisher.setPublishVideo(true); //For enabling video
            publisher.setPublishAudio(true);// For enabling the audio

            Log.d(TAG, "Camera enabled: " + publisher.getPublishVideo());

            publisher.setPublisherListener(publisherListener); //A publisher listener who are organizing the video

            Log.d(TAG, "Publisher renderer = " + publisher.getRenderer());

            publisher.getRenderer().setStyle(
                    BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL
            ); // for controlling the video appearance

            publisherViewContainer.removeAllViews();

//            publisherViewContainer.addView(publisher.getView(), new FrameLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//            ));

            publisherViewContainer.setBackgroundColor(Color.RED);

            publisherViewContainer.addView( //A video view for publishing
                    publisher.getView(),
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );

            Log.d(TAG, "Publisher children = " + publisherViewContainer.getChildCount());

            if (publisher.getView() instanceof GLSurfaceView) { //Vonage uses openGL rendering GLSurfaceView

                GLSurfaceView publisherView =
                        (GLSurfaceView) publisher.getView();

                publisherView.setZOrderMediaOverlay(true); //For controlling the layer's priority.
            }

            session.publish(publisher); //Publishing the publisher session. this sends stream to Vonage.

            Log.d(TAG, "Publishing my stream");
        }


        private PublisherKit.PublisherListener publisherListener = new PublisherKit.PublisherListener() {
            @Override
            public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
                Log.d(TAG, "onStreamCreated: Publisher Stream Created. Own stream" + stream.getStreamId());
            }

            @Override
            public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
                Log.d(TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own Stream" + stream.getStreamId());
            }

            @Override
            public void onError(PublisherKit publisherKit, OpentokError opentokError) {
                Log.e(TAG, "PublisherKit onError: " + opentokError.getMessage());
            }
        };

        @Override
        public void onDisconnected(Session session) {
            Log.d(TAG, "onDisconnected: Disconnected from session: " + session.getSessionId());
        }

        private int subscriberCount = 0;

        @Override
        public void onStreamReceived(Session session, Stream stream) {
            Log.d(TAG, "onStreamReceived: New Stream Received " + stream.getStreamId());

                subscriber = new Subscriber.Builder(MainActivity.this, stream).build();//Subscriber created
                subscribers.add(subscriber);
                subscriber.setSubscribeToVideo(true);//For setting the camera and microphone.
                subscriber.setSubscribeToAudio(true);

                Log.d(TAG, "Subscriber View = " + subscriber.getView());
                Log.d(TAG, "Subscriber renderer = " + subscriber.getRenderer());
                Log.d(TAG, "Subscriber children = " + subscriberViewContainer.getChildCount());

                subscriber.setSubscriberListener(subscriberListener);

                subscriber.getRenderer().setStyle(
                        BaseVideoRenderer.STYLE_VIDEO_SCALE,
                        BaseVideoRenderer.STYLE_VIDEO_FILL
                );

                session.subscribe(subscriber); //Connecting subscriber to remote stream.
                View view = subscriber.getView();
                if(view != null){
                    //subscriberViewContainer.removeAllViews();

                    subscriberViewContainer.addView(subscriber.getView(), new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));
                }

                if (subscriber.getView() instanceof GLSurfaceView) {
                    ((GLSurfaceView) subscriber.getView()).setZOrderMediaOverlay(false);
                }

                Log.d(TAG, "Subscribed to remote stream");
        }

        @Override
        public void onStreamDropped(Session session, Stream stream) { //
            Log.d(TAG, "onStreamDropped: Stream Dropped: " + stream.getStreamId() + " in session: " + session.getSessionId());
        }

        @Override
        public void onError(Session session, OpentokError opentokError) { //For identifying the error
            Log.e(TAG, "Session error: " + opentokError.getMessage());
        }
    };

    SubscriberKit.SubscriberListener subscriberListener = new SubscriberKit.SubscriberListener() {
        @Override
        public void onConnected(SubscriberKit subscriberKit) {
            Log.d(TAG, "onConnected: Subscriber connected. Stream: " + subscriberKit.getStream().getStreamId());
        }

        @Override
        public void onDisconnected(SubscriberKit subscriberKit) {
            Log.d(TAG, "onDisconnected: Subscriber disconnected. Stream: "+ subscriberKit.getStream().getStreamId());
        }

        @Override
        public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
            Log.e(TAG, "SubscriberKit onError: " + opentokError.getMessage());
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        publisherViewContainer = findViewById(R.id.publisher_container);
        subscriberViewContainer = findViewById(R.id.subscriber_container);

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions, grantResults, this);
        Log.d(TAG, "Camera permission: " +
                EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA));
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_CODE)
    private void requestPermissions(){
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this, perms)){
            //initialize and connect to the session
            initializeSession(VonageVideoConfig.APP_ID, VonageVideoConfig.SESSION_ID, VonageVideoConfig.TOKEN);
        }
        else {
            EasyPermissions.requestPermissions(this,"This app needs access to your camera and mic to make video calls", PERMISSIONS_REQUEST_CODE, perms);
        }
    }
    private void initializeSession(String appId, String sessionId, String token){
        Log.i(TAG, "appId: " + appId);
        Log.i(TAG, "sessionId: " + sessionId);
        Log.i(TAG, "token: " + token);

        session = new Session.Builder(this, appId, sessionId).build();
        session.setSessionListener(sessionListener);
        session.connect(token);
    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (session != null) {
//            session.onPause();
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session != null) {
            session.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (publisher != null) {
            publisher.destroy();
        }

        for(Subscriber s : subscribers){
            if (s != null) { s.destroy();}
        }

        if (session != null) {
            session.disconnect();
        }
    }

}
