package com.mylladecastro.ray;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Locale;


/**
 * Created by mylladecastro on 05/04/2018.
 */


@RequiresApi(api = Build.VERSION_CODES.DONUT)
public class TextToVoice{
    private static final String TAG = TextToVoice.class.getSimpleName();
    private Context activity_context;
    TextToSpeech tts;
    String text;
    boolean isSpeaking;
    boolean stopSpeaking;
    private static TextToVoice instance;
    MapsActivity mapsActivity;


    public TextToVoice(Context context, String text) {
        Log.d(TAG, "constructor!");
        this.activity_context = context;
        this.text = text;
        Log.d(TAG, "constructor, context:" + activity_context);
        initialize();
    }



    private void initialize() {
        Log.e(TAG,"TextToSpeech initialize");
        this.tts = new TextToSpeech(this.activity_context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.UK); //TODO: Check if locale is available before setting.
                    tts.setPitch(1);
                    tts.speak(text, TextToSpeech.QUEUE_ADD, null);
                    stopSpeaking = false;

                }else{
                    Log.e(TAG,"TextToSpeechInitializeError");
                }
            }
        });
    }

    public void speak(String text) {
        if (stopSpeaking == false) {
            stopSpeaking = true;
            this.tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            stopSpeaking = false;
        }
    }


}
