package bappleton.vocal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import static android.graphics.Bitmap.createBitmap;
import static java.lang.Float.NaN;


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

    //Parent handler for sending messages
    Handler parentHandler;

    private final String TAG = "vocalUI";

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
    private String[] notes = {"A3","A#3","B3","C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4", "C5", "C#5", "D5", "D#5", "E5"};
    private  float[] note_centers;
    private float yPosNowBottom;
    private float yPosNowTop;
    private float left_time_bound_ms;
    private float right_time_bound_ms;
    private float left_pixel_bound;
    private float right_pixel_bound;
    private int lowestNoteKeyID;
    private int highestNoteKeyID;
    /*
    End rendering variables
     */


    //Messages received from child pitch detection thread
    private final int SIGNAL_DETECTION_RUNNING      = 2004;
    private final int SIGNAL_DETECTION_STOPPED      = 2005;
    private final int SIGNAL_PITCH_REQUEST          = 2006;

    //Messages for main rendering thread
    private final int CASE_START_RENDERING  = 3000;
    private final int CASE_RENDER_FRAME     = 3001;
    private final int CASE_STOP_RENDERING   = 3002;
    private final int CASE_BEGIN_SONG       = 3003;
    private final int CASE_END_SONG         = 3004;

    //Messages to send back to parent thread
    private final int SIGNAL_SONG_COMPLETE  = 4000;
    private final int SIGNAL_UI_UPDATE      = 4001;

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

        //Try to make it transparent
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

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
        this.setSong(new vocalExerciseLibrary().demoSong1());
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
        //BRIAN CHANGED THIS 11/23. THIS IS WHERE WE ORIGINALLY SET THE THREAD RUNNING.
        //thread.setRunning(true);
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

    public void setParentHandler(Handler parentHandler) {
        this.parentHandler = parentHandler;
    }

    public void beginSong() {
        //Capture start time and begin UI rendering

        //Message-based approach
        Message beginSongMsg = Message.obtain();
        beginSongMsg.what = CASE_BEGIN_SONG;
        thread.vocalUIHandler.sendMessage(beginSongMsg);

        //Direct access based approach
        /*
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
        */

    }

    public void endSong() {

        //Message based appproach
        Message endSongMsg = Message.obtain();
        endSongMsg.what = CASE_END_SONG;
        thread.vocalUIHandler.sendMessage(endSongMsg);

        //Direct access based approach
        /*
        startTime_ms = 0;
        isSongPlaying = false;
        */


    }

    public void beginRendering() {
        Message beginRenderMsg = Message.obtain();
        beginRenderMsg.what = CASE_START_RENDERING;
        thread.vocalUIHandler.sendMessage(beginRenderMsg);
    }

    public void stopRendering() {
        Message stopRenderMsg = Message.obtain();
        stopRenderMsg.what = CASE_STOP_RENDERING;
        thread.vocalUIHandler.sendMessage(stopRenderMsg);
    }

    private void configureDisplayConstants() {

        //The spacing between the horizontal music note lines
        lineSpacing = (float)Math.round(0.045*height); //50;

        //Music staff linewidth. 1 gives a hairline.
        strokeWidth = 1;

        //Offset between the bottom music note line and the bottom of the canvas
        bottomLineOffset = (float)Math.round(0.10*height); //20;

        //Offset between the left bound of the music note line and the canvas. This will be adjusted automatically if the note name won't fit.
        xLeftPadding = (float)Math.round(0.02*height);//20;

        //Offset between the right bound of the music note line and the canvas
        xRightPadding = 0;

        //Paint configuration for music note lines
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(strokeWidth);
        linePaint.setAntiAlias(true);

        //Paint configuration for music note names
        //Presently these scale as a function of canvas width
        noteTextPaint = new Paint();
        noteTextPaint.setColor(Color.GRAY);
        noteTextPaint.setTextSize((float)Math.round(0.04*width));  //(30);
        noteTextPaint.setAntiAlias(true);

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

        //Calculate array that holds the y-positions of the 20 music note lines. 0 corresponds to the bottom line.
        //There MUST be note names stores in the "notes" array to match up with these
        note_centers = new float[20];
        for (int i = 0; i < note_centers.length; i++) {
            note_centers[i] = height - (bottomLineOffset + i * lineSpacing);
        }

        //Indicate key IDs of lowest and highest notes that we will display.
        //This must jive with the note_centers array. highestNoteKeyID-lowestNoteKeyID+1 = sizeof(note_centers).
        //note_centers[0] will be the vertical canvas position of lowestNoteKeyID
        //note_centers[sizeof(note_centers)-1] will be the vertical canvas position of highestNoteKeyID.
        //The function keyIDtoVertCanvasPos will be used to avoid direct access of the note_centers array.
        lowestNoteKeyID  = 37; //C4
        highestNoteKeyID = 56; //C5

        //Set the y-limits of the vertical line for "Now"
        yPosNowBottom = note_centers[0];
        yPosNowTop = note_centers[note_centers.length-1];
    }

    private float keyIDtoVertCanvasPos(int keyID) {
        if (keyID >= lowestNoteKeyID && keyID <= highestNoteKeyID) {
            return note_centers[keyID-lowestNoteKeyID];
        }
        else {
            Log.e(TAG, "Cannot plot note " + keyID +". Outside inclusive bounds of " + lowestNoteKeyID + " and " + highestNoteKeyID + ".");
            return -1; //Calling function must handle this case.
        }

    }

    private void drawMusicStaff(Canvas canvas) {

        /*
        Draw the staff lines, note names, and a vertical line indicating "now"
         */

        //20 staff lines and note names
        for (int i = 0; i < note_centers.length; i++) {
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
                float thisVertNoteCenter;

                for (vocalSongNote thisNote : songNotesCopy) {
                    thisNoteRect.setEmpty();

                    thisVertNoteCenter = keyIDtoVertCanvasPos(thisNote.pianoKeyID);
                    if (thisVertNoteCenter == -1) {
                        //If we attempted to plot a keyID outside (lowestNoteKeyID,highestNoteKEYID), skip it.
                        Log.w(TAG, "Failed to plot note " + thisNote.pianoKeyID);
                        //Go to the next note in the parent for loop.
                        continue;
                    }

                    //Scenario 1: Left side of this note is cut off by the left bound
                    if (thisNote.startTime_s * 1000 < left_time_bound_ms && thisNote.startTime_s * 1000 + thisNote.duration_ms > left_time_bound_ms) {
                        //render a left-truncated note
                        left = (int) left_pixel_bound;
                        right = left + (int) ((thisNote.startTime_s * 1000 + thisNote.duration_ms - left_time_bound_ms) * pixelsPerMillisecond);
                        top = (int) (thisVertNoteCenter - noteWidth / 2);
                        bottom = (int) (thisVertNoteCenter + noteWidth / 2);

                    }

                    //Scenario 2: Note is encapsulated entirely inside the left and right bounds
                    else if (thisNote.startTime_s * 1000 >= left_time_bound_ms && thisNote.startTime_s * 1000 + thisNote.duration_ms < right_time_bound_ms) {
                        //render the note normally
                        left = (int) (left_pixel_bound + (thisNote.startTime_s * 1000 - left_time_bound_ms) * pixelsPerMillisecond);
                        right = (int) (right_pixel_bound - ((right_time_bound_ms - (thisNote.startTime_s * 1000 + thisNote.duration_ms)) * pixelsPerMillisecond));
                        top = (int) (thisVertNoteCenter - noteWidth / 2);
                        bottom = (int) (thisVertNoteCenter + noteWidth / 2);
                    }

                    //Scenario 3: Right side of this note is cut off by the right bound
                    else if (thisNote.startTime_s * 1000 < right_time_bound_ms && thisNote.startTime_s * 1000 + thisNote.duration_ms > right_time_bound_ms) {
                        //render a right-truncated note
                        right = (int) right_pixel_bound;
                        left = right - (int) ((right_time_bound_ms - thisNote.startTime_s * 1000) * pixelsPerMillisecond);
                        top = (int) (thisVertNoteCenter - noteWidth / 2);
                        bottom = (int) (thisVertNoteCenter + noteWidth / 2);
                    }

                    //Uh oh
                    else {
                        Log.e("vocalUI", "Attempted to plot out-of-bounds note. Time bounds: (" + left_time_bound_ms + "," + right_time_bound_ms + "). This note: (" + thisNote.startTime_s*1000 + "," + (thisNote.startTime_s*1000+thisNote.duration_ms) + ").");
                        //Skip to the next note
                        continue;
                    }

                    //Experimental: Note coloring based on hit success
                    if(thisNote.pitchMatchedKeyID) {
                        noteRectPaint.setColor(Color.argb(255,0,100,255));
                    }
                    else {
                        noteRectPaint.setColor(Color.GRAY);
                    }

                    //Draw the note on the canvas
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
                lyricsPaint.setAntiAlias(true);

                for (vocalLyric thisLyric : songLyricsCopy) {

                    float textYposition = height;


                    //Set x position simply based on the left time bound
                    float textXposition = (thisLyric.startTime_s*1000-left_time_bound_ms)*pixelsPerMillisecond + left_pixel_bound;

                    canvas.drawText(thisLyric.lyric, textXposition, textYposition, lyricsPaint);

                }
            }
        }
    }

    private void drawPitchFeedback(Canvas canvas, float exactKeyID, vocalSongNote currentNote) {
        //Canvas: canvas on which we should draw
        //exactKeyID: what the user is currently singing
        //currentNote: what the song is currently playing

        //Horizontial position of the feedback is xPosNow; we're drawing it on top of the "now" line.
        //Vertical position is determined using lowestNoteKeyID, highestNoteKeyID, yPosNowBottom, yPosNowTop

        int yMarkerPos;
        int xMarkerPos;
        int markerWidth;

        //Configure vertical position of the pitch feedback marker.
        //This indicates what pitch the user is singing
        if (exactKeyID >= lowestNoteKeyID-0.5 && exactKeyID <= highestNoteKeyID+0.5) {
            //If the user's singing a note that is within the bounds of the canvas
            yMarkerPos = Math.round((exactKeyID - (float) lowestNoteKeyID) * (yPosNowTop - yPosNowBottom) / ((float) (highestNoteKeyID - lowestNoteKeyID)) + yPosNowBottom);
        }
        else if (exactKeyID < lowestNoteKeyID && exactKeyID >= 1){
            //User is singing a note that is below the canvas bounds
            yMarkerPos = Math.round(yPosNowBottom + lineSpacing/2); //Should this be handled more dynamically? How do we make sure there's room on the canvas?
        }
        else if (exactKeyID > highestNoteKeyID) {
            //User if singing a note that is higher than the canvas bounds
            yMarkerPos = Math.round(yPosNowTop - lineSpacing/2); //Should this be handled more dynamically? How do we make sure there's room on the canvas?
        }
        else {
            //Intentionally draw the pitch feedback off-canvas.
            yMarkerPos = Math.round(height+lineSpacing*2);
        }

        float no_error = (float)0.3; //What is the threshold for singing the correct note?
        float currentError;

        if(exactKeyID  != -1) {
            //If there's a pitch detected

            //Calculate currentError
            currentError = Math.abs(exactKeyID-currentNote.pianoKeyID);

            //Record whether the correct pith was sung
            //This information is used to score the user
            if (currentError < no_error && !currentNote.pitchMatchedKeyID) {
                Log.i(TAG, "Note hit.");
                currentNote.pitchMatchedKeyID = true;
            }
        }

        xMarkerPos = Math.round(xPosNow);
        markerWidth = Math.round(lineSpacing * 3 / 4) * 2; //This will always produce an even number. Later we divide by 2 and want an integer.

        Point p1 = new Point(xMarkerPos, yMarkerPos);
        Point p2 = new Point(xMarkerPos - markerWidth / 2, yMarkerPos - markerWidth / 2);
        Point p3 = new Point(xMarkerPos - markerWidth / 2, yMarkerPos + markerWidth / 2);

        Path markerPath = new Path();
        markerPath.setFillType(Path.FillType.EVEN_ODD);

        markerPath.moveTo(p1.x, p1.y);
        markerPath.lineTo(p2.x, p2.y);
        markerPath.lineTo(p3.x, p3.y);
        markerPath.close();

        Paint markerPaint = new Paint();
        markerPaint.setARGB(255,50,50,50);

        canvas.drawPath(markerPath, markerPaint);
    }

    public class MainThread extends Thread {
        //Handler for this thread
        public Handler vocalUIHandler;

        private boolean running;
        private boolean pitchDetectionRunning;
        private SurfaceHolder surfaceHolder;
        private vocalUI VUI;
        private Canvas UIcanvas;

        //Pitch detection object
        pitchDetectThread pitchThread;

        //Variables for frame rate tracking
        private int frameCount;
        private float frameRate;
        private float framePeriod;
        private long renderStartTime;
        private long renderTimer;

        //Variables for updating main UI
        private vocalUIupdate mainUIupdate;
        private float elapsedTime_ms;
        private float lastUpdate_ms;

        public MainThread(SurfaceHolder surfaceHolder, vocalUI VUI) {
            super();
            this.surfaceHolder = surfaceHolder;
            this.VUI = VUI;
        }


        public void setRunning(boolean running) {
            this.running = running;
        }

        private void checkFramerate() {
            frameCount++;
            //Every ten seconds, check the framerate
            renderTimer = SystemClock.uptimeMillis()-renderStartTime;
            if(renderTimer > 10000) {
                framePeriod = ((float)renderTimer) / ((float)(frameCount));
                frameRate = 1000/framePeriod;
                //If we're geting less than 25 fps, print a warning.
                if(Math.round(frameRate) < 25) {
                    Log.w("vocalUI", "Frame rate is less than 25 fps target: " + Math.round(frameRate) + " fps (" + Math.round(framePeriod) + " ms/frame)");
                    Log.w("vocalUI", "Hardware acceleration enabled: " + UIcanvas.isHardwareAccelerated());
                }
                frameCount = 0;
                renderStartTime = SystemClock.uptimeMillis();
            }
        }

        private void updateMainUI(boolean now) {
            //Function to update main UI with pertinent information during playback

            //Send a temporal position and score update to the main UI every second
            elapsedTime_ms = SystemClock.uptimeMillis()-startTime_ms;
            if (elapsedTime_ms - lastUpdate_ms > 1000 || now) {

                //Set time information
                mainUIupdate.setTimeStatus(elapsedTime_ms,currentSong.getSongLength_s());

                //Set score information
                mainUIupdate.setScore(currentSong.getFinalScore());

                //Send data to main UI
                Message mainUIupdateMSG = Message.obtain();
                mainUIupdateMSG.what = SIGNAL_UI_UPDATE;
                mainUIupdateMSG.obj  = mainUIupdate;
                parentHandler.sendMessage(mainUIupdateMSG);

                //Record the time of this update, in terms of the elapsed song time
                lastUpdate_ms = elapsedTime_ms;
            }

        }

        @Override
        public void run() {

            Log.i("vocalUI", "Starting display loop");

            //AdditionalTestingInit();

            android.os.Process.setThreadPriority(android.os.Process.myTid(), Process.THREAD_PRIORITY_URGENT_DISPLAY);

            //This will be update to true when pitchThread confirms it's running
            pitchDetectionRunning = false;

            //Initialize the UI update object
            mainUIupdate = new vocalUIupdate();
            lastUpdate_ms = 0;

            //Render a frame so we don't have a black box at startup
            UIcanvas = surfaceHolder.lockCanvas();
            UIcanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            VUI.drawMusicStaff(UIcanvas);
            surfaceHolder.unlockCanvasAndPost(UIcanvas);

            Looper.prepare();
            //Define the message handler for this thread
            vocalUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case CASE_START_RENDERING:
                            //Set running flag to true
                            running = true;
                            //Set up framerate monitoring variables
                            frameCount = 0;
                            renderStartTime = SystemClock.uptimeMillis();
                            renderTimer = 0;
                            //Start pitch detection
                            pitchThread.startPitchDetection();
                            //Send a message to myself to render the first frame
                            Message startRenderingMsg = Message.obtain();
                            startRenderingMsg.what = CASE_RENDER_FRAME;
                            vocalUIHandler.sendMessage(startRenderingMsg);
                            Log.i(TAG, "Rendering started.");
                            break;
                        case CASE_RENDER_FRAME:
                            //Draw and post a canvas if rendering is running
                            if(running) {
                                //Render a frame
                                UIcanvas = surfaceHolder.lockCanvas();
                                //UIcanvas.drawColor(Color.WHITE);
                                //UIcanvas.drawColor(Color.argb(255,250,250,250));
                                UIcanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                VUI.drawMusicStaff(UIcanvas);
                                //If we have pitch detection running, provide pitch feedback
                                if (pitchDetectionRunning) {
                                    //Presently we will directly grab pitch data from the pitch thread.
                                    //This is a nice approach because we will get whatever the pitch is at time of rendering.
                                    //However, I am not sure if it is more kosher to receive data via Message. TBD.
                                    drawPitchFeedback(UIcanvas, pitchThread.getPitchDirectly().getExactKeyID(), currentSong.getCurrentNote(SystemClock.uptimeMillis()-startTime_ms));
                                    //Note that the drawPitchFeedback routine also sets the pitchMatchedKeyID flag of the curent note.
                                }
                                surfaceHolder.unlockCanvasAndPost(UIcanvas);
                                //Check the framerate
                                checkFramerate();

                                //If a song is currenly playing, communicate relevant info back to the main UI
                                if(isSongPlaying) {
                                    //Check to see if the song has ended. If so, tell the main UI via message immediately.
                                   if(currentSong.isSongOver(SystemClock.uptimeMillis() - startTime_ms - timeWindowRenderPastNow)) {
                                       Log.i(TAG, "Song complete");
                                       Log.i(TAG, "Score: " + currentSong.getFinalScore() + "%");
                                       isSongPlaying = false;
                                       Message songCompleteMsg = Message.obtain();
                                       songCompleteMsg.what = SIGNAL_SONG_COMPLETE;
                                       songCompleteMsg.arg1 = currentSong.getFinalScore();
                                       parentHandler.sendMessage(songCompleteMsg);
                                       updateMainUI(true);
                                   }
                                    //Perform lower-priority periodic main UI updates
                                    updateMainUI(false);
                                }

                                //Tell myself to do it again.
                                //This does not try to stabilize the framerate; it'll
                                //deliver as fast a framerate as the device can render.
                                //Not ideal, but OK for now. Future improvement.
                                Message renderAnotherMsg = Message.obtain();
                                renderAnotherMsg.what = CASE_RENDER_FRAME;
                                vocalUIHandler.sendMessage(renderAnotherMsg);
                            }
                            break;
                        case CASE_STOP_RENDERING:
                            running = false;
                            //Stop pitch detection
                            pitchThread.stopPitchDetection();
                            pitchDetectionRunning = false;
                            Log.i(TAG, "Rendering stopped.");
                            break;
                        case SIGNAL_DETECTION_RUNNING:
                            Log.i(TAG, "Received confirmation from pitch thread that it is running.");
                            pitchDetectionRunning = true;
                            break;
                        case SIGNAL_DETECTION_STOPPED:
                            Log.i(TAG, "Received confirmation from pitch thread that it has stopped");
                            pitchDetectionRunning = false;
                            break;
                        case CASE_BEGIN_SONG:
                            //Set variables to begin rendering
                            startTime_ms = SystemClock.uptimeMillis();
                            lastUpdate_ms = 0;
                            isSongPlaying = true;
                            updateMainUI(true);
                            break;
                        case CASE_END_SONG:
                            //Set variables to stop rendering
                            startTime_ms = 0;
                            isSongPlaying = false;
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            };

            //Start the pitch detection thread, give it the handler we just made
            pitchThread = new pitchDetectThread(vocalUIHandler);
            pitchThread.start();

            Looper.loop();

        }
    }

}
