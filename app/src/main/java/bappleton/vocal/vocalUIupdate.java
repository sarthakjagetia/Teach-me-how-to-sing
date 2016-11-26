package bappleton.vocal;

import android.util.Log;

/**
 * Created by Brian on 11/26/2016.
 */

public class vocalUIupdate {
    private int seconds_elapsed;
    private int seconds_remaining;
    private float seconds_duration;
    private int score;

    private final String TAG = "vocalUIupdate";


    vocalUIupdate() {
        seconds_elapsed = 0;
        seconds_remaining = 0;
    }

    public String getTimeElapsed() {
        int seconds = this.seconds_elapsed%60;
        int minutes = (int)((this.seconds_elapsed-seconds)/60);

        String display_string = "";
        display_string = Integer.toString(seconds);
        if (display_string.length() == 1) {
            display_string = '0' + display_string;
        }
        display_string = Integer.toString(minutes) + ':' + display_string;

        return display_string;
    }

    public String getTimeRemaining() {
        int seconds = this.seconds_remaining%60;
        int minutes = (int)((this.seconds_remaining-seconds)/60);

        String display_string = "";
        display_string = Integer.toString(seconds);
        if (display_string.length() == 1) {
            display_string = '0' + display_string;
        }
        display_string = Integer.toString(minutes) + ':' + display_string;

        return display_string;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getScore() {
        return "Your score: " + Integer.toString(this.score) + "%";
    }

    public int getPercentComplete() {
        float percent = (float)seconds_elapsed/seconds_duration;
        return Math.round(percent*100);
    }

    public void setTimeStatus(float milliSecondsElapsed, float secondsDuration) {
        this.seconds_elapsed = Math.round(milliSecondsElapsed/1000);
        this.seconds_remaining = Math.round(secondsDuration-milliSecondsElapsed/1000);
        this.seconds_duration = secondsDuration;
    }

}
