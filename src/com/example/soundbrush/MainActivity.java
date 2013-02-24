package com.example.soundbrush;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.widget.RelativeLayout;
import java.lang.Math;

import com.example.soundbrush.FingerPaint.MyView;

public class MainActivity extends Activity implements ColorPickerDialog.OnColorChangedListener
{
	  final int TRI = 0;
	  final int SAW = 1;
	  final int SQUARE = 2;
		final int bufferSizeSamples = 4000;
		final int sampleRate = 44100;
		int [] pictureIDs;
		int totalh;
		int totalw;
		playTask player;
    AudioGen soundMaker;
    AudioTrack audioPipeline;
    short [] audioBuffer;
    float yScalar;
    float [] halfStepScalars;
    final float startingFreq = (float) (440 * Math.pow(2.0, 1 / 12.0)); // start from Bb (REMOVE THIS LATER)
    final int [] majorScaleIndexes = {0, 2, 4, 5, 7, 9, 11, 12};
    final int [] minorScaleIndexes = {0, 2, 3, 5, 7, 8, 10, 12};
  	int brushSize = 12;
  	int alphanum = 127;
  	int colornum = 0xFFAAAAAA;
  	int timemultiplier;
  	int scale = 0;
 
  	MyView mainView;
  	
  	private SoundEffectPlayer seplayer;
  	private static final int SOUNDEFFECT_BRUSH = 1;
	
		@Override
    public void onCreate(Bundle savedInstanceState) 
		{
        super.onCreate(savedInstanceState);
        mainView = new MyView(this);
        setContentView(mainView);
        timemultiplier = 1;
        pictureIDs = new int [6];
        pictureIDs[0] = R.drawable.adam_and_god;
        pictureIDs[1] = R.drawable.caveman_picture;
        pictureIDs[2] = R.drawable.the_kiss;
        pictureIDs[3] = R.drawable.the_scream;
        pictureIDs[4] = R.drawable.mona_lisa;
        pictureIDs[5] = R.drawable.blank_canvas;
        splash.prefEditor.putInt("BRUSH_SIZE", brushSize); // loads default Brush Size
        splash.prefEditor.putInt("ALPHA_NUM",alphanum);
        splash.prefEditor.commit();
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(brushSize);
        mPaint.setAlpha(splash.thePrefs.getInt("ALPHA_NUM", 255));

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                                       0.4f, 6, 3.5f);

        seplayer = new SoundEffectPlayer(this);
        seplayer.addSound(SOUNDEFFECT_BRUSH, R.raw.brush);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioPipeline = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeSamples * 2, AudioTrack.MODE_STREAM);
        soundMaker = new AudioGen();
        soundMaker.osc1.setOscType(TRI);
        soundMaker.osc2.setOscType(SAW);
        soundMaker.osc3.setOscType(SQUARE);
        audioBuffer = new short [bufferSizeSamples];
        halfStepScalars = new float [13];
        halfStepScalars[0] = 1.0f;
        for(int i = 1; i < 13; i++) // set up multiplier array
        	halfStepScalars[i] = (float) Math.pow(2.0, i / 12.0);
    }
		
		@Override
		public void onStart()
		{
			super.onStart();
			
		}
    
    public short conv_float(float input)
    {
    	if(input > 0.0f)
    		return (short) (input * 32767.0);
    	else
    		return (short) (input * 32768.0);
    }
    
    public void go()
    {
    		for(int i = 0; i < bufferSizeSamples; i++)
    			audioBuffer[i] = 0;
    		soundMaker.prepareForPlay();
    		audioPipeline.play();
    		soundMaker.osc1.setFreq(startingFreq);
    		soundMaker.osc2.setFreq(startingFreq);
    		soundMaker.osc3.setFreq(startingFreq);
  			fillBuffer();
    }
    
    public void fillBuffer() // where the magic happens..
    {
    	int curScaleDegree = 0;
    	int curBufPos = 0;
    	for(int i = 0; i < totalh; i++)
    	{
    		int prevScaleDegree = curScaleDegree;
    		curScaleDegree = (int) (i / yScalar);
    		if(curScaleDegree != prevScaleDegree) // time for a new note
    		{
    			Log.d("KOOL", Integer.toString(curScaleDegree));
    			if (scale == 0){
    				soundMaker.osc1.setFreq(startingFreq * halfStepScalars[majorScaleIndexes[curScaleDegree]]);
    				soundMaker.osc2.setFreq(startingFreq * halfStepScalars[majorScaleIndexes[curScaleDegree]]);
    				soundMaker.osc3.setFreq(startingFreq * halfStepScalars[majorScaleIndexes[curScaleDegree]]);}
    			else{
    				soundMaker.osc1.setFreq(startingFreq * halfStepScalars[minorScaleIndexes[curScaleDegree]]);
    				soundMaker.osc2.setFreq(startingFreq * halfStepScalars[minorScaleIndexes[curScaleDegree]]);
    				soundMaker.osc3.setFreq(startingFreq * halfStepScalars[minorScaleIndexes[curScaleDegree]]);}
    		}
    		for(int j = 0; j < totalw; j++)
	    	{
	    		if(player.isCancelled())
	    			return;
	    		soundMaker.osc1.setAmp(Color.red(mainView.mBitmap.getPixel(j, i)) / 255.0f);
	    		soundMaker.osc2.setAmp(Color.green(mainView.mBitmap.getPixel(j, i)) / 255.0f);
	    		soundMaker.osc3.setAmp(Color.blue(mainView.mBitmap.getPixel(j, i)) / 255.0f);
	    		float output1 = soundMaker.osc1.doOscillate();
	    		float output2 = soundMaker.osc2.doOscillate();
	    		float output3 = soundMaker.osc3.doOscillate();
	    		float output_mix = (output1 + output2 + output3) / 3.0f;
	    		output_mix *= Color.alpha(mainView.mBitmap.getPixel(j, i)) / 255.0f;
	    		if(output_mix >= 1.0f) // limiter
	    			output_mix = 0.99f;
	    		else if(output_mix <= -1.0f)
	    			output_mix = -0.99f;
	    		// all processing is BEFORE this point
	    		audioBuffer[curBufPos] = conv_float(output_mix);
	    		audioPipeline.setStereoVolume(1.0f - (i % yScalar) / yScalar, (i % yScalar) / yScalar);
	    		audioPipeline.write(audioBuffer, curBufPos, 1);
	    		if(curBufPos == bufferSizeSamples)
	    			curBufPos = 0;
	    		j += (timemultiplier - 1);
	    	}
    	}
    }
    
    protected class playTask extends AsyncTask <Void, Void, Void>
    {
			@Override
			protected void onPreExecute()
			{
				totalh = mainView.getMeasuredHeight();
        totalw = mainView.getMeasuredWidth();
        yScalar = totalh / 8.0f;
        //Toast size = Toast.makeText(getApplicationContext(), "W: " + Integer.toString(w) + ", H: " + Integer.toString(h ), Toast.LENGTH_LONG);
        //size.show();
			}
    	
    	@Override
			protected Void doInBackground(Void... params) 
			{
				go();
				return null;
			}  
			
			@Override
			protected void onCancelled()
			{
				audioPipeline.pause();
				audioPipeline.flush();
				audioPipeline.stop();
			}
			
			@Override 
			protected void onPostExecute(Void result)
			{
				audioPipeline.pause();
				audioPipeline.flush();
				audioPipeline.stop();
			}
    }

    
    @Override
    protected void onStop()
    {
    	super.onStop();
    	if(player != null)
    		player.cancel(true);
    	if(audioPipeline != null)
    	{
    		audioPipeline.release();
    		audioPipeline = null;
    	}
    }

    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    public class MyView extends View {

        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;

        private Bitmap  mBitmap, sBitmap;
        private Canvas  mCanvas;
        private Path    mPath;
        private Paint   mBitmapPaint;

        public MyView(Context c) {
            super(c);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            int whichPic = pictureIDs[splash.thePrefs.getInt("CUR_PIC_ID", 0)];
            sBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), whichPic),
            		w, h, true);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.drawBitmap(sBitmap, new Matrix(), null);
        }

        @Override
        protected void onDraw(Canvas canvas) { 
            canvas.drawColor(0xFFAAAAAA);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawBitmap(sBitmap, new Matrix(), null);
            canvas.drawPath(mPath, mPaint);
            
            
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                		seplayer.playSound(SOUNDEFFECT_BRUSH, 0.5f);
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
    
    

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
    //New Stuff
    private static final int CHANGE_SIZE_MENU_ID = Menu.FIRST +5;
    private static final int CHANGE_ALPHA = Menu.FIRST + 6;
    //private static final int SHARE_BUTTON = Menu.FIRST + 7;
    private static final int PIC_MENU = Menu.FIRST + 7;
    private static final int PLAY_MENU_ID = Menu.FIRST + 8;
    private static final int STOP_MENU_ID = Menu.FIRST + 9;
    private static final int TIMING_MENU_ID = Menu.FIRST + 10;
    private static final int SCALE_ID = Menu.FIRST + 11;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');
        //New stuff
        menu.add(0, CHANGE_SIZE_MENU_ID,0,"Brush Size").setShortcut('5','z');
        menu.add(0,CHANGE_ALPHA,0,"Alpha Value").setShortcut('5', 'z');
        //menu.add(0,SHARE_BUTTON,0,"Share").setShortcut('5','z');
        menu.add(0,PIC_MENU,0,"Picture Menu").setShortcut('5', 'z');
        menu.add(0, PLAY_MENU_ID, 0, "PLAY").setShortcut('0', 'z');
        menu.add(0, STOP_MENU_ID, 0, "STOP").setShortcut('0', 'z');
        menu.add(0, TIMING_MENU_ID, 0, "Set Time").setShortcut('0', 'z');
        menu.add(0, SCALE_ID, 0, "Major/Minor").setShortcut('0', 'z');
        /****   Is this the mechanism to extend with filter effects?
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                              Menu.ALTERNATIVE, 0,
                              new ComponentName(this, NotesList.class),
                              null, intent, 0, null);
        *****/
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    //MENU STUFFS  ~~~~~~****~~~~~~

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                        PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                    PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
                
            //~~~~~~~~~*****~~~~~~ HERRO! New Code starts here!
                
            case CHANGE_SIZE_MENU_ID:
            	final SeekBar sb = new SeekBar(this);
            	sb.setMax(500);
            	sb.setProgress(splash.thePrefs.getInt("BRUSH_SIZE", 0));
            	AlertDialog ad = new AlertDialog.Builder(this).create();
            	ad.setTitle("Brush Size");
                ad.setView(sb);  	
            	ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						splash.prefEditor.putInt("BRUSH_SIZE", sb.getProgress() + 1);
						splash.prefEditor.commit();
						mPaint.setStrokeWidth(sb.getProgress());
						Toast check = Toast.makeText(getApplicationContext(), Integer.toString(splash.thePrefs.getInt("BRUSH_SIZE", 255)), Toast.LENGTH_SHORT);
						check.show();
						
					}
				});
            	ad.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
            	ad.show();
            	return true;
            case CHANGE_ALPHA:
            	final SeekBar sb2 = new SeekBar(this);
            	sb2.setMax(254);
            	sb2.setProgress(splash.thePrefs.getInt("ALPHA_NUM", 0));
            	AlertDialog ad2 = new AlertDialog.Builder(this).create();
            	ad2.setTitle("Opacity");
                ad2.setView(sb2);  	
            	ad2.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						splash.prefEditor.putInt("ALPHA_NUM", sb2.getProgress() + 1);
						splash.prefEditor.commit();
						mPaint.setAlpha(sb2.getProgress());
						Toast check = Toast.makeText(getApplicationContext(), Integer.toString(splash.thePrefs.getInt("ALPHA_NUM", 255)), Toast.LENGTH_SHORT);
						check.show();
						
					}
				});
            	ad2.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
            	ad2.show();
            	return true;
            //case SHARE_BUTTON:
            	
            //	return true;
            
            case PIC_MENU:
            	startActivity(new Intent(getBaseContext(), picmenu.class));
            	return true;
            	
            case PLAY_MENU_ID:
            {
            	player = (playTask) new playTask().execute();
            	return true;
            }
            
            case STOP_MENU_ID:
            {
            	if(player != null)
            	{
            		if(player.getStatus() == AsyncTask.Status.RUNNING)
            			player.cancel(true);
            		return true;
            	}
            }
            case TIMING_MENU_ID:
            {
            	AlertDialog ad99 = new AlertDialog.Builder(this).create();
            	final EditText et = new EditText(this);
            	et.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            	ad99.setTitle("Choose total play time");
            	ad99.setView(et);
            	ad99.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									timemultiplier = Integer.valueOf(et.getText().toString()).intValue(); 
								}
							});
            	ad99.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									
								}
							});
            	ad99.show();
            	return true;
            }
            case SCALE_ID:
            {
            	if(scale == 0)
            		scale = 1;
            	else if (scale == 1)
            		scale = 0;
            	return true;
            }
            default:
          		return super.onOptionsItemSelected(item);
        }
        
    }
}
