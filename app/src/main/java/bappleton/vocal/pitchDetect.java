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

    //Private booleans to indicate status of the AudioRecord object
    private boolean IS_AR1_INITIALIZED = false;
    private boolean IS_AR1_RECORDING   = false;

    //Private variables to assist processing
    int dominantPitch = 0;

    public boolean initialize(){

        //Construct settings for initializing the AudioRecord instance
        int audioSource = MediaRecorder.AudioSource.DEFAULT; //MediaRecorder.AudioSource.UNPROCESSED requires API24
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        //int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //NEEDS to mesh with read() call and bytesPerElement; Use with reading into a short
        int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
        //bufferElements = 1024; //number of array elements to fetch from AudioRecord
        bufferElements = 16384; //trying this
        //int bytesPerElement = 2; //when reading into a short, each element consumes 2 bytes
        int bytesPerElement = 4; //when reading into a float, each element consumes 4 bytes
        int bufferSizeInBytes = bufferElements * bytesPerElement;

        //Make sure that bufferSizeInByte meets the minimum buffer size (MBS) requirement
        int MBS = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig, audioFormat);
        if (bufferSizeInBytes < MBS) {
            Log.e("pitchDetect", "Requested buffer size is insufficiently large");
        }

        //Create AR1, our AudioRecord instance
        AR1 = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

        //check whether the audioRecord instance was properly initialized, print result to console
        if (AR1.getState() == AudioRecord.STATE_INITIALIZED){
            Log.i("pitchDetect", "Successfully initialized");
            IS_AR1_INITIALIZED = true;
        }
        else if (AR1.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e("pitchDetect", "Not successfully initialized");
            IS_AR1_INITIALIZED = false;
        }
        else {
            Log.e("pitchDetect", "Unrecognized state");
            IS_AR1_INITIALIZED = false;
        }

        ///
        //PREPARE FFT OBJECT FOR FFT APPROACH
        //Key variables: sampleRateInHz and bufferElements
        fft = new FFT(bufferElements);
        re = new double[bufferElements];
        im = new double[bufferElements];
        f  = new double[bufferElements];
        //calculate and store the frequency data in f
        for (int i=0; i<bufferElements; i++ ) {
            f[i] = i*sampleRateInHz/bufferElements;
        }
        //Allocate storage for the buffer into which we'll read audio samples
        recorder_data = new short[bufferElements];
        //END FFT OBJECT PREP
        ///

        ///
        //PREPARE A TARSOS YIN OBJECT FOR TESTING
        fy_pitchDetect = new FastYin(sampleRateInHz, bufferElements);
        recorder_data_yin = new float[bufferElements];
        //END TARSOS YIN PREP
        ///

        return IS_AR1_INITIALIZED;
    }

    public void start() {
        if (IS_AR1_INITIALIZED && !IS_AR1_RECORDING) {
            AR1.startRecording();
            IS_AR1_RECORDING = true;
        }
        else {
            Log.e("pitchDetect", "Cannot start recording. Either already recording or not initialized successfully");
        }
    }

    public void stop() {
        if (IS_AR1_RECORDING && IS_AR1_INITIALIZED) {
            AR1.stop();
            IS_AR1_RECORDING = false;
        }
        else {
            Log.e("pitchDetect", "Cannot stop recording. Not in a recording state.");
        }
    }

    public void tearDown() {

        if (IS_AR1_RECORDING) {
            stop();
        }
        AR1.release(); //releases native resources; set AR1 to null after this call
        AR1 = null;
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
        }
        else {
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
        }
        else {
            Log.e("pitchDetect", "Can't read. Not initalized and recording.");
            success = false;
        }

        return success;
    }

    public int getPitch() {

        if(read_yin()){
            pitchDetectResult_yin = fy_pitchDetect.getPitch(recorder_data_yin);
            //we get a float back... can deal with that problem later. For now, truncate:
            return (int) pitchDetectResult_yin.getPitch();
        }




        //start with generating a random integer.
        //Random rand = new Random();
        //dominantPitch = rand.nextInt(50)+1; //1 to 50
        //return dominantPitch;

        return 0;
    }

}
