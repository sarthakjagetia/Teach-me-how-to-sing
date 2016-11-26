package bappleton.vocal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class vocal extends AppCompatActivity {

    private Handler mainHandler;
    pitchDetectThread thread_test;

    private final int SIGNAL_DETECTION_RUNNING      = 2004;
    private final int SIGNAL_DETECTION_STOPPED      = 2005;
    private final int SIGNAL_SONG_COMPLETE  = 4000;

    //Define int constants for permission handling
    public final int PERMISSIONS_REQUEST_RECORD_AUDIO = 2000;
    public final int PERMISSIONS_REQUEST_INTERNET     = 2001;
    boolean PERMISSIONS_RECORD_AUDIO = false;
    boolean PERMISSIONS_INTERNET = false;

    //Define int constants for application behavior
    private final int pitch_refresh_period = 100; //Delay between UI updates for pitch, in ms

    //Define booleans to control application flow
    private boolean MAIN_UI_SONG_RUNNING = false; //Indicates whether the UI thread and pitch detect thread should be in a request pitch/receive pitch loop
    
    private final String TAG = "vocalMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.vocal);

        //Define a message handler for the main UI thread
        mainHandler = new Handler(Looper.getMainLooper()) {
            //Override handleMessage to intercept messages
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SIGNAL_SONG_COMPLETE:
                        //Recieved message from vocalUI object that the song has ended
                        MAIN_UI_SONG_RUNNING = false;
                        Button togglePDButton = (Button) findViewById(R.id.toggleButton);
                        togglePDButton.setText("PLAY");
                        vocalUI VUI = (vocalUI) findViewById(R.id.vocalUIdisplay);
                        VUI.stopRendering();
                        break;
                    default:
                        //Let the parent class handle any messages that I don't
                        super.handleMessage(inputMessage);
                }
            }
        };

        //This app requires use of the microphone. Check recording permissions and request if necessary.
        checkPermissions();

    }


    public void toggleDetection(View view) {

        vocalUI VUI = (vocalUI) findViewById(R.id.vocalUIdisplay);
        Button togglePDButton = (Button) findViewById(R.id.toggleButton);

        if(!MAIN_UI_SONG_RUNNING) {
            VUI.beginRendering();
            VUI.setSong(new vocalExerciseLibrary().demoSong1());
            VUI.beginSong();
            VUI.setParentHandler(mainHandler);
            MAIN_UI_SONG_RUNNING = true;
            togglePDButton.setText("STOP");
        }
        else {
            VUI.stopRendering();
            VUI.endSong();
            MAIN_UI_SONG_RUNNING = false;
            togglePDButton.setText("PLAY");
        }

    }

    public void checkPermissions(){
        //CHECK FOR PERMISSION TO RECORD AUDIO
        // Assume thisActivity is the current activity
        //Older APIs request permission at install, API 23 and above requests permission as needed
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED){
            //including a "B_" here so I know it's coming from me
            Log.i("B_RECORD_AUDIO", "Permission denied, requesting RECORD_AUDIO permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        else if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            Log.i("B_RECORD_AUDIO", "Permission granted");
            PERMISSIONS_RECORD_AUDIO = true;
        }
        else {
            Log.e("B_RECORD_AUDIO", "Permission unrecognized");
        }

        //Check for permission to access internet
        permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        if (permissionCheck == PackageManager.PERMISSION_DENIED){
            Log.i(TAG, "Internet permission denied, requesting permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    PERMISSIONS_REQUEST_INTERNET);
        }
        else if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Internet permission granted");
            PERMISSIONS_INTERNET = true;
        }
        else {
            Log.e(TAG, "Internet permission state unrecognized.");
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    PERMISSIONS_RECORD_AUDIO = true;
                    Log.i("RECORD_AUDIO", "Permission granted.");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    PERMISSIONS_RECORD_AUDIO = false;
                    Log.i("RECORD_AUDIO", "Permission denied.");
                }
                break;
            case PERMISSIONS_REQUEST_INTERNET:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    PERMISSIONS_INTERNET = true;
                    Log.i(TAG, "Internet permission granted.");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    PERMISSIONS_INTERNET = false;
                    Log.i(TAG, "Internet permission denied.");
                }
                break;
            // other 'case' lines to check for other
            // permissions this app might request
        }

    }
}