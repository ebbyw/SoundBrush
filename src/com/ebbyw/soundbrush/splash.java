package com.ebbyw.soundbrush;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class splash extends Activity implements View.OnClickListener
{
	static SharedPreferences thePrefs;
	static SharedPreferences.Editor prefEditor;
	
	//Initial Preference values
  	int brushSize = 12;
  	int alphanum = 255;
  	int colornum = 0xFFFFAAAA; //That's red btw
  	int timemultiplier = 1;
  	int scale = 0;
	
	MediaPlayer mp;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_layout);
		thePrefs = getPreferences(MODE_PRIVATE);
		prefEditor = thePrefs.edit(); //Sets up the Preferences for editing
		splash.prefEditor.putInt("BRUSH_TYPE", 0); // 0 - normal , 1- blur, 2-emboss, 3-srcatop, 4-eraser
		splash.prefEditor.putInt("BRUSH_SIZE", brushSize); // loads default Brush Size
        splash.prefEditor.putInt("ALPHA_NUM",alphanum);
        splash.prefEditor.putInt("TIME_MULT", timemultiplier);
        splash.prefEditor.putInt("SCALE", 0);
        splash.prefEditor.putInt("COLOR_VAL", colornum);
        splash.prefEditor.commit();
        
        //sets up the onclick listeners for the buttons
		findViewById(R.id.start).setOnClickListener(this);
		findViewById(R.id.instruct).setOnClickListener(this);
		
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
		    case R.id.instruct:{
		    	final TextView instructView = new TextView(this);
		    	instructView.setBackgroundColor(Color.WHITE);
		    	instructView.setMovementMethod(new ScrollingMovementMethod());
		    	instructView.setText(
		    			getString(R.string.instruc_menu_nobutton)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_color)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_emboss)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_blur)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_erase)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_srcatop)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_brushsz)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_alpha)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_gallery)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_play)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_stop)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_time)+"\n"+"\n"+
		    			getString(R.string.instruc_menu_majorminor)
		    		    );
		    	AlertDialog adInstruc = new AlertDialog.Builder(this).create();
		    	adInstruc.setTitle("Menu Options");
		    	adInstruc.setView(instructView);
		    	adInstruc.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}});
		    	adInstruc.show();
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
