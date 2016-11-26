package bappleton.vocal;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;


import static java.lang.StrictMath.max;


/**
 * Created by Brian on 11/6/2016.
 */

public class vocalSong {

    //Primary objects. Vectors to store the song notes and lyrics
    private ArrayList<vocalSongNote> notes;
    private ArrayList<vocalLyric> lyrics;

    public vocalSong(int numNotes, int numLyrics) {
        notes  = new ArrayList<vocalSongNote>(numNotes);
        lyrics = new ArrayList<vocalLyric>(numLyrics);
    }

    public vocalSong() {
        notes  = new ArrayList<vocalSongNote>(0);
        lyrics = new ArrayList<vocalLyric>(0);
    }

    public vocalSong(ArrayList<vocalSongNote> songNotes, ArrayList<vocalLyric> songLyrics) {
        this.notes  = songNotes;
        this.lyrics = songLyrics;
    }

    //Note: this function requires, for efficiency, that notes are stored in sequential order
    public ArrayList<vocalSongNote> getNotesInWindow (float startTime_ms, float endTime_ms) {

        //Declare the vector of notes that we're going to return, as well as a buffer "nextNote"
        ArrayList<vocalSongNote> selectedNotes = new ArrayList<vocalSongNote>();
        //vocalSongNote nextNote;

        //Get an iterator to the private object notes
        //Iterator<vocalSongNote> itr = notes.iterator();

        //Loop through notes and pick out the notes that fall in time between startTime_ms and endTime_ms
        for(vocalSongNote nextNote : notes) {
            //nextNote = itr.next();
            if (nextNote.startTime_s*1000+nextNote.duration_ms > startTime_ms && nextNote.startTime_s*1000 < endTime_ms ) {
                //we're within the requested window, add this element to the return array
                selectedNotes.add(nextNote);
                //Log.i("vocalSong", "Selected note starting at " + nextNote.startTime_s + " seconds and ending at " + (nextNote.startTime_s+nextNote.duration_ms/1000) + " seconds");
            }
            else if (nextNote.startTime_s*1000 > endTime_ms) {
                //For efficiency, let's require that the notes have been stored with their start times in chronological order.
                //No need to keep searching the vector after we've found an element that falls outside of the requested vector
                break;
            }
        }

        return selectedNotes;
    }

    //Note: this function requires, for efficiency, that notes are stored in sequential order
    public int getCurrentNote (float currentTime_ms) {
        //Loop through the notes in the song and pick out the one that the user should be singing at time currentTime_ms
        for (vocalSongNote nextNote : notes) {
            if (currentTime_ms >= nextNote.startTime_s*1000 && currentTime_ms <= nextNote.startTime_s*1000+nextNote.duration_ms) {
                //If we've found the note that's playing at currentTime_ms
                return nextNote.pianoKeyID;
            }
            else if (nextNote.startTime_s*1000 > currentTime_ms) {
                //If we've found a note that starts beyond the start of currentTime_ms, stop searching.
                //Calling function needs to handle this case. No note should be sung right now.
                return -1;
            }
        }
        //No note should be sung right now. Calling function needs to handle this case.
        return -1;
    }

    //Note: this function requires, for efficiency, that notes are stored in sequential order
    public ArrayList<vocalLyric> getLyricsinWindow (float startTime_ms, float endTime_ms) {
        //Let's attack this similary to how we got the music notes in the window, except, the lyrics don't have a duration

        //Declare arrayList that we'll return
        ArrayList<vocalLyric> selectedLyrics = new ArrayList<vocalLyric>();

        //Loop through lyrics and pick out the ones between the start and end times
        for (vocalLyric nextLyric : lyrics) {
            if(nextLyric.startTime_s*1000 > startTime_ms && nextLyric.startTime_s*1000 < endTime_ms) {
                selectedLyrics.add(nextLyric);
            }
            else if (nextLyric.startTime_s*1000 > endTime_ms) {
                //If the next lyric in the lyrics array is beyond the end time, stop searching. Requires sorted ArrayList.
                break;
            }
        }

        return selectedLyrics;
    }


    public float getSongLength_s() {

        float duration;

        try {
            vocalSongNote lastNote = notes.get(notes.size()-1);
            vocalLyric lastLyric = lyrics.get(notes.size()-1);

            duration = max(lastNote.startTime_s+lastNote.duration_ms/1000, lastLyric.startTime_s);

        }
        catch(IndexOutOfBoundsException e) {
            Log.e("vocalSong", "Error occurred retreiving last element of song. Is it empty?");
            return 0;
        }

        return duration;
    }

    public int getNumNotes() {
        return notes.size();
    }

    public int getNumLyrics() {
        return lyrics.size();
    }

    ///Additional functions to be defined as needed


}
