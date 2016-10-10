package bappleton.dft_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

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

    }
}
