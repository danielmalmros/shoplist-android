package org.projects.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int RESULT_CODE_PREFERENCES = 1;

    ArrayAdapter<String> adapter;
    ListView listView;
    ArrayList<String> bag = new ArrayList<>();

    public ArrayAdapter getMyAdapter()
    {
        return adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Needed to get the toolbar to work on older versions
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //do we have some saved state?
        if (savedInstanceState != null) {
            System.out.println("test");
            ArrayList<String> savedBag = savedInstanceState.getStringArrayList("savedBag");
            if (savedBag != null) { //did we save something
                System.out.println("test deeper");
                bag = savedBag;
            }
        }

        //getting our listiew - you can check the ID in the xml to see that it
        //is indeed specified as "list"
        listView = findViewById(R.id.list);
        //here we create a new adapter linking the bag and the
        //listview
        adapter =  new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,bag );

        //setting the adapter on the listview
        listView.setAdapter(adapter);
        //here we set the choice mode - meaning in this case we can
        //only select one item at a time.
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        final EditText addText = findViewById(R.id.addText);
        final EditText addQty = findViewById(R.id.addQty);

        Button addButton =  findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.add(addText.getText().toString() + " x " + addQty.getText().toString());
                Log.d("Bag","Items in back: "+bag.size());
            }
        });
    }

    //This method updates our text views.
    public void updateUI(String name, boolean male)
    {
        TextView myName = findViewById(R.id.myName);
        TextView myGender = findViewById(R.id.myGender);
        myName.setText(name);
        if (male)
            myGender.setText(R.string.male);
        else
            myGender.setText(R.string.female);
    }

    //This method is called before our activity is destroyed
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		/* Here we put code now to save the state */
        outState.putStringArrayList("savedBag", bag);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==RESULT_CODE_PREFERENCES) //the code means we came back from settings
        {
            //I can can these methods like this, because they are static
            boolean male = SettingsPreference.isMale(this);
            String name = SettingsPreference.getName(this);
            String message = "Welcome, "+name+", You are male? "+male;
            Toast toast = Toast.makeText(this,message,Toast.LENGTH_LONG);
            toast.show();
            updateUI(name,male);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Start our settingsactivity and listen to result - i.e.
            //when it is finished.
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivityForResult(intent,RESULT_CODE_PREFERENCES);
            //notice the 1 here - this is the code we then listen for in the
            //onActivityResult
        }

        if (id == R.id.action_delete) {
            SparseBooleanArray position = listView.getCheckedItemPositions();

            for(int i = bag.size() -1; i > -1; i--) {
                if (position.get(i)) {
                    bag.remove(i);
                }
            }
            listView.clearChoices();
            adapter.notifyDataSetChanged();
        }
        if (id == R.id.action_clear) {
            bag.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this,"All items cleared", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}