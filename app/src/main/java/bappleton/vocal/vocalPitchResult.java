package bappleton.vocal;

import android.util.Log;

import be.tarsos.dsp.pitch.PitchDetectionResult;

/**
 * Created by Brian on 10/27/2016.
 * Important information about this class:
 *
 *      Regarding purpose:
 *          This class accepts a TarsosDSP PitchDetectionResult object
 *          It uses the PitchDetetionResult information to calculate other information pertinent to Vocal
 *          Primarily, this includes:
 *              The piano key number closest in frequency to the detected pitch
 *              The name of the piano key closest in frequeny to the detected pitch
 *              Error between the exact pitch and the closest piano key, expressed as a percent 0-100
 *          This class is the basis for an object that includes "everything we need to know" about the detected pitch.
 *          It can be passed via Message between threads using the .obj parameter.
 *
 *      Regarding frequency:
 *          We are using the twelve-tone equal tempered scale
 *          A4 is the 49th key on the piano and is assigned a frequency of 440 Hz
 *          Frequency of other notes is found based on their location relative to A4, the 49th key
 *              Given a key in location n, its frequency is: 2^((n-49)/12) * 440 Hz
 *              The lowest key we'll recognize is C0, the -8th key (it's not on a piano)
 *              The highest key we'll recognize is B8 (also not on a piano)
 *
 *
 */

public class vocalPitchResult {
    private float A4_frequency;
    private float pitchHz_f;
    private int pitchHz_i;
    private float prob;
    private float exactKeyID;
    private int   closestKeyID;
    private String noteName;
    private int errorPercent;

    vocalPitchResult() {
        A4_frequency = 440;
        pitchHz_f = 0;
        prob = 0;
        exactKeyID = 0;
        closestKeyID = 0;
        noteName = "";
        errorPercent = 0;
    }

    public void setPitchDetectionResult (PitchDetectionResult pdr) {
        pitchHz_f = pdr.getPitch();
        pitchHz_i = Math.round(pitchHz_f);
        prob = pdr.getProbability();

        calculateKeyID();
        calculateErrorClosestNote();
        calculateNoteName();
    }

    private void calculateKeyID() {
        exactKeyID = (float) (Math.log10(pitchHz_f/A4_frequency)*12/Math.log10(2) + 49);
        closestKeyID = Math.round(exactKeyID);
    }

    private void calculateErrorClosestNote() {
        errorPercent = (int)(Math.abs(exactKeyID - (float)closestKeyID)*100);
    }

    private void calculateNoteName() {
        int octave = 0;
        int octave_key = 0;
        int shiftedKeyID = closestKeyID + 8; //Shifts keys so that C0 is key 0

        octave = (shiftedKeyID - shiftedKeyID%12)/12;
        octave_key = shiftedKeyID%12;

        String[] notes = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        if (octave_key < 12 && octave_key >=0) {
            //Make sure that we're not trying to access the notes[] array at some garbage value. Added this to prevent crashes for bad note detection instances.
            noteName = notes[octave_key] + Integer.toString(octave);
        }
        else {
            noteName = "";
            Log.e("vocalPitchResult", "Bad note name. ClosestKeyID: " + closestKeyID + " Octave: " + octave + " Key: " + octave_key);
        }
    }

    public int getPitchHzInt() {
        return pitchHz_i;
    }

    public float getPitchHzFloat() {
        return pitchHz_f;
    }

    public String getNoteName() {
        return noteName;
    }

    public int getErrorPercent() {
        return errorPercent;
    }

    public int getClosestKeyID() {
        return closestKeyID;
    }

    public float getClarity() {
        return prob;
    }

    public float getExactKeyID() {
        return exactKeyID;
    }

}


