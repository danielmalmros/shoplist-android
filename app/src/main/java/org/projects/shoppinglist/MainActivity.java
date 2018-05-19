package org.projects.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements MyDialogFragment.OnPositiveListener, AdapterView.OnItemSelectedListener {
    MyDialogFragment dialog;
    Context context;

    private final int RESULT_CODE_PREFERENCES = 1;

    FirebaseListAdapter<Product> adapter;
    ListView listView;

    //Save copys of delted products
    Map<String, Product> saveProductCopy = new HashMap<String, Product>();

    public FirebaseListAdapter<Product> getMyAdapter()
    {
        return adapter;
    }

    // Initialize Database
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference firebase = mRootRef.child("items");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /*************************************************************************************************
         * Uncomment the line below to force crash the application, to see the crash repport in Crashlytics.
         * Only use this line if you need to force crash the application.
         *************************************************************************************************/
        // Crashlytics.getInstance().crash();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Firebase connected to shopping list ListView from adapter
        Query query = FirebaseDatabase.getInstance().getReference().child("items");

        FirebaseListOptions<Product> options = new FirebaseListOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .setLayout(android.R.layout.simple_list_item_multiple_choice)
                .build();

        adapter = new FirebaseListAdapter<Product>(options) {
            @Override
            protected void populateView(View v, Product product, int position) {
                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(product.toString());
            }
        };

        // Getting listiew
        listView = findViewById(R.id.list);

        // Setting the adapter on the listview
        listView.setAdapter(adapter);

        // Setting the choice mode - meaning in this case we can select more than one item
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Setting the spinner for the shopping list
        final Spinner spinner = findViewById(R.id.spinner1);

        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.variant_array, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);
        spinner.setOnItemSelectedListener(this);

        final EditText addText = findViewById(R.id.addText);
        final EditText addQty = findViewById(R.id.addQty);

        // Creating the add to list button
        Button addButton =  findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addText.length() > 0 && addQty.length() > 0) {
                    Product p = new Product(addText.getText().toString(), Integer.parseInt(addQty.getText().toString()), spinner.getSelectedItem().toString());
                    firebase.push().setValue(p);
                    getMyAdapter().notifyDataSetChanged();

                    // Clear text from input field when item is added
                    addText.setText("");
                }
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    // Show dialog to delete all item in shopping list.
    public void showDialog() {
        dialog = new MyDialog();
        dialog.show(getFragmentManager(), "MyFragment");
    }

    // Positive button (yes button):
    // Clear database with data.
    @Override
    public void onPositiveClicked() {
        mRootRef.setValue(null);
        listView.clearChoices();
        adapter.notifyDataSetChanged();
        Toast.makeText(this,"All items cleared", Toast.LENGTH_SHORT).show();
    }

    // Negativ button (no button)
    // This makes sure we do not delete the hole shopping list.
    public static class MyDialog extends MyDialogFragment {
        @Override
        protected void negativeClick() {
            Toast toast = Toast.makeText(getActivity(),
                    "Your shopping is still here!", Toast.LENGTH_SHORT);
            toast.show();
        }
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

    public void saveDeletedProducts(String pos, Product product) {
        saveProductCopy.put(pos, product);
    }

    public void reAddSavedProducts() {
        for(Map.Entry<String, Product> entry : saveProductCopy.entrySet()){
            String key = entry.getKey();
            Product value = entry.getValue();

            // Re add products to list
            firebase.child(key).setValue(value);
        }
        saveProductCopy.clear();
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

        if (id == R.id.action_share) {
            StringBuilder sb = new StringBuilder();
            Intent intent = new Intent(Intent.ACTION_SEND);

            sb.append("Shopping list:\n");
            for (Product bagItem : adapter.getSnapshots()) {
                sb.append(bagItem.toString());
                sb.append("\n");
            }

            String productList = sb.toString();
            intent.putExtra(Intent.EXTRA_TEXT, productList);
            intent.setType("text/plain");
            startActivity(intent);
        }

        if (id == R.id.action_delete) {
            SparseBooleanArray position = listView.getCheckedItemPositions();

            for(int i = adapter.getCount() -1; i > -1; i--) {
                if (position.get(i)) {

                    //int i = listView.getCheckedItemPosition();
                    saveDeletedProducts(getMyAdapter().getRef(i).getKey(), adapter.getItem(i));
                    //bag.remove(i);
                    getMyAdapter().getRef(i).setValue(null);
                }
            }

            Snackbar snackbar = Snackbar
                    .make(listView, "Deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reAddSavedProducts();
                            adapter.notifyDataSetChanged();
                        }
                    });
            if (saveProductCopy.size() > 0) {
                snackbar.show();
            }

            listView.clearChoices();
            adapter.notifyDataSetChanged();

        }
        if (id == R.id.action_clear) {
            showDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}