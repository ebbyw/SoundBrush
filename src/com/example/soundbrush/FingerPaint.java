/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.soundbrush;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

public class FingerPaint extends Activity implements ColorPickerDialog.OnColorChangedListener 
{
	SharedPreferences thePrefs;
	SharedPreferences.Editor prefEditor;
	int brushSize = 12;
	int alphanum = 127;
	int colornum = 0xFFAAAAAA;
	MyView mainView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = new MyView(this);
        setContentView(mainView);
        thePrefs = getPreferences(MODE_PRIVATE);
       
        prefEditor = thePrefs.edit(); //Sets up the Preferences for the app
        prefEditor.putInt("BRUSH_SIZE", brushSize); // loads default Brush Size
        prefEditor.putInt("ALPHA_NUM",alphanum);
        
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(brushSize);
        mPaint.setAlpha(thePrefs.getInt("ALPHA_NUM", 255));

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                                       0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
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
            sBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.adam_and_god),
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
            	sb.setMax(499);
            	AlertDialog ad = new AlertDialog.Builder(this).create();
            	ad.setTitle("Brush Size");
                ad.setView(sb);  	
            	ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						prefEditor.putInt("BRUSH_SIZE", sb.getProgress() + 1);
						mPaint.setStrokeWidth(sb.getProgress());
						Toast check = Toast.makeText(getApplicationContext(), Integer.toString(thePrefs.getInt("BRUSH_SIZE", 255)), Toast.LENGTH_SHORT);
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
            	AlertDialog ad2 = new AlertDialog.Builder(this).create();
            	ad2.setTitle("Opacity");
                ad2.setView(sb2);  	
            	ad2.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						prefEditor.putInt("ALPHA_NUM", sb2.getProgress() + 1);
						mPaint.setAlpha(sb2.getProgress());
						Toast check = Toast.makeText(getApplicationContext(), Integer.toString(thePrefs.getInt("ALPHA_NUM", 255)), Toast.LENGTH_SHORT);
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
           // case SHARE_BUTTON:
            	
           // 	return true;
            
            case PIC_MENU:
            	startActivity(new Intent(getBaseContext(), picmenu.class));
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
