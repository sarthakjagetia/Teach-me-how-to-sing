package bappleton.vocal;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Process;
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

    //Duration of song in seconds. Calculated inside constructor.
    private float duration;

    //Playback related variables
    private String audioPath;
    private boolean audioPlaybackEnabled;

    private final String TAG = "vocalSong";

    /*
    public vocalSong(int numNotes, int numLyrics) {
        notes  = new ArrayList<vocalSongNote>(numNotes);
        lyrics = new ArrayList<vocalLyric>(numLyrics);
        audioPath = "";
        audioPlaybackEnabled = false;
    }

    public vocalSong() {
        notes  = new ArrayList<vocalSongNote>(0);
        lyrics = new ArrayList<vocalLyric>(0);
        audioPath = "";
        audioPlaybackEnabled = false;
    }
    */

    public vocalSong(ArrayList<vocalSongNote> songNotes, ArrayList<vocalLyric> songLyrics) {
        this.notes  = songNotes;
        this.lyrics = songLyrics;
        audioPath = "";
        audioPlaybackEnabled = false;
        calculateDuration();
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
    public vocalSongNote getCurrentNote (float currentTime_ms) {
        //Loop through the notes in the song and pick out the one that the user should be singing at time currentTime_ms
        for (vocalSongNote nextNote : notes) {
            if (currentTime_ms >= nextNote.startTime_s*1000 && currentTime_ms <= nextNote.startTime_s*1000+nextNote.duration_ms) {
                //If we've found the note that's playing at currentTime_ms
                //return nextNote.pianoKeyID;
                //TESTING: Return actual vocalSongNote instead of the integer pianoKeyID. Because it's passed by reference, instead of a
                //  as a copy, the calling function is able to modfiy the object. This is helpful for setting nextNote.pitchMatchedKeyID.
                return nextNote;
            }
            else if (nextNote.startTime_s*1000 > currentTime_ms) {
                //If we've found a note that starts beyond the start of currentTime_ms, stop searching.
                //Calling function needs to handle this case. No note should be sung right now.
                //return -1;
                return new vocalSongNote(-1,0,0);
            }
        }
        //No note should be sung right now. Calling function needs to handle this case.
        //return -1;
        return new vocalSongNote(-1,0,0);
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

    private void calculateDuration() {
        try {
            vocalSongNote lastNote = notes.get(notes.size()-1);
            vocalLyric lastLyric = lyrics.get(notes.size()-1);

            duration = max(lastNote.startTime_s+lastNote.duration_ms/1000, lastLyric.startTime_s);

        }
        catch(IndexOutOfBoundsException e) {
            Log.e("vocalSong", "Error occurred retreiving last element of song. Is it empty?");
        }
    }

    public float getSongLength_s() {
        /*
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
        */
        return duration;
    }

    public boolean isSongOver(float currentTime_ms) {
        if (currentTime_ms > duration*1000) {
            return true;
        }
        else {
            return false;
        }
    }

    public int getNumNotes() {
        return notes.size();
    }

    public int getNumLyrics() {
        return lyrics.size();
    }

    public int getFinalScore() {
        int noteCount = 0;
        for (vocalSongNote nextNote : notes) {
            if (nextNote.pitchMatchedKeyID) {
                noteCount++;
            }
        }

        return Math.round((float)noteCount/(float)getNumNotes()*100);
    }


    //SANDBOX AREA. AUDIO PLAYBACK TESTING/DEV.

    public void setAudioPath(String path, boolean audioPlaybackEnabled) {
        this.audioPath = path;
        this.audioPlaybackEnabled = audioPlaybackEnabled;
    }

    public void setAudioPlayback(boolean audioPlaybackEnabled) {
        this.audioPlaybackEnabled = audioPlaybackEnabled;
    }

    public boolean isAudioPlaybackEnabled() {
        return this.audioPlaybackEnabled;
    }


    public void playAudio() {
        if (isAudioPlaybackEnabled()) {
            playbackThread playSong = new playbackThread(this.audioPath);
            playSong.start();
        }
        else {
            Log.w(TAG, "Cannot play. Audio playback is disabled");
        }
    }

    private class playbackThread extends Thread {

        public MediaPlayer mp;
        private String audioPath;

        public playbackThread(String audioPath) {
            this.audioPath = audioPath;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.myTid(), Process.THREAD_PRIORITY_AUDIO);
            try {
                mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource("https://s3.amazonaws.com/vocal-contentdelivery-mobilehub-1874297389/Good+Friday+(feat.+Common%2C+Pusha+T%2C.mp3");
                Log.i(TAG, "Playback source set.");
                mp.prepare();
                Log.i(TAG, "Preparation complete.");
                mp.setOnBufferingUpdateListener(
                        new MediaPlayer.OnBufferingUpdateListener() {
                            @Override
                            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                                Log.i(TAG, "Buffering percent: " + percent);
                                if (percent == 100 && !mp.isPlaying()) {
                                    mp.start();
                                }
                            }
                        }
                );
                //song.start();
            }
            catch (Exception e) {

            }
        }

    }
}

//END SANDBOX
