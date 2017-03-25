package com.nilhcem.ledcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int HANDLER_MSG_SHOW = 1;
    private static final int HANDLER_MSG_STOP = 2;
    private static final int FRAME_DELAY_MS = 125;

    private LedControl ledControl;

    private int index;
    private final HandlerThread handlerThread = new HandlerThread("FrameThread");
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ledControl = new LedControl("SPI0.0");
            ledControl.setIntensity(0);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing LED matrix", e);
        }

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != HANDLER_MSG_SHOW) {
                    return;
                }

                try {
                    byte[] frame = Invaders.FRAMES[index];
                    for (int i = 0; i < frame.length; i++) {
                        ledControl.setRow(i, frame[i]);
                    }

                    index = (index + 1) % Invaders.FRAMES.length;
                    handler.sendEmptyMessageDelayed(HANDLER_MSG_SHOW, FRAME_DELAY_MS);
                } catch (IOException e) {
                    Log.e(TAG, "Error displaying frame", e);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.sendEmptyMessage(HANDLER_MSG_SHOW);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.sendEmptyMessage(HANDLER_MSG_STOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            handlerThread.quitSafely();

            ledControl.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing LED matrix", e);
        } finally {
            handler = null;
        }
    }
}