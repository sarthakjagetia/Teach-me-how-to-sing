package bappleton.dft_test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Arrays;



public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 22;
    boolean PERMISSIONS_RECORD_AUDIO = false;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //define FFT size (must be a power of 2)
        int N = 512;
        //define sampling rate
        int Fs = 1000;

        FFT fft_test = new FFT(N);
        Log.i("hey!", "sup!");
        double[] window = fft_test.getWindow();

        //create arrays to contain the real and imaginary parts of the signal
        double[] re = new double[N];
        double[] signal = new double[N]; //store a copy of the signal
        double[] im = new double[N];
        //create array to store the time component of the signal
        double[] time = new double[N];

        //Generate a signal on which to perform an FFT
        // create time array
        for(int i=0; i<N; i++){
            time[i] = ((double)i)/((double)Fs);
        }
        Log.i("fft-time", Arrays.toString(time));
        //generate signal
        int f = 100; //frequency of signal, in hertz
        double T = 1/((double)f); //period of signal, in seconds
        for(int i=0; i<N; i++) {
            re[i] = Math.cos(2*Math.PI*time[i]/T);
            signal[i] = Math.cos(2*Math.PI*time[i]/T);
            im[i] = 0;
        }
        Log.i("fft-re-pre", Arrays.toString(re));
        fft_test.fft(re, im);
        Log.i("fft-re-post", Arrays.toString(re));
        //compute power spectrum (|A|^2)
        double[] power = new double[N];
        for (int i=0; i<N; i++) {
            power[i]=Math.pow(re[i],2)+Math.pow(im[i],2);
        }

        //compute frequency axis for FFT plot
        double[] frequency = new double[N];
        for (int i=0; i<N; i++){
            frequency[i]=i*Fs/N;
        }

        //Print arrays to console (must run in debug mode)
        Log.i("fft-pow", Arrays.toString(power));
        Log.i("fft-freq", Arrays.toString(frequency));

        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();

        //store original signal in series variable
//        for (int i=0; i<N; i++) {
//            series.appendData(new DataPoint(time[i], signal[i]), true, N);
//        }
        //store FFT in series variable
        for (int i=0; i<N/2; i++) {
            series.appendData(new DataPoint(frequency[i], power[i]), true, N/2);
        }
        graph.addSeries(series);

        //AUDIORECORD TESTING

        //Check and configure permissions
        checkPermissions();
        if (PERMISSIONS_RECORD_AUDIO) {
            Log.i("RECORD_AUDIO", "Permissions are good, continuing execution.");
        }

        //Try to open an AudioRecord instance and get a sample
        getSample();
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
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
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

    protected void getSample() {
        //confgure AudioRecord paramters
        int audioSource = MediaRecorder.AudioSource.DEFAULT; //MediaRecorder.AudioSource.UNPROCESSED requires API24
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                //see documentation for other options: ENCODING_PCM_FLOAT, ENCODING_PCM_8BIT
                //NEEDS to mesh with read() call and bytesPerElement
        int bufferElements = 1024; //number of array elements to fetch from AudioRecord
        int bytesPerElement = 2; //when reading into a short, each element consumes 2 bytes
        int bufferSizeInBytes = bufferElements * bytesPerElement;

        int MBS = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig, audioFormat);
        if (bufferSizeInBytes < MBS) {
            Log.e("AudioRecord", "Requested buffer size is insufficiently large");
        }

        Log.i("AR_MinBuffSize", Integer.toString(MBS));
        Log.i("AR_ReqestedBuffSize",Integer.toString(bufferSizeInBytes));


        AudioRecord AR1;
        AR1 = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

        //check whether the audioRecord instance was properly initialized, print result to console
        if (AR1.getState() == AudioRecord.STATE_INITIALIZED){
            Log.i("AudioRecord", "Successfully initialized");
        }
        else if (AR1.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e("AudioRecord", "Not successfully initialized");
        }
        else {
            Log.e("AudioRecord", "Unrecognized state");
        }

        //create array of shorts to hold data. In java this is a 16-bit signed integer.
        short recorder_data[] = new short[bufferElements];

        //can check actual sample rate using
        //int actual_sample_rate = AudioRecord.getSampleRate();
        AR1.startRecording();

        int AR1_result;
        AR1_result = AR1.read(recorder_data, 0, bufferElements);
        if (AR1_result == bufferElements || AR1_result == 0){
            //read completed without error
            Log.i("AudioRecord", "Read successful");
        }
        else {
            Log.e("AudioRecord", "Read error");
        }

        AR1.stop(); //stops recording
        AR1.release(); //releases native resources; set AR1 to null after this call
        AR1 = null;

        //see if we got something
        Log.i("AudioRecordData", Arrays.toString(recorder_data));

    }
}
