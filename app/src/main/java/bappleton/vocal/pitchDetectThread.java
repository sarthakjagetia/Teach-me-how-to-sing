package bappleton.vocal;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import be.tarsos.dsp.pitch.FastYin;
import be.tarsos.dsp.pitch.PitchDetectionResult;

/**
 * Created by Brian on 11/23/2016.
 */

public class pitchDetectThread extends Thread {

    //Handler for my message loop
    private Handler pitchDetectHandler;

    //Handler to parent thread, so we can send it messages
    private Handler parentHandler;

    private final int CASE_THREAD_IDENTIFY_YOURSELF = 1001;
    private final int CASE_START_DETECTION          = 1002;
    private final int CASE_STOP_DETECTION           = 1003;
    private final int SIGNAL_DETECTION_RUNNING      = 2004;
    private final int SIGNAL_DETECTION_STOPPED      = 2005;
    private final String TAG = "pitchDetectThread";





    public pitchDetectThread(Handler parentHandler) {
        //Do something when thread is constructed. This is executed on the calling thread.
        //Save handler of the parent so we can send it messages
        this.parentHandler = parentHandler;
    }

    @Override
    public void run() {
        Looper.prepare();

        //Define the message handler for this thread
        pitchDetectHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CASE_THREAD_IDENTIFY_YOURSELF:
                        //Print the ID of the thread
                        Log.i(TAG, Thread.currentThread().getName());
                        break;
                    case CASE_START_DETECTION:
                        if (!IS_AR1_RECORDING) {
                            if (initialize()) {
                                AR1.startRecording();
                                //Update internal recording flag
                                IS_AR1_RECORDING = true;
                                Log.i(TAG, "Pitch detection is running.");
                                //Send a message to the parent thread to tell it we're ready
                                Message recordingStarted = Message.obtain();
                                recordingStarted.what = SIGNAL_DETECTION_RUNNING;
                                parentHandler.sendMessage(recordingStarted);
                            }
                            else {
                                Log.e(TAG, "Initialization error.");
                            }
                        }
                        else {
                            Log.e(TAG, "Pitch detection is already running.");
                        }
                        break;
                    case CASE_STOP_DETECTION:
                        if (IS_AR1_RECORDING) {
                            AR1.stop();
                            if (AR1 != null) {
                                AR1.release();
                            }
                            AR1 = null;
                            Log.i(TAG, "Pitch detection stopped.");
                        }
                        else {
                            Log.e(TAG, "Nothing to stop. Pitch detection not running.");
                        }
                        //Update internal recording flag
                        IS_AR1_RECORDING = false;
                        //Send a message to the parent thread to tell it we've stopped detection
                        Message recordingStopped = Message.obtain();
                        recordingStopped.what = SIGNAL_DETECTION_STOPPED;
                        parentHandler.sendMessage(recordingStopped);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
        Looper.loop();

    }

    //Declare AudioRecord object and necessary global attributes
    AudioRecord AR1;
    int bufferElements; //number of elements in the buffer

    //OBJECTS FOR FAST YIN APPROACH
    FastYin fy_pitchDetect;
    float recorder_data_yin[];
    PitchDetectionResult pitchDetectResult_yin;
    AudioRecord.OnRecordPositionUpdateListener recordDataAvailable;
    int pitch_yin = 0; //Being replaced with an object
    vocalPitchResult vpr;

    //Private booleans to indicate status of the AudioRecord object
    private boolean IS_AR1_INITIALIZED = false;
    private boolean IS_AR1_RECORDING = false;

    //Private variables to assist processing
    int dominantPitch = 0;
    long time1 = 0;
    long time2 = 0;
    long time3 = 0;

    private boolean initialize() {

        //Construct settings for initializing the AudioRecord instance
        int audioSource = MediaRecorder.AudioSource.DEFAULT; //MediaRecorder.AudioSource.UNPROCESSED requires API24
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        //int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //NEEDS to mesh with read() call and bytesPerElement; Use with reading into a short
        int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
        //bufferElements = 1024; //number of array elements to fetch from AudioRecord
        bufferElements = 8192; //185 ms between samples
        //bufferElements = 16384; //370 ms between samples
        //int bytesPerElement = 2; //when reading into a short, each element consumes 2 bytes
        int bytesPerElement = 4; //when reading into a float, each element consumes 4 bytes
        int bufferSizeInBytes = bufferElements * bytesPerElement;

        //Make sure that bufferSizeInByte meets the minimum buffer size (MBS) requirement
        int MBS = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSizeInBytes < MBS) {
            Log.e(TAG, "Requested buffer size is insufficiently large");
        }

        //Create AR1, our AudioRecord instance
        AR1 = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

        //check whether the audioRecord instance was properly initialized, print result to console
        if (AR1.getState() == AudioRecord.STATE_INITIALIZED) {
            Log.i(TAG, "Successfully initialized");
            IS_AR1_INITIALIZED = true;
        } else if (AR1.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "Not successfully initialized");
            IS_AR1_INITIALIZED = false;
        } else {
            Log.e(TAG, "Unrecognized state");
            IS_AR1_INITIALIZED = false;
        }

        ///
        //PREPARE A TARSOS YIN OBJECT
        fy_pitchDetect = new FastYin(sampleRateInHz, bufferElements);
        recorder_data_yin = new float[bufferElements];
        vpr = new vocalPitchResult();

        //Make a callback function for the AudioRecord instance.
        //This gets called when there are bufferElements of new recorder data available
        //The value of the callback is that the AR1 read process executes in ~0ms. It doesn't block.
        AR1.setPositionNotificationPeriod(bufferElements);
        recordDataAvailable = new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {
                Log.i(TAG, "OnMarkerReached");
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                //Log.i(TAG, "OnPeriodicNotification");
                //recorder.read(recorder_data_yin, 0, bufferElements, AudioRecord.READ_BLOCKING);
                time1 = System.currentTimeMillis();
                read_yin();
                time2 = System.currentTimeMillis();
                processYin();
                Log.i(TAG, "New recorder data. Read: " + (time2 - time1) + " ms. Process: " + (System.currentTimeMillis() - time2) + " ms. Fetch interval: " + (time1 - time3) + " ms.");
                time3 = System.currentTimeMillis();
            }
        };
        AR1.setRecordPositionUpdateListener(recordDataAvailable);
        //END TARSOS YIN PREP
        ///

        return IS_AR1_INITIALIZED;
    }

    private void processYin() {
        pitchDetectResult_yin = fy_pitchDetect.getPitch(recorder_data_yin);

        //Deprecated, mark for delete
        //pitch_yin = (int) pitchDetectResult_yin.getPitch();

        //Convert the YIN pitch detection result into VocalPitchDetection object, which allows access to enhanced information
        vpr.setPitchDetectionResult(pitchDetectResult_yin);

    }


    public void startPitchDetection() {
        //Called by parent thread to begin recording
        Message startMsg = Message.obtain();
        startMsg.what = CASE_START_DETECTION;
        pitchDetectHandler.sendMessage(startMsg);
    }

    public void stopPitchDetection() {
        //Called by parent thread to stop recording.
        //This code executes on the parent thread
        Message stopMsg = Message.obtain();
        stopMsg.what = CASE_STOP_DETECTION;
        pitchDetectHandler.sendMessage(stopMsg);
    }


    private boolean read_yin() {
        //READ FUNCTION FOR YIN METHOD
        //READS AUDIO DATA INTO float[] recorder_data_yin[];
        int AR1_result;
        boolean success = false;

        if (IS_AR1_INITIALIZED && IS_AR1_RECORDING) {
            //Yin method wants a float. Reading into a float requires API23. So that's going to be our new minimum API?
            AR1_result = AR1.read(recorder_data_yin, 0, bufferElements, AudioRecord.READ_BLOCKING);

            if (AR1_result == bufferElements || AR1_result == 0) {
                //read completed without error
                //Log.i(TAG, "Read successful");
                success = true;
            } else {
                Log.e(TAG, "Read error");
                success = false;
            }
        } else {
            Log.e(TAG, "Can't read. Not initalized and recording.");
            success = false;
        }

        return success;
    }

    public vocalPitchResult getPitch() {
        //PUBLIC FUNCTION NEEDS TO BE UPDATED
        return vpr;
    }

    public void printThreadName() {
        //Log.i("pitchDetectThread", Thread.currentThread().getName() );
        Message msg = Message.obtain();
        msg.what = CASE_THREAD_IDENTIFY_YOURSELF;

        if (pitchDetectHandler == null) {
            Log.i(TAG, "My handler is null.");
        }
        else {
            pitchDetectHandler.sendMessage(msg);
        }
    }


}



