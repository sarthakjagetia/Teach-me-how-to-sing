package bappleton.vocal;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Process;
import android.os.SystemClock;
import android.provider.MediaStore;
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

    //Keeps track of when we started playing the song (insensitive to audio playback)
    private long startTime_ms;
    private boolean isSongPlaying;
    private long finishDelay_ms;

    //Audio playback related variables
    private String audioPath;
    private boolean audioPlaybackEnabled;
    private boolean audioCurrentlyPlaying;

    //General song information
    public String artist;
    public String trackName;

    private MediaPlayer mp;

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

    public vocalSong(ArrayList<vocalSongNote> songNotes, ArrayList<vocalLyric> songLyrics, String artist, String trackName) {
        this.notes  = songNotes;
        this.lyrics = songLyrics;
        this.artist = artist;
        this.trackName = trackName;
        this.finishDelay_ms = 0;

        audioPath = "";
        audioPlaybackEnabled = false;
        audioCurrentlyPlaying = false;
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

    @Deprecated
    public boolean isSongOver(float elapsedTime_ms) {
        //Report on whether the song has finished, strictly from a rendering perspective.
        //Insensitive to what the audio is doing
        if (elapsedTime_ms > duration*1000) {
            return true;
        }
        else {
            return false;
        }
    }

    public void setFinishDelay_ms(long delay_ms){
        this.finishDelay_ms = delay_ms;
    }

    public boolean isSongOver() {
        //Report on whether the song has finished, strictly from a rendering perspective.
        //Insensitive to what the audio is doing

        if(isSongPlaying) {
            long elapsedTime_ms = SystemClock.uptimeMillis()-startTime_ms;
            if (elapsedTime_ms > duration * 1000 + finishDelay_ms) {
                //If we find that the song is over
                isSongPlaying = false;
                return true;
            } else {
                return false;
            }
        }
        else {
            return true;
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


    public long getElapsedTime_ms() {
        //This function tells us how far we are into the song, relative to our call to play().

        if(isSongOver()) {
            Log.w(TAG, "Elapsed time was reuqested for a song that is not playing");
        }

        //If we're trying to synchronize to a currently-playing song, we can directly query the song
        if(audioPlaybackEnabled && audioCurrentlyPlaying) {
            return mp.getCurrentPosition();
        }

        //Otherwise, let's just report the duration relative to startTime_ms
        else {
            return SystemClock.uptimeMillis()-startTime_ms;
        }
    }

    public void stop() {
        if (audioCurrentlyPlaying) {
            mp.stop();
            audioCurrentlyPlaying = false;
        }
        if (mp != null) {
            mp.release();
        }
        isSongPlaying = false;
    }

    public void play() {
        //Records the start time of the song
        //Begins audio playback if it is enabled
        if (audioPlaybackEnabled && !audioCurrentlyPlaying) {
            try {
                mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(this.audioPath);
                Log.i(TAG, "Playback source set.");
                mp.prepare();
                Log.i(TAG, "Preparation complete.");
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        audioCurrentlyPlaying = false;
                    }
                });
                mp.start();
                audioCurrentlyPlaying = true;
            }
            catch (Exception e) {
                Log.e(TAG, "Error beginning audio playback. Disabling playback.");
                audioPlaybackEnabled = false;
            }
        }
        startTime_ms = SystemClock.uptimeMillis();
        isSongPlaying = true;
    }

    /*
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
                mp.setDataSource(this.audioPath);
                Log.i(TAG, "Playback source set.");
                mp.prepare();
                Log.i(TAG, "Preparation complete.");
                mp.setOnBufferingUpdateListener(
                        new MediaPlayer.OnBufferingUpdateListener() {
                            @Override
                            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                                Log.i(TAG, "Buffering percent: " + percent);
                                if (percent == 100 && !mp.isPlaying()) {
                                    //mp.start();
                                }
                            }
                        }
                );
                mp.start();
            }
            catch (Exception e) {

            }
            //Try waiting until the media player is done
            while(mp.isPlaying()) {}
        }

    } */
}

//END SANDBOX
