package bappleton.vocal;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.Random;

import be.tarsos.dsp.pitch.FastYin;
import be.tarsos.dsp.pitch.PitchDetectionResult;

/**
 * Created by Brian on 10/22/2016.
 */

public class pitchDetect {

    //Declare AudioRecord object and necessary global attributes
    AudioRecord AR1;
    int bufferElements; //number of elements in the buffer
    short recorder_data[]; //array of shorts into which we'll read AR1 data. A short is a 16-bit signed integer.

    //OBJECTS FOR FFT APPROACH
    FFT fft;
    double[] re; //real part of time-domain signal, gets replaced with frequency-domain info when fft is run
    double[] im; //imaginary part of time-domain signal, gets replaced with frequency-domain info when fft is run
    double[] f;  //frequency data for each point in re and im arrays

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

    public boolean initialize() {

        //Construct settings for initializing the AudioRecord instance
        int audioSource = MediaRecorder.AudioSource.DEFAULT; //MediaRecorder.AudioSource.UNPROCESSED requires API24
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        //int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //NEEDS to mesh with read() call and bytesPerElement; Use with reading into a short
        int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
        //bufferElements = 1024; //number of array elements to fetch from AudioRecord
        //bufferElements = 8192; //185 ms between samples
        bufferElements = 16384; //370 ms between samples
        //int bytesPerElement = 2; //when reading into a short, each element consumes 2 bytes
        int bytesPerElement = 4; //when reading into a float, each element consumes 4 bytes
        int bufferSizeInBytes = bufferElements * bytesPerElement;

        //Make sure that bufferSizeInByte meets the minimum buffer size (MBS) requirement
        int MBS = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSizeInBytes < MBS) {
            Log.e("pitchDetect", "Requested buffer size is insufficiently large");
        }

        //Create AR1, our AudioRecord instance
        AR1 = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

        //check whether the audioRecord instance was properly initialized, print result to console
        if (AR1.getState() == AudioRecord.STATE_INITIALIZED) {
            Log.i("pitchDetect", "Successfully initialized");
            IS_AR1_INITIALIZED = true;
        } else if (AR1.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e("pitchDetect", "Not successfully initialized");
            IS_AR1_INITIALIZED = false;
        } else {
            Log.e("pitchDetect", "Unrecognized state");
            IS_AR1_INITIALIZED = false;
        }

        ///
        //PREPARE FFT OBJECT FOR FFT APPROACH
        //Key variables: sampleRateInHz and bufferElements
        fft = new FFT(bufferElements);
        re = new double[bufferElements];
        im = new double[bufferElements];
        f = new double[bufferElements];
        //calculate and store the frequency data in f
        for (int i = 0; i < bufferElements; i++) {
            f[i] = i * sampleRateInHz / bufferElements;
        }
        //Allocate storage for the buffer into which we'll read audio samples
        recorder_data = new short[bufferElements];
        //END FFT OBJECT PREP
        ///

        ///
        //PREPARE A TARSOS YIN OBJECT FOR TESTING
        fy_pitchDetect = new FastYin(sampleRateInHz, bufferElements);
        recorder_data_yin = new float[bufferElements];
        vpr = new vocalPitchResult();
        AR1.setPositionNotificationPeriod(bufferElements);
        recordDataAvailable = new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {
                Log.i("pitchDetect", "OnMarkerReached");
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                //Log.i("pitchDetect", "OnPeriodicNotification");
                //recorder.read(recorder_data_yin, 0, bufferElements, AudioRecord.READ_BLOCKING);
                time1 = System.currentTimeMillis();
                read_yin();
                time2 = System.currentTimeMillis();
                processYin();
                Log.i("pitchDetect", "New recorder data. Read: " + (time2 - time1) + " ms. Process: " + (System.currentTimeMillis() - time2) + " ms. Fetch interval: " + (time1 - time3) + " ms.");
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
        pitch_yin = (int) pitchDetectResult_yin.getPitch();

        //Convert the YIN pitch detection result into VocalPitchDetection object, which allows access to enhanced information
        vpr.setPitchDetectionResult(pitchDetectResult_yin);

    }

    public void start() {
        if (IS_AR1_INITIALIZED && !IS_AR1_RECORDING) {
            AR1.startRecording();
            IS_AR1_RECORDING = true;
        } else {
            Log.e("pitchDetect", "Cannot start recording. Either already recording or not initialized successfully");
        }
    }

    public void stop() {
        if (IS_AR1_RECORDING && IS_AR1_INITIALIZED) {
            AR1.stop();
            IS_AR1_RECORDING = false;
        } else {
            Log.e("pitchDetect", "Cannot stop recording. Not in a recording state.");
        }
    }

    public void tearDown() {

        if (IS_AR1_RECORDING) {
            stop();
        }
        if (AR1 != null) {
            AR1.release(); //releases native resources; set AR1 to null after this call
            AR1 = null;
        } else {
            Log.e("pitchDetect", "Cannot tear down. Null AudioRecord instance.");
        }
        //should we also de-allocate the recording data buffer?
        IS_AR1_INITIALIZED = false;
    }

    private boolean read() {
        //READ FUNCTION FOR ORIGINAL APPROACH
        //READS AUDIO DATA INTO short[] recorder_data[]
        int AR1_result;
        boolean success = false;

        if (IS_AR1_INITIALIZED && IS_AR1_RECORDING) {
            AR1_result = AR1.read(recorder_data, 0, bufferElements);

            if (AR1_result == bufferElements || AR1_result == 0) {
                //read completed without error
                //Log.i("pitchDetect", "Read successful");
                success = true;
            } else {
                Log.e("pitchDetect", "Read error");
                success = false;
            }
        } else {
            Log.e("pitchDetect", "Can't read. Not initalized and recording.");
            success = false;
        }

        return success;
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
                //Log.i("pitchDetect", "Read successful");
                success = true;
            } else {
                Log.e("pitchDetect", "Read error");
                success = false;
            }
        } else {
            Log.e("pitchDetect", "Can't read. Not initalized and recording.");
            success = false;
        }

        return success;
    }

    public vocalPitchResult getPitch() {
        return vpr;
    }

}

//    public String getKeyID(float hertz) {
//
//
//        float A4_freq = 440; //Frequency in Hz of A4, the 49th key on the piano
//
////        float pitches[][] = new float[9][12]; //[row][col]. Row=octave from 0 to 8. Col = note from C to B.
////        float n = 0; //Seqential number of the key on the piano
////        for (int i=0; i<9; i++) {
////            for (int j=0; j<12; j++) {
////                n = (float) (i*12 + j);
////                pitches[i][j] = (float) Math.pow(2, (n-49-8)/12)*A4_freq; //We start at C0, which is the -8th key relative to A4(49)
////                Log.i("pitchDetect", "freq is " + pitches[i][j]);
////            }
////        }
//
//        double key = 0;
//        key = Math.log10(hertz/A4_freq)*12/Math.log10(2) + 49;
//
//        return Double.toString(key);
//    }
//
//}


