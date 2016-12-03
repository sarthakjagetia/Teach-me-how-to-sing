package bappleton.screenchangertest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SecondScreen extends AppCompatActivity {
    public final static int    ID_ACTIVITY_RESULT = 1000;
    public final static String    ID_ACTIVITY_DATA   = "bappleton.screenchangertest.ACTIVITYDATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);

        //Get intent that started this class
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //Let me know that this worked
        Log.i("SecondScreen", "Received message: " + message);


    }

    public void goBack(View view) {
        //Set result of this activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(ID_ACTIVITY_DATA, "Take this, first screen!");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

        //Close this activity
        finish();
    }
}
