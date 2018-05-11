package com.mylladecastro.ray;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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
    TextToSpeech tts;
    String text;
    Context activity_context;
    boolean isSpeaking;
    boolean stopSpeaking;

    public Boolean getSpeaking() {
        return isSpeaking;
    }



    public TextToVoice(String text, Context context) {
        Log.d(TAG, "TEXT TO SPEECH constructor!");
        this.text = text;
        this.activity_context = context;
        initialize();
    }



    private void initialize() {
        Log.e(TAG,"TextToSpeech initiaize");
        tts = new TextToSpeech(this.activity_context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    isSpeaking = true;
                    tts.setLanguage(Locale.UK); //TODO: Check if locale is available before setting.
                    tts.speak(text, TextToSpeech.QUEUE_ADD, null);
                    isSpeaking = false;
                }else{
                    Log.e(TAG,"TextToSpeechInitializeError");
                }
            }
        });
    }


}
