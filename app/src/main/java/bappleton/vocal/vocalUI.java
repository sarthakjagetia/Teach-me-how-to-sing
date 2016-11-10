package bappleton.vocal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Process;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import static android.graphics.Bitmap.createBitmap;


/**
 * Created by Brian on 11/3/2016.
 * Following this tutorial: http://obviam.net/index.php/a-very-basic-the-game-loop-for-android/
 */

public class vocalUI extends SurfaceView implements
    SurfaceHolder.Callback {

    //Child thread for rendering graphics
    private MainThread thread;

    //Height and width of graphics canvas
    private int height, width;

    //Currently selected song. This contains time info, notes, and lyrics;
    private vocalSong currentSong;

    //System time at beginning of song
    private long startTime_ms;

    //Is song playing?
    private boolean isSongPlaying;


    /*
    Rendering variables.
    See configureDisplayConstants() for more information.
    */
    float lineSpacing;
    float strokeWidth;
    float bottomLineOffset;
    float xLeftPadding;
    float xRightPadding;
    Paint linePaint;
    Paint noteTextPaint;
    Rect textBounds;
    private float timeWindow_ms;
    private float timeWindowRenderPastNow;
    private float noteDisplayWidth_pixels;
    private float pixelsPerMillisecond;
    private float xPosNow;
    private float noteWidth;
    private Paint noteRectPaint;
    private String[] notes = {"C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4", "C5"};
    private  float[] note_centers;
    private float yPosNowBottom;
    private float yPosNowTop;
    private float left_time_bound_ms;
    private float right_time_bound_ms;
    private float left_pixel_bound;
    private float right_pixel_bound;
    /*
    End rendering variables
     */

    //This gets called if doing:
    //setContentView(new vocalUI(this));
    //in main onCreate method. See other constructor definitions.
    public vocalUI (Context context) {
        super(context);
        init(context);
    }

    //This needs to be defined in order for incorporation of this class into XML layout to work
    //This gets called if launching view from XML file, with no defstyle
    public vocalUI (Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //This gets called if view-specfic defStyle is used. Not sure what that is.
    public vocalUI (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    //Called by ALL constructors defined above.
    private void init(Context context) {
        //add callback to surface holder in order to intercept events
        getHolder().addCallback(this);

        //create our dedicated thread. Give it a surfaceHolder and myself
        thread = new MainThread(getHolder(), this);

        //make the UI focusable so that it can handle events
        setFocusable(true);

        //initialize height and width
        height = 0;
        width  = 0;

        //initialize start time
        startTime_ms = 0;

        isSongPlaying = false;

    }

    //TO BE DELETED, for pre-integration testing only
    private void AdditionalTestingInit() {
        this.setSong(demoSong1());


        //this.beginSong();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("vocalUI", "SurfaceChanged call. Height: " + height + " width: " + width);
        this.height = height;
        this.width  = width;
        configureDisplayConstants();

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //start our thread
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //try to cleanly shut down the thread
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                //if that worked, don't retry
                retry = false;
            } catch (InterruptedException e) {
                //e.printStackTrace();
                //if that didn't work, try again
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    public int getCanvasHeight() {
        return height;
    }

    public int getCanvasWidth() {
        return width;
    }

    public void setSong(vocalSong song) {
        this.currentSong = song;
        Log.i("vocalUI", "New song selected. Song length is " + this.currentSong.getSongLength_s() + " seconds. Contains " + this.currentSong.getNumNotes() + " music notes and " + this.currentSong.getNumLyrics() + " lyrics.");
    }

    private vocalSong demoSong1() {
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

        return new vocalSong(notes, lyrics);

    }

    public void beginSong() {
        //Capture start time and begin UI rendering
        //thread.setRunning(true);

        if(currentSong.getNumNotes() !=0 ){
            startTime_ms = SystemClock.uptimeMillis();
            isSongPlaying = true;
            Log.i("vocalUI", "Song started. Start time is: " + startTime_ms);
        }
        else {
            startTime_ms = 0;
            isSongPlaying = false;
            Log.e("vocalUI", "Cannot begin song. Number of notes in selected song is zero.");
        }

    }

    public void endSong() {
        //thread.setRunning(false);
        startTime_ms = 0;
        isSongPlaying = false;
    }


    private void configureDisplayConstants() {

        //The spacing between the horizontal music note lines
        lineSpacing = (float)Math.round(0.06*height); //50;

        //Music staff linewidth. 1 gives a hairline.
        strokeWidth = 1;

        //Offset between the bottom music note line and the bottom of the canvas
        bottomLineOffset = (float)Math.round(0.08*height); //20;

        //Offset between the left bound of the music note line and the canvas. This will be adjusted automatically if the note name won't fit.
        xLeftPadding = (float)Math.round(0.02*height);//20;

        //Offset between the right bound of the music note line and the canvas
        xRightPadding = 0;

        //Paint configuration for music note lines
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(strokeWidth);

        //Paint configuration for music note names
        //Presently these scale as a function of canvas width
        noteTextPaint = new Paint();
        noteTextPaint.setColor(Color.GRAY);
        noteTextPaint.setTextSize((float)Math.round(0.04*width));  //(30);

        //Adjust xLeftPadding if the longest note name doesn't fit
        textBounds = new Rect();
        noteTextPaint.getTextBounds("C#4", 0, 3, textBounds);
        if (textBounds.width() > xLeftPadding) {
            xLeftPadding = textBounds.width() + 5;
        }

        //Left bound of music note display, in pixels
        left_pixel_bound = 0 + xLeftPadding;

        //Right bound of music note display, in pixels
        right_pixel_bound = width - xRightPadding;

        //Width, in pixels, of the music note display region
        noteDisplayWidth_pixels = width - xLeftPadding - xRightPadding;

        //Width, in time, of the music note display region
        timeWindow_ms = 3000;

        //Some notes will be rendered in the past, and indicated with a vertical line. How far into the past?
        //This will eat up a chunk of the time allocated to timeWindow_ms.
        timeWindowRenderPastNow = 300;

        //Conversion factor between pixel width and time width of music note display
        pixelsPerMillisecond = noteDisplayWidth_pixels / timeWindow_ms;

        //X-position of the vertical line indicating "now"
        xPosNow = left_pixel_bound + timeWindowRenderPastNow*pixelsPerMillisecond;

        //Width in y-direction of the music note rectangles
        noteWidth = (float) 0.75 * lineSpacing;

        //Paint configuration for music note rectangles
        noteRectPaint = new Paint();
        noteRectPaint.setColor(Color.BLUE);

        //Calculate array that holds the y-positions of the 13 music note lines. 0 corresponds to the bottom line.
        note_centers = new float[13];
        for (int i = 0; i < 13; i++) {
            note_centers[i] = height - (bottomLineOffset + i * lineSpacing);
        }

        //Set the y-limits of the vertical line for "Now"
        yPosNowBottom = note_centers[0];
        yPosNowTop = note_centers[12];
    }


    private void drawMusicStaff(Canvas canvas) {

        /*
        Draw the staff lines, note names, and a vertical line indicating "now"
         */

        //13 staff lines and note names
        for (int i = 0; i < 13; i++) {
            canvas.drawLine(0 + xLeftPadding, note_centers[i], width - xRightPadding, note_centers[i], linePaint);
            canvas.drawText(notes[i], 0, note_centers[i] + textBounds.height() / 2, noteTextPaint);
        }

        //vertical line to indicate current position in song
        canvas.drawLine(xPosNow, yPosNowBottom, xPosNow, yPosNowTop, linePaint);


        /*
        Draw the music notes
         */

        ArrayList<vocalSongNote> songNotesInWindow;

        //if a song is playing, let's draw some notes
        if (isSongPlaying) {

            //get time bounds, in terms of location in the song, for the display
            left_time_bound_ms = (float) (SystemClock.uptimeMillis() - startTime_ms - timeWindowRenderPastNow);
            right_time_bound_ms = left_time_bound_ms + timeWindow_ms;

            //get a vector of the song notes that are within the time bounds
            songNotesInWindow = currentSong.getNotesInWindow(left_time_bound_ms, right_time_bound_ms);

            //Try to make a deep copy of what we got back. !!!NOTE!!! This resolved a concurrent modification exeption casued by iterating directly through songNotesInWindow
            ArrayList<vocalSongNote> songNotesCopy = new ArrayList<vocalSongNote>(songNotesInWindow.size());
            for (vocalSongNote item : songNotesInWindow) songNotesCopy.add(new vocalSongNote(item));

            if (!songNotesCopy.isEmpty()) {

                //rectangle to draw the note. Everything here is in pixels
                Rect thisNoteRect = new Rect();
                int top, left, right, bottom;

                for (vocalSongNote thisNote : songNotesCopy) {
                    thisNoteRect.setEmpty();

                    //Scenario 1: Left side of this note is cut off by the left bound
                    if (thisNote.startTime_s * 1000 < left_time_bound_ms && thisNote.startTime_s * 1000 + thisNote.duration_ms > left_time_bound_ms) {
                        //render a left-truncated note
                        left = (int) left_pixel_bound;
                        right = left + (int) ((thisNote.startTime_s * 1000 + thisNote.duration_ms - left_time_bound_ms) * pixelsPerMillisecond);
                        top = (int) (note_centers[thisNote.pianoKeyID - 40] - noteWidth / 2); //THIS IS BAD. NEED TO DO SOMETHING MORE ROBUST.
                        bottom = (int) (note_centers[thisNote.pianoKeyID - 40] + noteWidth / 2);

                    }

                    //Scenario 2: Note is encapsulated entirely inside the left and right bounds
                    else if (thisNote.startTime_s * 1000 > left_time_bound_ms && thisNote.startTime_s * 1000 + thisNote.duration_ms < right_time_bound_ms) {
                        //render the note normally
                        left = (int) (left_pixel_bound + (thisNote.startTime_s * 1000 - left_time_bound_ms) * pixelsPerMillisecond);
                        right = (int) (right_pixel_bound - ((right_time_bound_ms - (thisNote.startTime_s * 1000 + thisNote.duration_ms)) * pixelsPerMillisecond));
                        top = (int) (note_centers[thisNote.pianoKeyID - 40] - noteWidth / 2); //THIS IS BAD. NEED TO DO SOMETHING MORE ROBUST.
                        bottom = (int) (note_centers[thisNote.pianoKeyID - 40] + noteWidth / 2);
                    }

                    //Scenario 3: Right side of this note is cut off by the right bound
                    else if (thisNote.startTime_s * 1000 < right_time_bound_ms && thisNote.startTime_s * 1000 + thisNote.duration_ms > right_time_bound_ms) {
                        //render a right-truncated note
                        right = (int) right_pixel_bound;
                        left = right - (int) ((right_time_bound_ms - thisNote.startTime_s * 1000) * pixelsPerMillisecond);
                        top = (int) (note_centers[thisNote.pianoKeyID - 40] - noteWidth / 2); //THIS IS BAD. NEED TO DO SOMETHING MORE ROBUST.
                        bottom = (int) (note_centers[thisNote.pianoKeyID - 40] + noteWidth / 2);
                    }

                    //Uh oh
                    else {
                        Log.e("vocalUI", "Attempted to plot out-of-bounds note");
                        break;
                    }

                    //Clear the coordinates of the note
                    thisNoteRect.set(left, top, right, bottom);
                    canvas.drawRect(thisNoteRect, noteRectPaint);

                    //Clear the song note vector
                    songNotesInWindow.clear();

                }
            }
        }

        /*
        Draw the lyrics
            How to go about this? Make lyrics simply scroll horizontially according to their start times?
         */

        ArrayList<vocalLyric> songLyricsInWindow;

        if(isSongPlaying) {

            songLyricsInWindow = currentSong.getLyricsinWindow(left_time_bound_ms, right_time_bound_ms);

            //Make a deep copy to avoid a potential concurrent modification issue. Definitely don't want to go down that road again.
            ArrayList<vocalLyric> songLyricsCopy = new ArrayList<vocalLyric>(songLyricsInWindow.size());
            for (vocalLyric vl : songLyricsInWindow) songLyricsCopy.add(new vocalLyric(vl));

            //If we have something, let's draw it
            if (!songLyricsCopy.isEmpty()) {
                Paint lyricsPaint = new Paint();
                lyricsPaint.setColor(Color.BLACK);
                lyricsPaint.setTextSize(30);

                for (vocalLyric thisLyric : songLyricsCopy) {

                    float textYposition = height;


                    //Set x position simply based on the left time bound
                    float textXposition = (thisLyric.startTime_s*1000-left_time_bound_ms)*pixelsPerMillisecond + left_pixel_bound;

                    canvas.drawText(thisLyric.lyric, textXposition, textYposition, lyricsPaint);

                }
            }


        }

    }

    public class MainThread extends Thread {
        private boolean running;
        private SurfaceHolder surfaceHolder;
        private vocalUI VUI;

        public MainThread(SurfaceHolder surfaceHolder, vocalUI VUI) {
            super();
            this.surfaceHolder = surfaceHolder;
            this.VUI = VUI;
        }


        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {

            Canvas canvas;
            //Paint paint = new Paint();
            //Log.i("vocalUI", "can you hear me??");
            Log.i("vocalUI", "Starting display loop");

            int frameCount = 0;
            float frameRate;
            float framePeriod;
            long renderStartTime = SystemClock.uptimeMillis();
            long renderTimer = 0;


            AdditionalTestingInit();

            android.os.Process.setThreadPriority(android.os.Process.myTid(), Process.THREAD_PRIORITY_URGENT_DISPLAY);


            while (running) {





                //Get a canvas from the surface holder
                //~50 ms to complete on Nexus 5 API 24 1080x1920 screen
                canvas = this.surfaceHolder.lockCanvas();



                //TESTING: Try making a canvas manually
                //Bitmap bmap = createBitmap(width, height, Bitmap.Config.ARGB_8888);
                //canvas = new Canvas(bmap);

                //Log.i("vocalUI", "Checkpoint 1b: " + (SystemClock.uptimeMillis() - renderPeriod));

                //do something to the canvas
                //paint.reset();

                //Log.i("vocalUI", "Checkpoint 1c: " + (SystemClock.uptimeMillis() - renderPeriod));

                //paint.setColor(Color.BLACK);
                //paint.setTextSize(50);

                //Log.i("vocalUI", "Checkpoint 1d: " + (SystemClock.uptimeMillis() - renderPeriod));

                canvas.drawColor(Color.WHITE);
                //canvas.drawText("Holy moly", 100, 100+i*5, paint);

                //Log.i("vocalUI", "Checkpoint 2: " + (SystemClock.uptimeMillis() - renderPeriod));

                VUI.drawMusicStaff(canvas);

                //Log.i("vocalUI", "Checkpoint 3: " + (SystemClock.uptimeMillis() - renderPeriod));

                //display
                //~50 ms to complete on Nexus 5 API 24 1080x1920 screen
                this.surfaceHolder.unlockCanvasAndPost(canvas);
                frameCount++;

                //TESTING: try to draw canvas manually
                //this.VUI.draw(canvas);

               // Log.i("vocalUI", "Rendered in " + (SystemClock.uptimeMillis() - renderPeriod) + " ms.");
               // renderPeriod = SystemClock.uptimeMillis();

                //Every ten seconds, check the framerate
                renderTimer = SystemClock.uptimeMillis()-renderStartTime;
                if(renderTimer > 10000) {
                    framePeriod = ((float)renderTimer) / ((float)(frameCount));
                    frameRate = 1000/framePeriod;
                    //If we're geting less than 25 fps, print a warning.
                    if(Math.round(frameRate) < 25) {
                        Log.w("vocalUI", "Frame rate is less than 25 fps target: " + Math.round(frameRate) + " fps (" + Math.round(framePeriod) + " ms/frame)");
                        Log.w("vocalUI", "Hardware acceleration enabled: " + canvas.isHardwareAccelerated());
                    }
                    frameCount = 0;
                    renderStartTime = SystemClock.uptimeMillis();
                }


//                try {
//                    thread.sleep(25);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                //Log.i("vocalUI", "Height is: " + VUI.getCanvasHeight());

                //Log.i("vocalUI", "loop iteration");

            }

        }
    }

}
