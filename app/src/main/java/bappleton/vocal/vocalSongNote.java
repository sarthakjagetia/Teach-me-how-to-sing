package bappleton.vocal;

/**
 * Created by Brian on 11/6/2016.
 */

public class vocalSongNote {

    //Integer corresponding to the piano key of this note
    public int pianoKeyID;

    //Start time of the note in the song, in seconds
    public float startTime_s;

    //Duration of the note in the song, in milliseconds
    public float duration_ms;

    public vocalSongNote() {
        pianoKeyID  = 49;
        startTime_s = 0;
        duration_ms  = 0;
    }

    public vocalSongNote(int pianoKeyID, float startTime_s, float duration_ms) {
        this.pianoKeyID = pianoKeyID;
        this.startTime_s = startTime_s;
        this.duration_ms = duration_ms;
    }

    //Constructor to assist deep copying
    public vocalSongNote(vocalSongNote makeCopy) {
        this.pianoKeyID = makeCopy.pianoKeyID;
        this.startTime_s = makeCopy.startTime_s;
        this.duration_ms = makeCopy.duration_ms;
    }

}
