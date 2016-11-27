package bappleton.vocal;

/**
 * Created by Brian on 11/6/2016.
 */

public class vocalLyric {

    //Lyrics (word or words)
    public String lyric;

    //Start time, seconds
    public float startTime_s;

    public vocalLyric() {
        lyric = "";
        startTime_s = 0;
    }

    public vocalLyric(String lyric, float startTime_s) {
        this.lyric = lyric;
        this.startTime_s = startTime_s;
    }

    //Constructor to assist deep copying
    public vocalLyric (vocalLyric makeCopy) {
        this.lyric = makeCopy.lyric;
        this.startTime_s = makeCopy.startTime_s;
    }

}
