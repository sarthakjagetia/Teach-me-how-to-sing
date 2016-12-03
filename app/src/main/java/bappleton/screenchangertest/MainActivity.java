package bappleton.screenchangertest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "bappleton.screenchangertest.MESSAGE";
    public final static int ID_ACTIVITY_RESULT = 1000;
    public final static String ID_ACTIVITY_DATA = "bappleton.screenchangertest.ACTIVITYDATA";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Configure ListView
         */

        //Get a handle to the listview
        ListView lv = (ListView) findViewById(R.id.myListView);

        //Create an array of items to display in the list view
        ArrayList<String> listViewItems = new ArrayList<String>();
        listViewItems.add("foo");
        listViewItems.add("bar");

        //Define the array adapter
        //This takes a context (this), some layout for the list items, and the arraylist we just created
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listViewItems);

        //Now set the contents of the list view to our array adapter
        lv.setAdapter(arrayAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("First Screen", "Item clicked");
                Log.i("First Screen", "Position is: " + position);
                Log.i("First Screen", "Item at position in parent is: " + parent.getItemAtPosition(position));
                Log.i("First Screen", "ID is: " + id);
            }
        });
    }

    /*
    CALLBACK FOR LIST ITEM SELECTION
     */


    /*
    SCREEN CHANGE APPROACH 1: STARTED ACTIVITY DOESN'T NEED TO RETURN INFORMATION
     */
    public void changeScreenButton(View view) {
        //Intent takes a context (mainactivity is a subclass of context) and the class that we should launch
        //An intent is everything we need to start a new activity
        Intent intent = new Intent(this, SecondScreen.class);

        //An extra is a key:value pair that we want to send to the activity that we launch
        String message = "Test me.";
        intent.putExtra(EXTRA_MESSAGE, message);

        startActivity(intent);
    }

    /*
    SCREEN CHANGE APPROACH 2: STARTED ACTIVITY IS DONE FOR THE PURPOSE OF RETURNING INFORMATION
     */
    public void changeScreenButton2(View view) {
        //Intent takes a context (mainactivity is a subclass of context) and the class that we should launch
        //An intent is everything we need to start a new activity
        Intent intent = new Intent(this, SecondScreen.class);

        //An extra is a key:value pair that we want to send to the activity that we launch
        String message = "Test me.";
        intent.putExtra(EXTRA_MESSAGE, message);

        startActivityForResult(intent, ID_ACTIVITY_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (ID_ACTIVITY_RESULT): {
                if (resultCode == Activity.RESULT_OK) {
                    String newText = data.getStringExtra(ID_ACTIVITY_DATA);
                    // Display the data we got back
                    Log.i("FirstScreen", newText);
                }
                break;
            }
        }
    }


}