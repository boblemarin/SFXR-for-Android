package be.minimal.sfxr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
/* V1.1 */
public class SFXR extends Activity implements OnTouchListener {
	
	private SFXRData sfxr;
	private Boolean must_play;
	private Typeface tf;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.next);
        
        tf=Typeface.createFromAsset(getAssets(),"uni05_64.ttf");
        
        final OnTouchListener tl = (OnTouchListener) this;
        
        tune( R.id.btnMut, tl );
        tune( R.id.btnPlay, tl );
        tune( R.id.btnRnd, tl );
        tune( R.id.btnRndBlip, tl );
        tune( R.id.btnRndCoin, tl );
        tune( R.id.btnRndExplose, tl );
        tune( R.id.btnRndHitHurt, tl );
        tune( R.id.btnRndJump, tl );
        tune( R.id.btnRndPowerUp, tl );
        tune( R.id.btnRndShoot, tl );
        
        sfxr = new SFXRData( (int) System.currentTimeMillis());
        sfxr.randomize();
        sfxr.playing_sample = false;
    }
    
    private void tune( int id, OnTouchListener tl )
    {
    	final Button b = (Button) findViewById(id);
    	b.setTypeface(tf);
    	b.setOnTouchListener(tl);
    }
    
    public void onStart()
    {
    	super.onStart();
    	must_play = true;
        // start sound thread
    	Thread background = new Thread( new Runnable( ) 
        {
           public void run( )
           {        		
        	   AndroidAudioDevice device = new AndroidAudioDevice( );
              double samples[] = new double[2048];
   
              while( must_play )
              {
                 for( int i = 0; i < samples.length; i++ )
                 {
                	 samples[i] = sfxr.synthSample();
                 }
                 device.writeSamples( samples );
              }        	
           }
        } );
    	background.start();
    }
    
    public void onStop()
    {
    	super.onStop();
    	must_play = false;
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	must_play = false;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	//Handle the back button
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
            .setTitle("SFXR")
            .setMessage("Quit application ?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Stop the activity
                	SFXR.this.finish();
                }
            })
            .setNegativeButton("No", null)
            .show();

            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
            .setTitle("SFXR for Android")
            .setMessage("Android version by\n\t boblemarin\n\n" +
            		"Original SFXR by\n\t Tomas Pettersson\n\n" +
            		"SFXR Java port by\n\tPer Nyblom")
            .setPositiveButton("Cool", null )
            .show();

            return true;
        }
        else{
            return super.onKeyDown(keyCode, event);
        }

    }
    
    public boolean onTouch( View view, MotionEvent event )
    {
    	final int action = event.getAction();
    	if ( action == MotionEvent.ACTION_DOWN )// || action == MotionEvent.ACTION_POINTER_DOWN )
    	{
    		switch( view.getId() )
    		{
    			case R.id.btnRndCoin:
    				randomFrom(0);
    				break;
    			case R.id.btnRndShoot:
    				randomFrom(1);
    				break;
    			case R.id.btnRndExplose:
    				randomFrom(2);
    				break;
    			case R.id.btnRndPowerUp:
    				randomFrom(3);
    				break;
    			case R.id.btnRndBlip:
    				randomFrom(6);
    				break;
    			case R.id.btnRndHitHurt:
    				randomFrom(4);
    				break;
    			case R.id.btnRndJump:
    				randomFrom(5);
    				break;
    			case R.id.btnPlay:
    				replay();
    				break;
    			case R.id.btnMut:
    				mutate();
    				break;
    			case R.id.btnRnd:
    				random();
    				break;
    		}
    	}
    	
    	return false;
    }
    
    private void replay()
    {
    	sfxr.playing_sample = false;
		sfxr.resetSample(false);
    	sfxr.playing_sample = true;
    }
    
    private void mutate()
    {
    	sfxr.playing_sample = false;
    	sfxr.resetSample(false);
		sfxr.mutate();
    	sfxr.playing_sample = true;
    }
    
    private void random()
    {
    	sfxr.playing_sample = false;
    	sfxr.resetSample(false);
    	sfxr.randomize();
    	sfxr.playing_sample = true;
    }
    
    private void randomFrom( int which )
    {
    	sfxr.playing_sample = false;
    	sfxr.random(which);
    	sfxr.resetSample(false);
    	sfxr.playing_sample = true;
    }
    
    
    
    
    /*
     * 
     * ANDROID AUDIO DEVICE CLASS
     * 
     */
    
    public class AndroidAudioDevice
    {
       AudioTrack track;
       short[] buffer = new short[2048];
     
       public AndroidAudioDevice( )
       {
          int minSize =AudioTrack.getMinBufferSize( 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );        
          track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, 
                                            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
                                            minSize, AudioTrack.MODE_STREAM);
          track.play();        
       }	   
     
       public void writeSamples(double[] samples) 
       {	
          fillBuffer( samples );
          track.write( buffer, 0, samples.length );
       }
     
       private void fillBuffer( double[] samples )
       {
          if( buffer.length < samples.length )
             buffer = new short[samples.length];
     
          for( int i = 0; i < samples.length; i++ )
             buffer[i] = (short)(samples[i] * Short.MAX_VALUE);;
       }		
    }
}