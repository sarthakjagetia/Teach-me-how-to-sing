package bappleton.vocal;

import android.util.Log;

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

        notes.add(new vocalSongNote(40,3,2000));
        notes.add(new vocalSongNote(42,5,2000));
        notes.add(new vocalSongNote(44,7,2000));
        notes.add(new vocalSongNote(45,9,2000));
        notes.add(new vocalSongNote(47,11,2000));
        notes.add(new vocalSongNote(49,13,2000));
        notes.add(new vocalSongNote(51,15,2000));
        notes.add(new vocalSongNote(52,17,2000));

        Log.i("vocalUI", "Notes vector contains: " + notes.size() + " elements.");

        ArrayList<vocalLyric> lyrics = new ArrayList<vocalLyric>(0);

        lyrics.add(new vocalLyric("Do", 3));
        lyrics.add(new vocalLyric("Re", 5));
        lyrics.add(new vocalLyric("Me", 7));
        lyrics.add(new vocalLyric("Fa", 9));
        lyrics.add(new vocalLyric("So", 11));
        lyrics.add(new vocalLyric("La", 13));
        lyrics.add(new vocalLyric("Ti", 15));
        lyrics.add(new vocalLyric("Do", 17));

        Log.i("vocalUI", "Lyrics vector contains: " + lyrics.size() + " elements.");

        return new vocalSong(notes, lyrics, "Vocal Beginner Series", "Exercise 1");

    }
}
