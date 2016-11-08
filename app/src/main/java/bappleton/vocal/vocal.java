package bappleton.vocal;

import android.Manifest;
import android.content.pm.PackageManager;
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
    pitchThread pitch_det;

    //Define int constants for message handling
    private final int CASE_BRIAN_WAS_HERE           = 1000;
    private final int CASE_THREAD_IDENTIFY_YOURSELF = 1001;
    private final int CASE_RECEIVE_PITCH            = 1002;
    private final int CASE_INITIALIZE               = 1003;
    private final int CASE_TEAR_DOWN                = 1004;
    private final int CASE_START_DETECTION          = 1005;
    private final int CASE_STOP_DETECTION           = 1006;
    private final int CASE_GET_PITCH                = 1007;
    private final int CASE_START_MAIN_UI_PITCH_DETECTION = 1008;
    private final int CASE_STOP_MAIN_UI_PITCH_DETECTION  = 1009;
    private final int CASE_READY_FOR_PITCH_REQUEST  = 1010;
    private final int CASE_TEAR_DOWN_MAIN_UI_PITCH_DETECTION = 1011;

    //Define int constants for permission handling
    public final int PERMISSIONS_REQUEST_RECORD_AUDIO = 2000;
    boolean PERMISSIONS_RECORD_AUDIO = false;

    //Define int constants for application behavior
    private final int pitch_refresh_period = 250; //Delay between UI updates for pitch, in ms

    //Define booleans to control application flow
    private boolean MAIN_UI_PITCH_DETECTION_RUNNING = false; //Indicates whether the UI thread and pitch detect thread should be in a request pitch/receive pitch loop
    private boolean MAIN_UI_PITCH_DETECTION_READY = false; //Indicates whether the pitch detect thread is ready to be asked for a pitch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //TESTING TESTING 11/3 - commented this out
        setContentView(R.layout.vocal);
        ///BEGIN TEST CODE
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setContentView(new vocalUI(this));

        //END TESTING

        //Instantiate the pitch detection thread and run it
        pitch_det = new pitchThread();
        pitch_det.start();

        //Initialize a message to use for communicatio with the pitch detection thread
        //Message msg_get_pitch = Message.obtain();

        //Define a message handler for the main UI thread
        mainHandler = new Handler(Looper.getMainLooper()) {
            //Override handleMessage to intercept messages
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    //handle messages here
                    case CASE_BRIAN_WAS_HERE:
                        Log.i("UI_message", "holy sweet jesus");
                        break;
                    case CASE_READY_FOR_PITCH_REQUEST:
                        //we get this message back after sending CASE_START_DETECTION to the pitch_det thread
                        MAIN_UI_PITCH_DETECTION_READY = true;
                        MAIN_UI_PITCH_DETECTION_RUNNING = true;
                        //kick off the pitch request loop by sending the first CASE_GET_PITCH message to the pitch_det thread. From here, pitch info is sent to CASE_RECEIVE_PITCH
                        Message msg_get_first_pitch = Message.obtain();
                        msg_get_first_pitch.what = CASE_GET_PITCH;
                        pitch_det.pitchHandler.sendMessage(msg_get_first_pitch);
                        break;
                    case CASE_RECEIVE_PITCH:
                        //Update UI with the pitch
                        vocalPitchResult vpr = (vocalPitchResult)inputMessage.obj;
                        updateUIPitchView(vpr);
                        if (MAIN_UI_PITCH_DETECTION_RUNNING) {
                            //If we're in a running state, send a new message to the background pitch thread to tell it to give us more pitch data
                            Message msg_get_pitch = Message.obtain();
                            msg_get_pitch.what = CASE_GET_PITCH;
                            pitch_det.pitchHandler.sendMessageDelayed(msg_get_pitch, pitch_refresh_period);
                        }
                        break;
                    case CASE_START_MAIN_UI_PITCH_DETECTION:
                        //MAIN_UI_PITCH_DETECTION_RUNNING = true;
                        //Send a new message to the background pitch thread to tell it to give us more pitch data
                        //Message msg_get_pitch = Message.obtain();
                        //msg_get_pitch.what = CASE_GET_PITCH;
                        //pitch_det.pitchHandler.sendMessage(msg_get_pitch);

                        if (MAIN_UI_PITCH_DETECTION_READY){
                            //If the pitch detection thread is already ready for a pitch request, go ahead and kick off the request loop
                            MAIN_UI_PITCH_DETECTION_RUNNING = true;
                            Message msg_get_first_pitch_1 = Message.obtain();
                            msg_get_first_pitch_1.what = CASE_GET_PITCH;
                            pitch_det.pitchHandler.sendMessage(msg_get_first_pitch_1);
                        }
                        else {
                            //If it's not yet ready, ask the pitch detection thread to initialize and start recording. We'll get CASE_READY_FOR_PITCH_REQUEST back if it was successful
                            Message msg_begin_detection = Message.obtain();
                            msg_begin_detection.what = CASE_START_DETECTION;
                            pitch_det.pitchHandler.sendMessage(msg_begin_detection);
                        }
                        break;
                    case CASE_STOP_MAIN_UI_PITCH_DETECTION:
                        MAIN_UI_PITCH_DETECTION_RUNNING = false;
                        MAIN_UI_PITCH_DETECTION_READY = false;
                        Message msg_stop_detection = Message.obtain();
                        msg_stop_detection.what = CASE_STOP_DETECTION;
                        pitch_det.pitchHandler.sendMessage(msg_stop_detection);
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
        //Test background thread message handling
//        if (pitch_det.pitchHandler != null) {
//            Message msg = Message.obtain();
//            msg.what = CASE_THREAD_IDENTIFY_YOURSELF;
//            pitch_det.pitchHandler.sendMessage(msg);
//        }
//        else {
//            Log.e("PitchHandler", "Null");
//        }
        Message togglePD = Message.obtain();
        Button togglePDButton = (Button) findViewById(R.id.toggleButton);
        vocalUI VUI = (vocalUI) findViewById(R.id.vocalUIdisplay);

        if (MAIN_UI_PITCH_DETECTION_RUNNING){
            //if it's currently running, stop it
            togglePD.what = CASE_STOP_MAIN_UI_PITCH_DETECTION;
            togglePDButton.setText("START DETECTION");
            VUI.endSong();
        }
        else {
            //if it's not currently running, start it
            togglePD.what = CASE_START_MAIN_UI_PITCH_DETECTION;
            togglePDButton.setText("STOP DETECTION");
            VUI.beginSong();
        }

        mainHandler.sendMessage(togglePD);

    }

    private void updateUIPitchView(vocalPitchResult vpr) {
        //TextView pitch_text = (TextView) findViewById(R.id.pitchView);
        //pitch_text.setText(Integer.toString(pitch) + " Hz");

        TextView pitchView = (TextView) findViewById(R.id.pitchView);
        TextView freqView = (TextView) findViewById(R.id.freqView);
        TextView clarityView = (TextView) findViewById(R.id.clarityView);
        TextView errorView = (TextView) findViewById(R.id.errorView);

        int pitchHz = vpr.getPitchHzInt();

        if (pitchHz == -1) {
            //if nothing was detected, make the interface respond nicely
            pitchView.setText("");
            freqView.setText("0 Hz");
        }
        else {
            //something detected, let's roll
            pitchView.setText(vpr.getNoteName());
            freqView.setText(vpr.getPitchHzInt() + " Hz");
        }

        clarityView.setText(Math.round(vpr.getClarity()*100) + " %");
        errorView.setText(vpr.getErrorPercent() + " %");

    }


    class pitchThread extends Thread {
        //WOULD IT HAVE BEEN BETTER to somehow integrate this class into the pitchDetect class??
        // Declare the handler for this thread
        public Handler pitchHandler;

        //Declare a pitchDetect object, which provides most of the functionality in this class
        pitchDetect pd = new pitchDetect();
        //Message pitch_msg = Message.obtain(); don't do this? error: message object already in use


        public pitchThread() {
            //Do something automatically when this class is constructed
        }

        @Override
        public void run() {
            //Prepare this thread to implement a message queue
            Looper.prepare();

            //Define the message handler for this thread
            pitchHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case CASE_THREAD_IDENTIFY_YOURSELF:
                            //Print the ID of the thread
                            Log.i("BG_message:", Thread.currentThread().getName());
                            break;
                        case CASE_START_DETECTION:
                            //Initialize recording object, begin recording, begin detection
                            if(pd.initialize()) {
                                pd.start();
                                //send back a message to tell the UI thread that we're ready
                                Message ready_msg = Message.obtain();
                                ready_msg.what = CASE_READY_FOR_PITCH_REQUEST;
                                mainHandler.sendMessage(ready_msg);
                            }
                            break;
                        case CASE_STOP_DETECTION:
                            //Stop recording, stop detection, release recording object
                            pd.stop();
                            pd.tearDown();
                            break;
                        case CASE_GET_PITCH:
                            //Request the pitch from the pitchdetect object, send the result to the UI thread
                            Message pitch_msg = Message.obtain();
                            pitch_msg.what = CASE_RECEIVE_PITCH;
                            //pitch_msg.arg1 = pd.getPitch();
                            pitch_msg.obj = pd.getPitch();
                            mainHandler.sendMessage(pitch_msg);
                            break;
                        default:
                            super.handleMessage(msg);
                    }

                }
            };

            //TESTING
            //Try sending a mesage to the main UI thread
            //Message msg = Message.obtain();
            //msg.what = CASE_BRIAN_WAS_HERE;
            //msg.arg1 = 1234;
            //mainHandler.sendMessage(msg);

            //Begin looping to handle messages
            Looper.loop();

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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO: {
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
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}