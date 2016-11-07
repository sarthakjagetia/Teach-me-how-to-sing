package bappleton.vocal;

import android.util.Log;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Brian on 11/6/2016.
 */

public class vocalSong {

    //Primary objects. Vectors to store the song notes and lyrics
    private Vector<vocalSongNote> notes;
    private Vector<vocalLyric> lyrics;

    public vocalSong(int numNotes, int numLyrics) {
        notes  = new Vector<vocalSongNote>(numNotes);
        lyrics = new Vector<vocalLyric>(numLyrics);
    }

    public vocalSong(Vector<vocalSongNote> songNotes, Vector<vocalLyric> songLyrics) {
        this.notes  = songNotes;
        this.lyrics = songLyrics;
    }

    public Vector<vocalSongNote> getNotesInWindow (float startTime_ms, float endTime_ms) {
        Vector<vocalSongNote> selectedNotes = new Vector<vocalSongNote>(0);
        vocalSongNote nextNote;

        Iterator<vocalSongNote> itr = notes.iterator();

        while(itr.hasNext()) {
            nextNote = itr.next();
            if (nextNote.startTime_s*1000+nextNote.duration_ms > startTime_ms && nextNote.startTime_s < endTime_ms ) {
                //we're within the requested window, add this element to the return array
                selectedNotes.addElement(nextNote);
            }
            else {
                //For efficiency, let's require that the notes have been stored with their start times in chronological order.
                //No need to keep searching the vector after we've found an element that falls outside of the requested vector
                break;
            }
        }

        return selectedNotes;
    }

    ///Additional functions to be defined as needed


}
