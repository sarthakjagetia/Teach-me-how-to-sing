package bappleton.vocal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Iterator;
import java.util.Vector;

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

    //Width of notes display, in milliseconds
    private float timeWindow_ms;

    //Width of notes display, in pixels
    //  this is NOT the same as width, because note names and padding eats up space
    private float noteDisplayWidth_pixels;

    //Relationship between pixels and time (timeWindow_s/noteDisplayWidth)
    private float pixelsPerMillisecond;

    //System time at beginning of song
    //  use System.currentTimeMillis()
    private float startTime_ms;

    //Is song playing?
    private boolean isSongPlaying;

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

        //Set up for testing
        AdditionalTestingInit(context);

    }

    //TO BE DELETED, for pre-integration testing only
    private void AdditionalTestingInit(Context context) {
        setSong(demoSong1());

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("vocalUI", "SurfaceChanged call. Height: " + height + " width: " + width);
        this.height = height;
        this.width  = width;


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
    }

    private vocalSong demoSong1() {
        //Do re me fa so la ti do

        Vector<vocalSongNote> notes = new Vector<vocalSongNote>(8);
        notes.setElementAt(new vocalSongNote(40,0,1000), 0);
        notes.setElementAt(new vocalSongNote(42,1,1000), 1);
        notes.setElementAt(new vocalSongNote(44,2,1000), 2);
        notes.setElementAt(new vocalSongNote(45,3,1000), 3);
        notes.setElementAt(new vocalSongNote(47,4,1000), 4);
        notes.setElementAt(new vocalSongNote(49,5,1000), 5);
        notes.setElementAt(new vocalSongNote(51,6,1000), 6);
        notes.setElementAt(new vocalSongNote(52,7,1000), 7);

        Vector<vocalLyric> lyrics = new Vector<vocalLyric>(8);
        lyrics.setElementAt(new vocalLyric("Do", 0), 0);
        lyrics.setElementAt(new vocalLyric("Re", 1), 1);
        lyrics.setElementAt(new vocalLyric("Me", 2), 2);
        lyrics.setElementAt(new vocalLyric("Fa", 3), 3);
        lyrics.setElementAt(new vocalLyric("So", 4), 4);
        lyrics.setElementAt(new vocalLyric("La", 5), 5);
        lyrics.setElementAt(new vocalLyric("Ti", 6), 6);
        lyrics.setElementAt(new vocalLyric("Do", 7), 7);

        return new vocalSong(notes, lyrics);

    }

    public void beginSong() {
        //Capture start time and begin UI rendering
        //thread.setRunning(true);
        startTime_ms = System.currentTimeMillis();
        isSongPlaying = true;
    }

    public void endSong() {
        //thread.setRunning(false);
        startTime_ms = 0;
        isSongPlaying = false;
    }

    private void drawMusicStaff(Canvas canvas) {
        //Purpose: draw five lines for the musical staff at the bottom of the screen

        /*
        Config settings follow. All units in pixels.
         */

        //Gap between five staff lines
        float lineSpacing = 50;

        //Line thickness, in pixels
        float strokeWidth = 1;

        //Distance between bottom of canvas and bottom line
        float bottomLineOffset = 20;

        //Padding between edge of canvas and edge of screen
        //xLeftPadding is overridden if the note names won't fit.
        float xLeftPadding = 20;
        float xRightPadding = 0;

        /*
        Draw the five lines on the canvas
         */

        //configure paint
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(strokeWidth);

        Paint noteTextPaint = new Paint();
        noteTextPaint.setColor(Color.GRAY);
        noteTextPaint.setTextSize(30);


        //draw lines and note names, bottom up, from C4 (key 40) to C5 (key 52)

        String[] notes = {"C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4", "C5"};
        float[]  note_centers = new float[13];

        //figure out the bounds of the text, in pixels. Height will be used to adjust position, width will be used to adjust xLeftPadding if needed
        Rect textBounds = new Rect();
        noteTextPaint.getTextBounds("C#4", 0, 3, textBounds);
        if (textBounds.width() > xLeftPadding) {
            xLeftPadding = textBounds.width() + 5;
        }

        float y_position = 0;

        for (int i = 0; i<13; i++) {
            //Draw staff line
            y_position = height - (bottomLineOffset + i * lineSpacing);
            canvas.drawLine(0 + xLeftPadding, y_position, width - xRightPadding, y_position, linePaint);
            //Record y_position of staff line
            note_centers[i] = y_position;
            //Draw note name to left of staff line
            canvas.drawText(notes[i], 0, height - (bottomLineOffset + i * lineSpacing) + textBounds.height()/2, noteTextPaint);
        }
        /*
        Draw the notes
         */

        //Configure global constants. NEED TO PUT THESE SOMEWHERE BETTER
        noteDisplayWidth_pixels = width - xLeftPadding - xRightPadding;
        timeWindow_ms = 3000;
        pixelsPerMillisecond = noteDisplayWidth_pixels/timeWindow_ms;

        //Draw a test note
        //For now, let's make the notes 75% as wide as the spaces
        float noteWidth = (float)0.75*lineSpacing;
        //Rect testNote = new Rect(100,(int)(note_centers[3]-noteWidth/2),300,(int)(note_centers[3]+noteWidth/2));
        Paint noteRectPaint = new Paint();
        noteRectPaint.setColor(Color.BLUE);
        //canvas.drawRect(testNote,noteRectPaint);

        Vector<vocalSongNote> songNotesInWindow;

        //if a song is playing, let's draw some notes
        if(isSongPlaying) {
            //get time bounds, in terms of location in the song, for the display
            float left_time_bound_ms = System.currentTimeMillis() - startTime_ms;
            float right_time_bound_ms = left_time_bound_ms + timeWindow_ms;

            //get pixel bounds
            float left_pixel_bound = 0 + xLeftPadding;
            float right_pixel_bound = width-xRightPadding;

            //get a vector of the song notes that are within the time bounds
            songNotesInWindow = currentSong.getNotesInWindow(left_time_bound_ms,right_time_bound_ms);

            //iterate through this vector
            Iterator<vocalSongNote> itr = songNotesInWindow.iterator();
            vocalSongNote thisNote;

            //rectangle to draw the note. Everything here is in pixels
            Rect thisNoteRect = new Rect();
            int top, left, right, bottom;

            while(itr.hasNext()) {
                thisNote = itr.next();


                //Scenario 1: Left side of this note is cut off by the left bound
                if(thisNote.startTime_s*1000 < left_time_bound_ms && thisNote.startTime_s*1000+thisNote.duration_ms > left_time_bound_ms) {
                    //render a left-truncated note
                    left = (int)left_pixel_bound;
                    right = left + (int)((thisNote.startTime_s*1000+thisNote.duration_ms-left_time_bound_ms)*pixelsPerMillisecond);
                    top = (int)(note_centers[thisNote.pianoKeyID-40]-noteWidth/2); //THIS IS BAD. NEED TO DO SOMETHING MORE ROBUST.
                    bottom = (int)(note_centers[thisNote.pianoKeyID-40]+noteWidth/2);

                }

                //Scenario 2: Note is encapsulated entirely inside the left and right bounds
                else if (thisNote.startTime_s*1000 > left_time_bound_ms && thisNote.startTime_s*1000+thisNote.duration_ms < right_time_bound_ms) {
                    //render the note normally
                }

                //Scenario 3: Right side of this note is cut off by the right bound
                else if (thisNote.startTime_s*1000 < right_time_bound_ms && thisNote.startTime_s*1000+thisNote.duration_ms > right_time_bound_ms) {
                    //render a right-truncated note
                }

                //Uh oh
                else {
                    Log.e("vocalUI", "Attempted to plot out-of-bounds note");
                }

                //Clear the coordinates of the note
                thisNoteRect.set(0,0,0,0);

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
            Paint paint = new Paint();
            Log.i("vocalUI", "can you hear me??");
            //Log.i("vocalUI", "Starting display loop");
            float i = 0;

            while(running) {

                Log.i("vocalUI", "entered running loop4");
                i++;
                //Update state and render to screen
                canvas = this.surfaceHolder.lockCanvas();
                //do something to the canvas
                paint.reset();
                paint.setColor(Color.BLACK);
                paint.setTextSize(50);
                canvas.drawColor(Color.WHITE);
                canvas.drawText("Holy moly", 100, 100+i*5, paint);
                VUI.drawMusicStaff(canvas);

                //display
                this.surfaceHolder.unlockCanvasAndPost(canvas);

                try {
                    thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i("vocalUI", "Height is: " + VUI.getCanvasHeight());

                Log.i("vocalUI", "loop iteration");

           }

        }
    }

}
