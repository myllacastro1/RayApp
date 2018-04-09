package com.mylladecastro.ray;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Locale;


/**
 * Created by mylladecastro on 05/04/2018.
 */


public class TextToVoice implements TextToSpeech.OnInitListener{
    private static final String TAG = TextToVoice.class.getSimpleName();
    TextToSpeech tts;
    String text;

    public TextToVoice(String text) {
        this.text = text;
        Log.d(TAG, "textToVoice constructor");

    }


    private void convertText(String text) {

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.UK);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This language is not supported.");
            } else {
                convertText(this.text);
            }
        }
    }
}
