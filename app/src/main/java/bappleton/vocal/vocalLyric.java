package bappleton.vocal;

/**
 * Created by Brian on 11/6/2016.
 */

public class vocalLyric {

    //Lyrics (word or words)
    public String lyric;

    //Start time, seconds
    public float startTime;

    public vocalLyric() {
        lyric = "";
        startTime = 0;
    }

    public vocalLyric(String lyric, float startTime_s) {
        this.lyric = lyric;
        this.startTime = startTime_s;
    }

}
