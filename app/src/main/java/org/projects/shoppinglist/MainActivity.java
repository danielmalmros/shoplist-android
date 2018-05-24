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

    // Save copies of deleted products in HashMap, so that we can get key and value.
    Map<String, Product> saveProductCopy = new HashMap<String, Product>();

    public FirebaseListAdapter<Product> getMyAdapter() {
        return adapter;
    }

    // Initialize Database from Firebase.
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

        // Firebase connected to shopping list ListView from adapter.
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

        // Getting ListView.
        listView = findViewById(R.id.list);

        // Setting the adapter on the ListView.
        listView.setAdapter(adapter);

        // Setting the choice mode - meaning in this case we can select more than one item.
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Setting the spinner for the shopping list.
        final Spinner spinner = findViewById(R.id.spinner1);

        // Array adapter for spinner items.
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.variant_array, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);
        spinner.setOnItemSelectedListener(this);

        // Getting the add item and add quantities id.
        final EditText addText = findViewById(R.id.addText);
        final EditText addQty = findViewById(R.id.addQty);

        // Creating the add to list button
        Button addButton = findViewById(R.id.addButton);
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

    // Get the position of the selected item in spinner.
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Interface callback.
    }

    // Show dialog to delete all item in shopping list.
    public void showDialog() {
        dialog = new MyDialog();
        dialog.show(getFragmentManager(), "MyFragment");
    }

    // Positive button (yes button).
    // Clear database with data.
    @Override
    public void onPositiveClicked() {
        mRootRef.setValue(null);
        listView.clearChoices();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "All items cleared", Toast.LENGTH_SHORT).show();
    }

    // Negative button (no button).
    // This makes sure we do not delete the hole shopping list.
    public static class MyDialog extends MyDialogFragment {
        @Override
        // Shows a toast that all items are still in the shopping list.
        protected void negativeClick() {
            Toast toast = Toast.makeText(getActivity(),
                    "Your shopping is still here!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // This method updates our text views for settings options.
    public void updateUI(String name, boolean male) {
        TextView myName = findViewById(R.id.myName);
        TextView myGender = findViewById(R.id.myGender);
        myName.setText(name);

        if (male) {
            myGender.setText(R.string.male);
        } else {
            myGender.setText(R.string.female);
        }
    }

    // Inflate the menu - this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Shows a toast to the user about the settings done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE_PREFERENCES) {
            boolean male = SettingsPreference.isMale(this);
            String name = SettingsPreference.getName(this);
            String message = "Welcome, " + name + ", You are male? " + male;
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            toast.show();
            updateUI(name, male);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // This function save the deleted item to the created hash map at the top.
    public void saveDeletedProducts(String pos, Product product) {
        saveProductCopy.put(pos, product);
    }

    // This function is created to add the deleted item back if user choose UNDO.
    public void reAddSavedProducts() {

        // A simple loop to get the key and value back from the stored HashMap of deleted items.
        for (Map.Entry<String, Product> entry : saveProductCopy.entrySet()) {
            String key = entry.getKey();
            Product value = entry.getValue();

            // Using the key and value to re add the deleted item to Firebase.
            firebase.child(key).setValue(value);
        }

        // Clear everything stored in HashMap when products are re added.
        saveProductCopy.clear();
    }

    // Handler for each options buttons.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        // The action bar will automatically handle clicks, if we specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Starts the settingsActivity and listen to the result.
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, RESULT_CODE_PREFERENCES);
        }

        // Starts the sharing of the shopping list.
        if (id == R.id.action_share) {
            StringBuilder sb = new StringBuilder();
            Intent intent = new Intent(Intent.ACTION_SEND);

            // Looping over each item in shopping list and then adding them to message.
            sb.append("Shopping list:\n");
            for (Product bagItem : adapter.getSnapshots()) {
                sb.append(bagItem.toString());

                // Creates a new line for each item.
                sb.append("\n");
            }

            // Converting everything into a string.
            String productList = sb.toString();
            intent.putExtra(Intent.EXTRA_TEXT, productList);
            intent.setType("text/plain");
            startActivity(intent);
        }

        // Clear individual item or multiple.
        if (id == R.id.action_delete) {

            // Creates a array of checked item and there position.
            SparseBooleanArray position = listView.getCheckedItemPositions();

            // Loop is created so that it is possible to iterate over each selected item in the list.
            // Getting the index of the items to have the ref of it.
            for (int i = adapter.getCount() - 1; i > -1; i--) {
                if (position.get(i)) {

                    // if the position is true we call the saveDeletedProducts function and handle the rest from there.
                    saveDeletedProducts(getMyAdapter().getRef(i).getKey(), adapter.getItem(i));
                    getMyAdapter().getRef(i).setValue(null);
                }
            }

            // Creating a snackbar so that we can select UNDO if we NOT want to delete the item.
            Snackbar snackbar = Snackbar
                .make(listView, "Deleted item..", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    // If UNDO is pressed then we call the re Add fucntion.
                    reAddSavedProducts();
                    adapter.notifyDataSetChanged();
                    }
                });

            // Checking the saveProductCopy is not empty before showing the snackbar.
            if (saveProductCopy.size() > 0) {
                snackbar.show();
            }

            listView.clearChoices();
            adapter.notifyDataSetChanged();
        }

        // Clear entire shopping list
        if (id == R.id.action_clear) {
            showDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}