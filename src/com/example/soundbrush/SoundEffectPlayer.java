package com.example.soundbrush;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SoundEffectPlayer {
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundMap;
	private AudioManager mgr;
	Context context;
	
	public SoundEffectPlayer(Context context) {
		this.context = context;
        mgr = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        soundPool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
        soundMap = new HashMap<Integer, Integer>();
        Log.v("soundeffect", "Sound effect player created");
	}
	
	public int addSound(final int index, int soundID) {
		int didLoad = soundPool.load(context, soundID, 1);
		if(didLoad != 0) {
			soundMap.put(index, soundPool.load(context, soundID, 1));
		} else {
			Log.v("SOUNDLOAD", "sound did not load successfully");
			return 0;
		}
		Log.v("SOUNDLOAD", "load was successful");
		return didLoad;
	}
	
	public void playSound(final int index, float volume) {
		soundPool.play(soundMap.get(index), volume, volume, 1, 0, 1.0f);
	}
	
}
