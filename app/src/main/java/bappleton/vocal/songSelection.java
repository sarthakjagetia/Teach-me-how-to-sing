package bappleton.vocal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class songSelection extends AppCompatActivity implements constants{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_song_selection);

        /*
        Configure ListView
         */

        //Get a handle to the listview
        ListView lv = (ListView) findViewById(R.id.songListView);

        //Create an array of items to display in the list view
        ArrayList<String> listViewItems = new ArrayList<String>();
        listViewItems.add(SONG_DO_RE_ME);
        listViewItems.add(SONG_XMAS);

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
                launchSong((String)parent.getItemAtPosition(position));
            }
        });
    }

    private void launchSong(String song) {
        //Intent takes a context (mainactivity is a subclass of context) and the class that we should launch
        //An intent is everything we need to start a new activity
        Intent intent = new Intent(this, vocal.class);

        //Attach the song ID to the intent
        intent.putExtra(INTENT_SONG, song);

        startActivity(intent);

    }

}
