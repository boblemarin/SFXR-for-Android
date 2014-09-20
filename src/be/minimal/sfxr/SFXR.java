package be.minimal.sfxr;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
/* V1.1 */
public class SFXR extends Activity implements OnTouchListener {
	

   private static final int SUPERSAMPLING_ID = Menu.FIRST;
   private static final int PARAMS_ID = Menu.FIRST+1;
   private static final int CREDITS_ID = Menu.FIRST+2;
	
	private SFXRData sfxr;
	private Boolean must_play;
	private Boolean rendering;
	private Typeface tf;
	private Boolean superSampling = true;
	
	private LinearLayout mList;
	private String[] groups = { "Waveform", "Envelope", "Pitch / Slope", "Filter" };
    private String[][] children = {
            { "Wave type", "Square duty", "Duty sweep", "Repeat speed", "Phaser offset", "Phaser sweep" },
            { "Attack time", "Sustain time", "Sustain punch", "Decay time" },
            { "Start frequency", "Min frequency cutoff", "Slide", "Delta slide", "Vibrato depth", "Vibrato speed", "Vibrato delay", "Change amount", "Change speed" },
            { "LP filter cutoff", "LP filter cutoff sweep", "LP filter resonance", "HP filter cutoff", "HP filter cutoff sweep" }
            //{ "Low-pass filter cutoff", "Low-pass filter cutoff sweep", "Low-pass filter resonance", "High-pass filter cutoff", "High-pass filter cutoff sweep" }
    };
    
    private ViewFlipper vf;
	private ParamLayout[] pls;
	private ProgressDialog pd;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SET UP APP ENVIRONMENT
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.list);
        
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
        tune( R.id.btnEdit, tl );
        
        // POPULATE PARAMETERS LIST
        mList = (LinearLayout) findViewById(R.id.paramList);
        mList.setPadding( 10, 0, 80, 0);
        /*
        final Shape border = new Shape() {
			
    		final Paint borderPaint = new Paint();
    		
			@Override
			public void draw(Canvas canvas, Paint paint) {
				borderPaint.setStrokeWidth(70);
				borderPaint.setColor( 0xFF555555 );
				borderPaint.setStyle( Paint.Style.STROKE );
				borderPaint.setPathEffect(  new DashPathEffect(new float[] {10, 10}, 0 ) );
				
				//canvas.drawRect(0, 0, canvas.getWidth()-30, canvas.getHeight()-30, borderPaint );
				//canvas.drawRect(getWidth()-40, 1, getWidth()-15, getHeight()-1, borderPaint );
				canvas.drawLine(getWidth()-35, 1, getWidth()-35, getHeight()-1, borderPaint );
			}
		};
    	final ShapeDrawable borderDrawable = new ShapeDrawable( border );
    	
    	mList.setBackgroundDrawable( borderDrawable );
        */
        
        TextView tv;
        //boolean odd = true;
        //params = new HashMap<View, Integer>();
        //Resources res = this.getResources();
        //LayoutInflater li = this.getLayoutInflater();
        Integer nt=0;
        pls = new ParamLayout[24];
        //ShapeDrawable sd;
        
        Button save = (Button) findViewById(R.id.btnSaveFile);
        save.setTypeface(tf);
        save.setOnTouchListener( new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				saveToFile(event);
				return false;
			}
		});
        
        OnTouchListener otl = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ParamLayout pl = (ParamLayout) v;
				float val = (event.getX()-10) / (pl.getWidth()-20);
				if ( val < 0 ) val = 0f;
				if ( val > 1 ) val = 1f;
				//val = val*2f-1f;
				
				pl.setValue(val);
				setParam( pl.getParamID() , val );
				
				return true;
			}
		};
        
        for (int i = 0; i < groups.length; i++) {
        	
        	tv = new TextView( this );
        	tv.setTypeface( tf );
        	tv.setTextColor( 0xFFfbefac );
        	tv.setGravity( Gravity.CENTER );
        	//tv.setBackgroundColor( 0xFFFF0000 );
        	//tv.setBackgroundDrawable( res.getDrawable( R.drawable.pbutton_off2 ) );
        	tv.setPadding( 10, 10, 10, 10 );
        	tv.setTextSize( 20 );
        	tv.setText( groups[i].toUpperCase() );
        	
        	mList.addView( tv );
        	
            for (int j = 0; j < children[i].length; j++) {
            	ParamLayout pl = new ParamLayout(this);
            	mList.addView(pl);
            	pl.setLabel(children[i][j].toUpperCase());
            	pl.setParamID(nt);
            	pl.setOnTouchListener( otl );
            	pls[nt]=pl;
            	++nt;
            	
            }
        }
        //vf.setDisplayedChild(1);
        
        sfxr = new SFXRData( (int) System.currentTimeMillis());
        
        //TODO: restore last parameters from bundle
        
        sfxr.randomize();
        sfxr.playing_sample = false;
        updateBars();
    }

    public void setParam( Integer paramID, float value)
    {
    	switch( paramID )
    	{
    	
    //{ "Wave type", "Square duty", "Duty sweep", "Repeat speed", "Phaser offset", "Phaser sweep" },
    	
    	case 0 : // Wave type
    		sfxr.wave_type = (int) ( (value) *4);
    		updateBars();
    		break;
    	case 1 : // Square duty
    		sfxr.p_duty = value*2f-1f;
    		break;
    	case 2 : // Duty sweep
    		sfxr.p_duty_ramp = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 3 : // Repeat speed
    		sfxr.p_repeat_speed = value*2f-1f;
    		break;
    	case 4 : // Phaser offset
    		sfxr.p_pha_offset = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 5 : // Phaser sweep
    		sfxr.p_pha_ramp = Math.pow(value*2f - 1.0f, 5.0f);
    		break;
    		
   //{ "Attack time", "Sustain time", "Sustain punch", "Decay time" },
    		
    	case 6 : // Attack time
    		sfxr.p_env_attack = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 7 : // Sustain Time
    		sfxr.p_env_sustain = Math.pow(value*2f - 1.0f, 2.0f);
    		break;
    	case 8 : // Sustain punch
    		sfxr.p_env_punch = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 9 : // Decay time
    		sfxr.p_env_decay = value*2f-1f;
    		break;
    		
   	//{ "Start frequency", "Min frequency cutoff", "Slide", "Delta slide", 
    // "Vibrato depth", "Vibrato speed", "Vibrato delay", "Change amount", "Change speed" },
    		
    	case 10 : // Start frequency
    		sfxr.p_base_freq = Math.pow(value*2f - 1.0f, 2.0f);
    		break;
    	case 11 : // Min frequency cutoff
    		sfxr.p_freq_limit = value*2f-1f;
    		break;
    	case 12 : // Slide
    		sfxr.p_freq_ramp = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 13 : // Delta slide
    		sfxr.p_freq_dramp = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 14 : // Vibrato depth
    		sfxr.p_vib_strength = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 15 : // Vibrato speed
    		sfxr.p_vib_speed = value*2f-1f;
    		break;
    	case 16 : // Vibrato delay
    		sfxr.p_vib_delay = value*2f-1f;
    		break;
    	case 17 : // Change amount
    		sfxr.p_arp_mod = value*2f-1f;
    		break;
    	case 18 : // Change speed
    		sfxr.p_arp_speed = value*2f-1f;
    		break;
    		
	//{ "LP filter cutoff", "LP filter cutoff sweep", "LP filter resonance", 
	// "HP filter cutoff", "HP filter cutoff sweep" }
    	
    	case 19 : // LP filter cutoff
    		sfxr.p_lpf_freq = 1.0f - Math.pow(value, 3.0f);
    		break;
    	case 20 : // LP filter cutoff sweep
    		sfxr.p_lpf_ramp = Math.pow(value*2f - 1.0f, 3.0f);
    		break;
    	case 21 : // LP filter resonance
    		sfxr.p_lpf_resonance = value*2f-1f;
    		break;
    	case 22 : // HP filter cutoff
    		sfxr.p_hpf_freq = Math.pow(value, 5.0f);
    		break;
    	case 23 : // HP filter cutoff sweep
    		sfxr.p_hpf_ramp = Math.pow(value*2f - 1.0f, 5.0f);
    		break;
    		
    	}
    	if ( !sfxr.playing_sample )
    	{
    		replay();
    	}
    	
    }
    

    private void updateBars()
    {
    	if ( sfxr.wave_type == 0 ) pls[0].setLabel("WAVE TYPE: SQUARE");
    	else if ( sfxr.wave_type == 1 ) pls[0].setLabel("WAVE TYPE: SAW");
    	else if ( sfxr.wave_type == 2 ) pls[0].setLabel("WAVE TYPE: SINE");
    	else if ( sfxr.wave_type == 3 ) pls[0].setLabel("WAVE TYPE: NOISE");
    	
    	pls[0].setValue( (float) sfxr.wave_type / 4f + .12f );
    	pls[0].invalidate();
    	
    	pls[1].setValue( ( sfxr.p_duty + 1f ) * .5f );
    	pls[2].setValue( Math.pow( ( sfxr.p_duty_ramp + 1f ) * .5f, 1f / 3.0f ) );
    	pls[3].setValue( ( sfxr.p_repeat_speed + 1f ) * .5f );
    	pls[4].setValue( Math.pow( ( sfxr.p_pha_offset + 1f ) * .5f, 1f / 3.0f ) );
    	pls[5].setValue( Math.pow( ( sfxr.p_pha_ramp + 1f ) * .5f, 1f / 5.0f ) );
    	
    	
    	pls[6].setValue( Math.pow( ( sfxr.p_env_attack + 1f ) * .5f, 1f / 3.0f ) );
    	pls[7].setValue( Math.pow( ( sfxr.p_env_sustain + 1f ) * .5f, 1f / 2.0f ) );
    	pls[8].setValue( Math.pow( ( sfxr.p_env_punch + 1f ) * .5f, 1f / 3.0f ) );
    	pls[9].setValue( ( sfxr.p_env_decay + 1f ) * .5f );
    	
    	pls[10].setValue( Math.pow( ( sfxr.p_base_freq + 1f ) * .5f, 1f / 2.0f ) );
    	pls[11].setValue( ( sfxr.p_freq_limit + 1f ) * .5f );
    	
    	pls[12].setValue( Math.pow( ( sfxr.p_freq_ramp + 1f ) * .5f, 1f / 3.0f ) );
    	pls[13].setValue( Math.pow( ( sfxr.p_freq_dramp + 1f ) * .5f, 1f / 3.0f ) );
    	pls[14].setValue( Math.pow( ( sfxr.p_vib_strength + 1f ) * .5f, 1f / 3.0f ) );
    	
    	pls[15].setValue( ( sfxr.p_vib_speed + 1f ) * .5f );
    	pls[16].setValue( ( sfxr.p_vib_delay + 1f ) * .5f );
    	pls[17].setValue( ( sfxr.p_arp_mod + 1f ) * .5f );
    	pls[18].setValue( ( sfxr.p_arp_speed + 1f ) * .5f );
    	
    	pls[19].setValue( 1.0f - Math.pow( sfxr.p_lpf_freq, 1f / 3.0f ) );
    	pls[20].setValue( Math.pow( ( sfxr.p_lpf_ramp + 1f ) * .5f, 1f / 3.0f ) );
    	
    	pls[21].setValue( ( sfxr.p_lpf_resonance + 1f ) * .5f );
    	pls[22].setValue( Math.pow( sfxr.p_hpf_freq, 5.0f ) );
    	pls[23].setValue( Math.pow( ( sfxr.p_hpf_ramp + 1f ) * .5f, 1f / 5.0f ) );
    	
    	mList.invalidate();
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
		rendering = false;
        // start sound thread
    	Thread background = new Thread( new Runnable( ) 
        {
           public void run( )
           {        		
        	   AndroidAudioDevice device = new AndroidAudioDevice( );
              double samples[] = new double[2048];
   
              while( must_play )
              {
            	 // if ( superSampling )
            	  //{
            	  	if ( !rendering ) {
            		  for( int i = 0; i < samples.length; i++ )
                      {
                     	 samples[i] = sfxr.synthSample();
                      }
            	  	}
//            	  }
//            	  else
//            	  {
//            		  for( int i = 0; i < samples.length; i++ )
//                      {
//            			  samples[i] = sfxr.synthSampleNoSuperSampling();
//                      }
//            	  }
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
        		((Button)findViewById(R.id.btnEdit)).setText("EDIT");
        		vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_in_left));
    	        vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_out_right));
    			vf.setDisplayedChild(0);
        	}
        	else
        	{
        		
        		//Stop the activity
            	SFXR.this.finish();
        		
        		/*
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
                */
        	}
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
        	/*
        	
        	*/
        	//superSampling = !superSampling;

        	switchScreens();
        	
            return true;
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
    			case R.id.btnEdit:
    				switchScreens();
    				break;
    		}
    	}
    	
    	return false;
    }
    
    private void switchScreens()
    {
    	Button b = (Button) findViewById(R.id.btnEdit);
    	switch( vf.getDisplayedChild() )
    	{
    		case 0:
    			updateBars();
    			b.setText("BACK");
    			vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_in_right));
    	        vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_out_left));
    			vf.setDisplayedChild(1);
    			break;
    		case 1:
    			b.setText("EDIT");
    			vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_in_left));
    	        vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_out_right));
    			vf.setDisplayedChild(0);
    			break;
    	}
    }
    
    private void saveToFile(MotionEvent event)
    {
    	if ( event.getAction() == MotionEvent.ACTION_UP ) {
    		//Log.e("SFXR", "here i come !");
    		//Toast.makeText(this, "check:" + event.toString(), Toast.LENGTH_SHORT ).show();

    		rendering = true;
    		pd = ProgressDialog.show( this,"","Rendering. Please wait...",true);
    		
    		RenderTask task = new RenderTask();
    		task.execute(this);
    		
    	}
		
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
    	updateBars();
    }
    
    private void random()
    {
    	sfxr.playing_sample = false;
    	sfxr.randomize();
    	sfxr.resetSample(false);
    	sfxr.playing_sample = true;
    	updateBars();
    }
    
    private void randomFrom( int which )
    {
    	sfxr.playing_sample = false;
    	sfxr.random(which);
    	sfxr.resetSample(false);
    	sfxr.playing_sample = true;
    	updateBars();
    }
    
    /*
     * 
     * PARAM LAYOUT CLASS
     * 
     */
    
    
    public class ParamLayout extends LinearLayout {
    	private Context mContext;
    	private TextView tv;
    	private int paramID;
    	private ShapeDrawable sd;
    	private double paramValue = 0;
    	
    	/**
    	 * @param context
    	 */
    	public ParamLayout(Context context) {
    		super(context);
    		mContext = context;
    		init();
    	}

    	/**
    	 * @param context
    	 * @param attrs
    	 */
    	public ParamLayout(Context context, AttributeSet attrs) {
    		super(context, attrs);
    		mContext = context;
    		init();
    	}
    	
    	private void init()
    	{
    		this.setOrientation(VERTICAL);
    		
    		//this.setMinimumHeight(minHeight)
    		
    		tv = new TextView(mContext);
    		tv.setPadding(6, 15, 6, 15);
    		tv.setTypeface( tf );
        	tv.setGravity( Gravity.CENTER );
        	//tv.setPadding( 0, 20, 0, 20 );
        	tv.setTextColor( 0xFFFFFFFF );
        	addView( tv );
        	
        	final Shape border = new Shape() {
				
        		final Paint borderPaint = new Paint();
        		
				@Override
				public void draw(Canvas canvas, Paint paint) {
					borderPaint.setStrokeWidth(3);
					borderPaint.setColor( 0xFF555555 );
					borderPaint.setStyle( Paint.Style.STROKE );
					
					//canvas.drawRect(0, 0, canvas.getWidth()-30, canvas.getHeight()-30, borderPaint );
					canvas.drawRect(1, 1, getWidth()-2, getHeight()-2, borderPaint );
				}
			};
        	final ShapeDrawable borderDrawable = new ShapeDrawable( border );
        	
        	tv.setBackgroundDrawable( borderDrawable );
        	
        	sd = new ShapeDrawable();
        	sd.setColorFilter( 0xFF880000, Mode.SRC );
        	setBackgroundDrawable( sd );
    	}
    	
    	public void setLabel( String label )
    	{
    		tv.setText( label );
    		tv.invalidate();
    	}
    	
    	public void setParamID( int pid )
    	{
    		paramID = pid;
    	}
    	
    	public int getParamID()
    	{
    		return paramID;
    	}
    	
    	public void setValue( double v )
    	{
    		if ( v < 0 ) v = 0;
    		if ( v > 1 ) v = 1;
    		paramValue = v;
        	//sd.setBounds( 0, 20, (int)(getWidth()*paramValue), getHeight()-10 );
        	invalidate();
    	}
    	
    	@Override
    	protected void onDraw(Canvas canvas) {
    		sd.setBounds( 10, 10, (int)((getWidth()-20)*paramValue+10), getHeight()-10 );
    		super.onDraw(canvas);
    		//invalidate();
    	}
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
          int minSize = AudioTrack.getMinBufferSize( 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT );
          track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, 
                                            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 
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
    
    /**
     * sub-class of AsyncTask
     */
    protected class RenderTask extends AsyncTask<Context, Integer, String>
    {
        // -- run intensive processes here
        // -- notice that the datatype of the first param in the class definition matches the param passed to this method 
        // -- and that the datatype of the last param in the class definition matches the return type of this mehtod
                @Override
                protected String doInBackground( Context... params ) 
                {
                        //-- on every iteration
                        //-- runs a while loop that causes the thread to sleep for 50 milliseconds 
                        //-- publishes the progress - calls the onProgressUpdate handler defined below
                        //-- and increments the counter variable i by one
                		//must_play = false;
                		
                    	sfxr.playing_sample = false;
                		sfxr.resetSample(false);
                    	sfxr.playing_sample = true;
                		
                		int dataSize = 180000;
                		int realDataSize = 0;
                		
                		// collect sample data in arraylist before creating the file
                		List<Short> samples = new ArrayList<Short>();
                		try{
	                		while( sfxr.playing_sample ) {
	                			samples.add((short)(sfxr.synthSample()*32767) );
	                			++realDataSize;
	                		}
	                		System.out.println( "_sfxr generated samples  : " + realDataSize );
	                		dataSize = realDataSize*2;
			                		
		                }catch(Exception e)
		        		{
		        			//Toast.makeText(getApplicationContext(), "Error generating file...", Toast.LENGTH_SHORT).show();
		        			System.out.println("_sfxr error : " + e.getMessage() );
		        			return "error";
		        		}
                		
                		// get current time for the filename
                		long now = System.currentTimeMillis();
                		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                		System.out.println( "_sfxr getting time  : " + date.format(now) );
                		

                		// create the file
                		String filename = "sfxr-"+date.format(now)+".wav";
                		System.out.println("_sfxr saving file");
                		try{
                		
                			File folder = new File(Environment.getExternalStorageDirectory() + "/sfxr");
                			if (!folder.exists()) folder.mkdir();
                			
	                		//File file = new File(Environment.getExternalStorageDirectory(), filename);
	                		File file = new File(folder, filename);
	                		DataOutputStream outFile = new DataOutputStream( new FileOutputStream(file) );
	                		
	                		// write the wav file per the wav file format
	            			outFile.writeBytes("RIFF");					// 00 - RIFF
	            			outFile.write(intToByteArray((int)dataSize+36), 0, 4);		// 04 - how big is the rest of this file?
	            			//outFile.wri
	            			outFile.writeBytes("WAVE");					// 08 - WAVE
	            			outFile.writeBytes("fmt ");					// 12 - fmt 
	            			outFile.write(intToByteArray(16), 0, 4);	// 16 - size of this chunk
	            			outFile.write(shortToByteArray((short)1), 0, 2);		// 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
	            			outFile.write(shortToByteArray((short)1), 0, 2);	// 22 - mono or stereo? 1 or 2?  (or 5 or ???)
	            			outFile.write(intToByteArray(44100), 0, 4);		// 24 - samples per second (numbers per second)
	            			outFile.write(intToByteArray(88200), 0, 4);		// 28 - bytes per second
	            			outFile.write(shortToByteArray((short)2), 0, 2);	// 32 - # of bytes in one sample, for all channels
	            			outFile.write(shortToByteArray((short)16), 0, 2);	// 34 - how many bits in a sample(number)?  usually 16 or 24
	            			outFile.writeBytes("data");					// 36 - data
	            			outFile.write(intToByteArray((int)dataSize), 0, 4);		// 40 - how big is this data chunk
	            			
	                		//byte[] data = new byte[10300];
	                		
	            			System.out.println("_sfxr header written : "+realDataSize);
	            			
	                		for(int i = 0;i<realDataSize;i++){
	                			outFile.write( shortToByteArray(samples.get(i)) );
	                		}
	                		/*
	                		while( sfxr.playing_sample ) {
	                			//data[i] = 0;
	                			//wav.myData[i] = 0;//sfxr.synthSample();
	                			//float
	                			//System.out.println("SFXR wave : "+realDataSize);
	                			outFile.write(shortToByteArray((short)(sfxr.synthSample()*32767)),0,2);
	                			//i+=2;
	                			//if ( sfxr.playing_sample ) 
	                			realDataSize+=2;
	                		}
	                		*/
	                		
	                		System.out.println("_sfxr file saved : "+realDataSize);
	                		
	            			outFile.flush();
	            			outFile.close();
	            			outFile = null;
	            			file = null;
	            			folder = null;
                		
	            			
	            			//Toast.makeText(getApplicationContext(), "File : " + filename + "saved to SD card", Toast.LENGTH_SHORT).show();
                		}catch(Exception e)
                		{
                			Toast.makeText(getApplicationContext(), "Error saving file...", Toast.LENGTH_SHORT).show();
                			System.out.println("_sfxr error : " + e.getMessage() );
                			return "error";
                		}

                		return "ok";
                		
                }
                
            	// returns a byte array of length 4
            	private byte[] intToByteArray(int i)
            	{
            		byte[] b = new byte[4];
            		b[0] = (byte) (i & 0x00FF);
            		b[1] = (byte) ((i >> 8) & 0x000000FF);
            		b[2] = (byte) ((i >> 16) & 0x000000FF);
            		b[3] = (byte) ((i >> 24) & 0x000000FF);
            		return b;
            	}

            	// convert a short to a byte array
            	public byte[] shortToByteArray(short data)
            	{
            		return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
            	}
                
                // -- gets called just before thread begins
                @Override
                protected void onPreExecute() 
                {
                        //Log.i( "makemachine", "onPreExecute()" );
                        super.onPreExecute();
                        
                }
                
                // -- called from the publish progress 
                // -- notice that the datatype of the second param gets passed to this method
                @Override
                protected void onProgressUpdate(Integer... values) 
                {
                        super.onProgressUpdate(values);
                        //pd.setProgress(values[0]);
                        //Log.i( "makemachine", "onProgressUpdate(): " +  String.valueOf( values[0] ) );
                        /*
                        _percentField.setText( ( values[0] * 2 ) + "%");
                        _percentField.setTextSize( values[0] );
                        */
                }
                
                // -- called if the cancel button is pressed
                @Override
                protected void onCancelled()
                {
                        super.onCancelled();
                }

                // -- called as soon as doInBackground method completes
                // -- notice that the third param gets passed to this method
                @Override
                protected void onPostExecute( String result ) 
                {
                        super.onPostExecute(result);
                        pd.dismiss();
                		rendering = false;
                		
                		Toast t = Toast.makeText(getApplicationContext(), "File saved to SD card", Toast.LENGTH_SHORT);
                		t.show();
                		
                }
    }    
}