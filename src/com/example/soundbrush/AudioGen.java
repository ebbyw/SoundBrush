package com.example.soundbrush;

public class AudioGen 
{
	protected final int samplerate = 44100;
	protected final int wt_size = 4096;
	protected final int TRI = 0;
	protected final int SAW = 1;
	protected final int SQUARE = 2;
	protected float [] triTable; // wave tables
	protected float [] sawTable;
	protected float [] squareTable;
	Oscillator osc1, osc2, osc3; // oscillators
	
	public AudioGen()
	{
		triTable = new float [wt_size];
		sawTable = new float [wt_size];
		squareTable = new float [wt_size];
		calcWaveTables();
		osc1 = new Oscillator();
		osc2 = new Oscillator();
		osc3 = new Oscillator();
	}
	
	protected void calcWaveTables()
	{
		for(int i = 0; i < wt_size; i++)
		{
			// triangle
			if(i <= wt_size / 4.0f)
			{
				triTable[i] = i / (wt_size / 4.0f);
			}
			else if(i <= wt_size / 2.0f)
			{
				triTable[i] = 1.0f - ((i - wt_size / 4.0f)) / (wt_size / 4.0f);
			}
			else if(i <= 3.0f * wt_size / 4.0f)
			{
				triTable[i] = 0.0f - ((i - wt_size / 2.0f)) / (wt_size / 4.0f);
			}
			else
			{
				triTable[i] = -1.0f + ((i - 3.0f * wt_size / 4.0f)) / (wt_size / 4.0f);
			}
			// sawtooth
			if(i <= wt_size / 2.0f)
			{
				sawTable[i] = i / (wt_size / 2.0f);
			}
			else
			{
				sawTable[i] = -1.0f + (i - wt_size / 2.0f) / (wt_size / 2.0f);
			}
			// square
			if(i <= wt_size / 2.0f)
			{
				squareTable[i] = 1.0f;
			}
			else
			{
				squareTable[i] = -1.0f;
			}
		}
	}
	
	public void prepareForPlay()
	{
		osc1.prepareForPlay();
		osc2.prepareForPlay();
		osc3.prepareForPlay();
	}
	
	protected class Oscillator
	{
		protected float freqHz; // freq
		protected int oscType; // 0 = triangle, 1 = saw, 2 = square
		protected float wtIndex; // wavetable index
		protected float wtInc; // increment value
		protected float amplitude; // 0.0 - 1.0
		
		public Oscillator()
		{
			freqHz = 440.0f;
			wtIndex = 0.0f;
			wtInc = 0.0f;
			amplitude = 1.0f;
			oscType = TRI;
		}
		
		protected void getIncVal() // calculate wavetable increment - DO THIS AFTER ANY FREQUENCY CHANGE
		{
			wtInc = wt_size * freqHz / samplerate;
		}
		
		public void setOscType(int newType)
		{
			oscType = newType;
		}
		
		public void setFreq(float newFreq)
		{
			if(newFreq < 20.0f)
				newFreq = 20.0f;
			else if(newFreq > 20000.0f)
				newFreq = 20000.0f;
			freqHz = newFreq;
			getIncVal();
		}
		
		public void setAmp(float newAmp)
		{
			amplitude = newAmp;
		}
		
		void prepareForPlay()
		{
			wtIndex = 0.0f;
			amplitude = 1.0f;
			getIncVal();
		}
		
		protected float interpolate(float prevVal, float nextVal, float frac) // do linear interpolation, return interpolated value
		{
			return prevVal + frac * (nextVal - prevVal);
		}
		
		public float doOscillate()
		{
			float result;
			int prevIndex = (int) wtIndex;
			int nextIndex = (prevIndex == wt_size - 1) ? 0 : prevIndex + 1;
			float fracpart = wtIndex - prevIndex; // fractional part
			switch(oscType)
			{
				case TRI:
				{
					result = interpolate(triTable[prevIndex], triTable[nextIndex], fracpart); 
					break;
				}
				case SAW:
				{
					result = interpolate(sawTable[prevIndex], sawTable[nextIndex], fracpart); 
					break;
				}
				case SQUARE:
				{
					result = interpolate(squareTable[prevIndex], squareTable[nextIndex], fracpart); 
					break;
				}
				default:
				{
					return 0.0f;
				}
			}
			wtIndex += wtInc; // increment
			if(wtIndex >= wt_size) // wrap
				wtIndex -= wt_size;
			return result * amplitude;
		}
	}
}
