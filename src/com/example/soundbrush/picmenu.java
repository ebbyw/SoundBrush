package com.example.soundbrush;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class picmenu extends Activity implements View.OnClickListener
{
	MediaPlayer mp;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picture_layout2);
		findViewById(R.id.imageButton1).setOnClickListener(this);
		findViewById(R.id.imageButton2).setOnClickListener(this);
		findViewById(R.id.imageButton3).setOnClickListener(this);
		findViewById(R.id.imageButton4).setOnClickListener(this);
		
		mp = new MediaPlayer();
		mp.setLooping(true);
		mp.setVolume(1.0f, 1.0f);
		String fileLocation = "android.resource://" +
		getPackageName() + "/" + R.raw.soundbrush_selection_screen;
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
	}
	
	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.imageButton1:
	    {
	    	try
	    	{
	    		splash.prefEditor.putInt("CUR_PIC_ID", 0);
	    		splash.prefEditor.commit();
	    		startActivity(new Intent(getApplicationContext(), MainActivity.class));
	    	}
	    	catch(Throwable t)
	    	{
	    		Log.d("EBBY", t.toString());
	    	}
	    	break;
	    }
			case R.id.imageButton2:
	    {
	    	try
	    	{
	    		splash.prefEditor.putInt("CUR_PIC_ID", 1);
	    		splash.prefEditor.commit();
	    		startActivity(new Intent(getApplicationContext(), MainActivity.class));
	    	}
	    	catch(Throwable t)
	    	{
	    		Log.d("EBBY", t.toString());
	    	}
	    	break;
	    }
			case R.id.imageButton3:
	    {
	    	try
	    	{
	    		splash.prefEditor.putInt("CUR_PIC_ID", 2);
	    		splash.prefEditor.commit();
	    		startActivity(new Intent(getApplicationContext(), MainActivity.class));
	    	}
	    	catch(Throwable t)
	    	{
	    		Log.d("EBBY", t.toString());
	    	}
	    	break;
	    }
			case R.id.imageButton4:
	    {
	    	try
	    	{
	    		splash.prefEditor.putInt("CUR_PIC_ID", 3);
	    		splash.prefEditor.commit();
	    		startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
