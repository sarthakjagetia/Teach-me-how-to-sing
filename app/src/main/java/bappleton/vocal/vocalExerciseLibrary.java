package bappleton.vocal;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Brian on 11/26/2016.
 */

public class vocalExerciseLibrary {

    public vocalExerciseLibrary() {

    }

    public vocalSong Exercise1_DoReMe() {
        //Do re me fa so la ti do

        ArrayList<vocalSongNote> notes = new ArrayList<vocalSongNote>();

        notes.add(new vocalSongNote(40,3,1000));
        notes.add(new vocalSongNote(42,4,1000));
        notes.add(new vocalSongNote(44,5,1000));
        notes.add(new vocalSongNote(45,6,1000));
        notes.add(new vocalSongNote(47,7,1000));
        notes.add(new vocalSongNote(49,8,1000));
        notes.add(new vocalSongNote(51,9,1000));
        notes.add(new vocalSongNote(52,10,1000));

        Log.i("vocalUI", "Notes vector contains: " + notes.size() + " elements.");

        ArrayList<vocalLyric> lyrics = new ArrayList<vocalLyric>(0);

        lyrics.add(new vocalLyric("Do", 3));
        lyrics.add(new vocalLyric("Re", 4));
        lyrics.add(new vocalLyric("Me", 5));
        lyrics.add(new vocalLyric("Fa", 6));
        lyrics.add(new vocalLyric("So", 7));
        lyrics.add(new vocalLyric("La", 8));
        lyrics.add(new vocalLyric("Ti", 9));
        lyrics.add(new vocalLyric("Do", 10));

        Log.i("vocalUI", "Lyrics vector contains: " + lyrics.size() + " elements.");

        vocalSong DoReMe = new vocalSong(notes, lyrics, "Vocal Beginner Series", "Exercise 1");
        DoReMe.setAudioPath("https://s3.amazonaws.com/vocal-contentdelivery-mobilehub-1874297389/do_re_me.mp3", true);
        return DoReMe;


    }

    public vocalSong Song1_WeWishYou() {
        ArrayList<vocalSongNote> notes = new ArrayList<vocalSongNote>();
        ArrayList<vocalLyric> lyrics = new ArrayList<vocalLyric>(0);

        //We wish you a merry christmas
        String words[] = {"We","wish","you a","","Merry","","Christ-","mas,","We ","wish","you a","","Merry","","Christ-","mas,","We","wish","you a","","Merry","","Christ-"," mas","And a","","hap-","py","New","Year", "We","wish","you a","","Merry","","Christ-"," mas,","We ","wish","you a","","Merry","","Christ-","mas,","We","wish","you a","","Merry","","Christ-","mas","And a","","hap-","py","New","Year", "Glad","tid-","ings","we","bring","To","you","and","your","kin","Glad","tid-","ings","for","Christ-","mas,","And a","","hap-","py","New","Year!"};
        double start_times[] = {2.236024845,2.608695652,2.98136646,3.167701863,3.354037267,3.540372671,3.726708075,4.099378882,4.472049689,4.844720497,5.217391304,5.403726708,5.590062112,5.776397516,5.962732919,6.335403727,6.708074534,7.080745342,7.453416149,7.639751553,7.826086957,8.01242236,8.198757764,8.571428571,8.944099379,9.130434783,9.316770186,9.689440994,10.0621118,10.43478261,11.18012422,11.55279503,11.92546584,12.11180124,12.29813665,12.48447205,12.67080745,13.04347826,13.41614907,13.78881988,14.16149068,14.34782609,14.53416149,14.72049689,14.9068323,15.27950311,15.65217391,16.02484472,16.39751553,16.58385093,16.77018634,16.95652174,17.14285714,17.51552795,17.88819876,18.07453416,18.26086957,18.63354037,19.00621118,19.37888199,20.1242236,20.49689441,20.86956522,21.24223602,21.61490683,22.36024845,22.73291925,23.10559006,23.47826087,23.85093168,24.59627329,24.9689441,25.34161491,25.71428571,26.08695652,26.45962733,26.83229814,27.01863354,27.20496894,27.57763975,27.95031056,28.32298137};
        double durations[] = {372.6708075,372.6708075,186.3354037,186.3354037,186.3354037,186.3354037,372.6708075,372.6708075,372.6708075,372.6708075,186.3354037,186.3354037,186.3354037,186.3354037,372.6708075,372.6708075,372.6708075,372.6708075,186.3354037,186.3354037,186.3354037,186.3354037,372.6708075,372.6708075,186.3354037,186.3354037,372.6708075,372.6708075,372.6708075,745.3416149,372.6708075,372.6708075,186.3354037,186.3354037,186.3354037,186.3354037,372.6708075,372.6708075,372.6708075,372.6708075,186.3354037,186.3354037,186.3354037,186.3354037,372.6708075,372.6708075,372.6708075,372.6708075,186.3354037,186.3354037,186.3354037,186.3354037,372.6708075,372.6708075,186.3354037,186.3354037,372.6708075,372.6708075,372.6708075,745.3416149,372.6708075,372.6708075,372.6708075,372.6708075,745.3416149,372.6708075,372.6708075,372.6708075,372.6708075,745.3416149,372.6708075,372.6708075,372.6708075,372.6708075,372.6708075,372.6708075,186.3354037,186.3354037,372.6708075,372.6708075,372.6708075,800};
        int keys[] = {40,45,45,47,45,43,42,39,42,47,47,49,47,45,44,40,40,49,49,51,49,47,45,42,40,40,42,47,44,45,40,45,45,47,45,43,42,39,42,47,47,49,47,45,44,40,40,49,49,51,49,47,45,42,40,40,42,47,44,45,40,45,45,45,44,44,45,44,42,40,40,49,47,45,52,40,40,40,42,47,44,45};

        //This is not a very safe iteration. MAKE SURE that the number of elements in all of the arrays are the same.
        for (int i = 0; i < Array.getLength(start_times); i++ ) {
            notes.add(new vocalSongNote(keys[i], (float)start_times[i], (float)durations[i]));
            lyrics.add(new vocalLyric(words[i], (float)start_times[i]));
        }

        vocalSong WeWishYou = new vocalSong(notes, lyrics, "Traditional Carol", "We Wish You a Merry Christmas");
        WeWishYou.setAudioPath("https://s3.amazonaws.com/vocal-contentdelivery-mobilehub-1874297389/Christmas_Carol_We_Wish_You_A_Merry_Christmas(Instrumental_Version_(Without_backing_vocals))_62.mp3", true);

        return  WeWishYou;
    }
}
