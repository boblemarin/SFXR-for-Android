package be.minimal.sfxr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ExpandableListView.OnGroupExpandListener;
/* V1.1 */
public class SFXR extends Activity implements OnTouchListener {
	

   private static final int SUPERSAMPLING_ID = Menu.FIRST;
   private static final int PARAMS_ID = Menu.FIRST+1;
   private static final int CREDITS_ID = Menu.FIRST+2;
	
	private SFXRData sfxr;
	private Boolean must_play;
	private Typeface tf;
	private Boolean superSampling = true;
	
	private ExpandableListView mList;
    private int expandedGroup = -1;	
	private String[] groups = { "Waveform", "Envelope", "Pitch / Slope", "Filter" };
    private String[][] children = {
            { "Wave type", "Square duty", "Duty sweep", "Repeat speed", "Phaser offset", "Phaser sweep" },
            { "Attack time", "Sustain time", "Sustain punch", "Decay time" },
            { "Start frequency", "Min frequency cutoff", "Slide", "Delta slide", "Vibrato depth", "Vibrato speed", "Change amount", "Change speed" },
            { "Low-pass filter cutoff", "Low-pass filter cutoff sweep", "Low-pass filter resonance", "High-pass filter cutoff", "High-pass filter cutoff sweep" }
    };
    
    
    
    private ViewFlipper vf;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SET UP APP ENVIRONMENT
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.next);
        
        // GET FLIPPER REFERENCE
        vf = (ViewFlipper) findViewById(R.id.flipper);
        
        // GET FONT AND LISTENER REFERENCE
        tf=Typeface.createFromAsset(getAssets(),"uni05_64.ttf");
        final OnTouchListener tl = (OnTouchListener) this;
        
        // APPLY FONT TO BUTTONS AND ADD LISTENER
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
        
        // POPULATE PARAMETERS LIST
        mList = (ExpandableListView) findViewById(R.id.paramList);
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        
        for (int i = 0; i < groups.length; i++) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put("name", groups[i] );
        
            List<Map<String, String>> childrenData = new ArrayList<Map<String, String>>();
            for (int j = 0; j < children[i].length; j++) {
                Map<String, String> curChildMap = new HashMap<String, String>();
                childrenData.add(curChildMap);
                curChildMap.put("name",children[i][j] );
            }
            childData.add(childrenData);
        }
        
        mList.setAdapter(new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { "name" },
                new int[] { android.R.id.text1 },
                childData,
                R.layout.param,
                new String[] { "name" },
                new int[] { R.id.name }
                ) );
        
        mList.setOnGroupExpandListener( new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				if ( groupPosition != expandedGroup )
				{
					if ( expandedGroup != -1 )
					{
						mList.collapseGroup( expandedGroup );
						expandedGroup = -1;
					}
					
					expandedGroup = groupPosition;
				}
				
			}
		});
        
        
        sfxr = new SFXRData( (int) System.currentTimeMillis());
        sfxr.randomize();
        sfxr.playing_sample = false;
    }
    
    public void changeParam()
    {
    	
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
            	  if ( superSampling )
            	  {
            		  for( int i = 0; i < samples.length; i++ )
                      {
                     	 samples[i] = sfxr.synthSample();
                      }
            	  }
            	  else
            	  {
            		  for( int i = 0; i < samples.length; i++ )
                      {
            			  samples[i] = sfxr.synthSampleNoSuperSampling();
                      }
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
        	if ( vf.getDisplayedChild() == 1 )
        	{
        		vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_in_left));
    	        vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_out_right));
    			vf.setDisplayedChild(0);
        	}
        	else
        	{
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
        	}
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
        	/*
        	
        	*/
        	//superSampling = !superSampling;

            return false;
        }
        else{
            return super.onKeyDown(keyCode, event);
        }

    }
    
    @Override public boolean onCreateOptionsMenu(Menu menu) {
    	
        menu.add(0, SUPERSAMPLING_ID, 0, "Supersampling");
        menu.findItem(SUPERSAMPLING_ID).setCheckable(true);
        
        menu.add(0, PARAMS_ID, 0, "Parameters");
        
        menu.add(0, CREDITS_ID, 0, "Credits");
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
    	
        menu.findItem(SUPERSAMPLING_ID).setChecked(superSampling);
        
        
        menu.findItem(SUPERSAMPLING_ID).setTitle( superSampling ? "Disable SuperSampling" : "Enbale SuperSampling" );
        
        switch( vf.getDisplayedChild() )
    	{
    		case 0:
    			menu.findItem(PARAMS_ID).setTitle("Parameters");
    			break;
    		case 1:
    			menu.findItem(PARAMS_ID).setTitle("Generators");
    			break;
    	}
        return super.onPrepareOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SUPERSAMPLING_ID:
            	superSampling = !superSampling;
            	Toast.makeText(this, superSampling ? "SuperSampling is ON" : "SuperSampling is OFF", 150).show();
            	return true;
            case PARAMS_ID:
            	switch( vf.getDisplayedChild() )
            	{
            		case 0:
            			vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_in_right));
            	        vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_out_left));
            			vf.setDisplayedChild(1);
            			break;
            		case 1:
            			vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_in_left));
            	        vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_out_right));
            			vf.setDisplayedChild(0);
            			break;
            	}
                return true;
            case CREDITS_ID:
            	new AlertDialog.Builder(this)
                .setTitle("SFXR for Android")
                .setMessage("Android version by\n\t boblemarin\n\n" +
                		"Original SFXR by\n\t Tomas Pettersson\n\n" +
                		"SFXR Java port by\n\tPer Nyblom")
                .setPositiveButton("Cool", null )
                .show();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public boolean onTouch( View view, MotionEvent event )
    {
    	final int action = event.getAction();
    	if ( action == MotionEvent.ACTION_DOWN ) // || action == MotionEvent.ACTION_POINTER_DOWN )
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