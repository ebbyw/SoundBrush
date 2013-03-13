package com.example.soundbrush;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class splash extends Activity implements View.OnClickListener
{
	static SharedPreferences thePrefs;
	static SharedPreferences.Editor prefEditor;
	
	MediaPlayer mp;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		thePrefs = getPreferences(MODE_PRIVATE);
		prefEditor = thePrefs.edit(); //Sets up the Preferences for the app
		findViewById(R.id.start).setOnClickListener(this);
		
	  mp = new MediaPlayer();
		mp.setLooping(true);
		String fileLocation = "android.resource://" +
		getPackageName() + "/" + R.raw.soundbrush_theme_song;
		Uri uri = Uri.parse(fileLocation);
		try {
					mp.setDataSource(this, uri);
				} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		try {
		mp.prepare();
		} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		mp.start();
		
		/*ImageView theCat = new ImageView(this);
		theCat.setBackgroundResource(R.drawable.angrycat);
		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setTitle("HELLO WORLD");
		ad.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		ad.setView(theCat);
		ad.show();*/
	}
	
	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
		    case R.id.start:
		    {
		    	try
		    	{
		    		startActivity(new Intent(getBaseContext(), picmenu.class));
		    	}
		    	catch(Throwable t)
		    	{
		    		Log.d("EBBY", t.toString());
		    	}
		    	break;
		    }
			default:
				break;
		}
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		mp.stop();
	}
}
