package sfsu.csc413.foodcraft;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import java.util.List;


public class IngredientSearch extends AppCompatActivity
        implements SearchView.OnQueryTextListener, SearchableIngredientFragment.OnFragmentInteractionListener {

    private Toolbar toolbar;
    static ArrayList<String> selectedFoods = new ArrayList<>();
    private static final String SELECTED_FOODS_ARRAY = "sfsu.csc413foodcraft.SELECTED_FOODS_ARRAY";

    // Activity self reference
    IngredientSearch selfReference;

    //ListViews to display ArrayLists
    static ListView lvSelectedIngredients;

    //ArrayAdapters for both ListViews
    //ArrayAdapter<String> lvIngredientSearchAdapter;
    static CustomAdapter<String> lvSelectedIngredientsAdapter;

    // Buttons
    Button searchButton;
    Button scanButton;

    //SearchView
    static SearchView searchView;

    UPCFragment upcfrag = new UPCFragment();
    static SearchableIngredientFragment searchableFrag = new SearchableIngredientFragment();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_holder, searchableFrag);
        transaction.show(searchableFrag);
        transaction.commit();

        setContentView(R.layout.activity_ingredient_search);
        new Eula(this).show();
        new LocationServices(this).show();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        searchButton = (Button) findViewById(R.id.foodcraft_search_button);
        scanButton = (Button) findViewById(R.id.buttonScanUPC);
        if (toolbar != null) {
            setActionBar(toolbar);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);

            // https://stackoverflow.com/questions/16240605/change-action-bar-title-color -- Brilliant!
            getActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Select Ingredients</font>"));

            getActionBar().setElevation(7);
        }
        selfReference = this;

        //initialize selected ingredient list, non-searchable
        lvSelectedIngredientsAdapter = new CustomAdapter<>(this, R.layout.list_item_selected_ingredients, R.id.selected_item, selectedFoods);
        lvSelectedIngredients = (ListView) findViewById(R.id.selectedIngredientsListView);

        // on click searchButton
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeSearchRequest searchRequest = new RecipeSearchRequest(getApplicationContext(), selfReference);
                searchRequest.run(selectedFoods);
            }
        });

        //initialize SearchView for searchable ingredient list
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.menu_item_search);
        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("eggs, bacon, etc.");
        searchView.setSubmitButtonEnabled(true);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void addselectedFoods(List<UPCObject> item, boolean cached) {
        togglePhotoFragment(upcfrag.getView());
        if (selectedFoods.size() == 0) {
            lvSelectedIngredients.setAdapter(lvSelectedIngredientsAdapter);
        }
        if (item.size() == 1 && cached && item.get(0).product_title != null) {
            selectedFoods.add(item.get(0).product_title.toLowerCase());
            lvSelectedIngredientsAdapter.notifyDataSetChanged();
        } else if (item.size() == 1 && !cached) {
            //Prompt edit string of the title, add to cache
            //Confirmation of title
            SingleIngredientAlert(item.get(0));
        } else if (item.size() > 1 && !cached) {
            //// TODO: 12/1/15  fix radio button selection
            //Radio button selection of each item, and 'Other' selection that adds custom title to database
            MultipleIngredientAlert(item);
        }
    }

    private void SingleIngredientAlert(final UPCObject product) {
        LayoutInflater li = getLayoutInflater();
        final View promptsView = li.inflate(R.layout.single_ingredient_alert, null);
        EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        userInput.setText(product.original_title, TextView.BufferType.EDITABLE);

        new AlertDialog.Builder(this.selfReference)
                .setView(promptsView)
                .setTitle("Unable to detect ingredient!")
                .setMessage("Please confirm the ingredient that you scanned. This will be saved for the next time you scan this item.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                        selectedFoods.add(userInput.getText().toString().toLowerCase());
                        lvSelectedIngredientsAdapter.notifyDataSetChanged();
                        upcfrag.addtoDatabase(product.code, userInput.getText().toString(), selfReference.getApplicationContext());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void MultipleIngredientAlert(final List<UPCObject> matches) {
        LayoutInflater li = getLayoutInflater();
        View promptsView = li.inflate(R.layout.multiple_ingredient_alert, null);
        RelativeLayout layout_root = (RelativeLayout) promptsView.findViewById(R.id.multiple_layout_root);
        final RadioGroup radioGroup = new RadioGroup(this.selfReference);
        layout_root.addView(radioGroup);
        int counter = 0;
        for (UPCObject item : matches) {
            RadioButton radioButtonView = new RadioButton(this.selfReference);
            radioButtonView.setText(item.product_title);
            radioGroup.addView(radioButtonView, counter);
            counter++;
        }
        RadioButton radioButtonView = new RadioButton(this.selfReference);
        radioButtonView.setText("Not Listed");
        radioGroup.addView(radioButtonView, counter);
        int selected;
        new AlertDialog.Builder(this.selfReference)
                .setView(promptsView)
                .setTitle("Unable to detect ingredient!")
                .setMessage("Please select the ingredient that you scanned. This will be saved for the next time you scan this item.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int radioButtonID = radioGroup.getCheckedRadioButtonId();
                        RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioButtonID);
                        String selection = (String) radioButton.getText();
                        if (selection.equals("Not Listed")) {
                            SingleIngredientAlert(matches.get(0));
                        } else {
                            selectedFoods.add(selection.toLowerCase());
                            lvSelectedIngredientsAdapter.notifyDataSetChanged();
                            upcfrag.addtoDatabase(matches.get(0).code, selection, selfReference.getApplicationContext());
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void clearSelectedIngredientsList() {
        selectedFoods.clear();
        lvSelectedIngredientsAdapter.notifyDataSetChanged();
    }

    protected void launchSearchResultsActivity(ArrayList<Recipe> recipes) {

        Log.i("LAUNCH_RESULTS", "Started with recipes list of length " + recipes.size());

        // Add ingredients I always have to selectedFoods
        android.content.SharedPreferences sharedPrefs = getSharedPreferences("myprefs", MODE_PRIVATE);

        if (sharedPrefs.getBoolean("salt", false)) {
            selectedFoods.add("salt");
        }
        if (sharedPrefs.getBoolean("pepper", false)) {
            selectedFoods.add("pepper");
        }
        if (sharedPrefs.getBoolean("sugar", false)) {
            selectedFoods.add("sugar");
        }
        if (sharedPrefs.getBoolean("butter", false)) {
            selectedFoods.add("butter");
        }
        if (sharedPrefs.getBoolean("eggs", false)) {
            selectedFoods.add("eggs");
        }
        if (sharedPrefs.getBoolean("water", false)) {
            selectedFoods.add("water");
        }

        Intent intent = new Intent(this, CardviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CardviewActivity.RECIPE_SEARCH_RESULTS, recipes);
        bundle.putStringArrayList(CardviewActivity.SELECTED_FOODS_ARRAY, selectedFoods);
        intent.putExtras(bundle);

        //reset lists in case user backtracks to this activity
        selectedFoods.clear();
        lvSelectedIngredientsAdapter.notifyDataSetChanged();
        searchableFrag.refreshArray();

        Log.i("LAUNCH_RESULTS", "Starting new activity");
        selfReference.startActivity(intent);

    }

    public void togglePhotoFragment(View view) {
        if (!upcfrag.isAdded()) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.fragment_holder, upcfrag);
            transaction.show(upcfrag);
            transaction.hide(searchableFrag);
            scanButton.setHint("Cancel");
            transaction.commit();
        } else if (upcfrag.isHidden()) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            upcfrag.onResume();
            transaction.show(upcfrag);
            transaction.hide(searchableFrag);
            scanButton.setHint("Cancel");
            transaction.commit();
        } else if (upcfrag.isVisible()) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.show(searchableFrag);
            transaction.hide(upcfrag);
            scanButton.setHint("Scan UPC");
            upcfrag.onPause();
            transaction.commit();
        }
    }

    public void createToast(String text) {
        // Modified from https://developer.android.com/guide/topics/ui/notifiers/toasts.html
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent(this, SharedPreferences.class);
        startActivity(intent);
        return true;
    }


    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "IngredientSearch Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://sfsu.csc413.foodcraft/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "IngredientSearch Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://sfsu.csc413.foodcraft/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private static ArrayList<String> insertAlphabetized (String string, ArrayList<String> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            int compare = string.compareTo(arrayList.get(i));
            if (compare < 0) {
                arrayList.add(i, string);
                return arrayList;
            }
        }

        arrayList.add(string);
        return  arrayList;
    }

    public static void addIngredientBack (String string) {
        ArrayList<String> alphabetizedList = insertAlphabetized(string, searchableFrag.getSearchableIngredients());
        searchableFrag.setSearchableIngredients(alphabetizedList);
    }

}

class CustomAdapter<T> extends ArrayAdapter {

    ArrayList<String> list;

    public CustomAdapter(Context context, int layoutContainerId, int layoutResourceId, ArrayList<String> list) {
        super(context, layoutContainerId, layoutResourceId, list);

        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup PARENT) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_selected_ingredients, null);
        }

        String string = (String) getItem(position);

        TextView t = (TextView) v.findViewById(R.id.selected_item);

        t.setText(string);

            ImageButton button = (ImageButton) v.findViewById(R.id.delete);
            button.setTag(position);
            button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int pos = (int) view.getTag();
                IngredientSearch.addIngredientBack(list.get(pos));
                list.remove(pos);
                CustomAdapter.this.notifyDataSetChanged();

            }
        });

        return v;

    }

}

