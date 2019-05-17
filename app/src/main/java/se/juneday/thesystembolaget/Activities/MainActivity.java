package se.juneday.thesystembolaget.Activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.juneday.thesystembolaget.Fragments.FavoritesFragment;
import se.juneday.thesystembolaget.Fragments.HomeFragment;
import se.juneday.thesystembolaget.Fragments.SearchFragment;
import se.juneday.thesystembolaget.R;
import se.juneday.thesystembolaget.dialogs.ProductErrorDialog;
import se.juneday.thesystembolaget.domain.Product;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private List<Product> products;
    private ListView listView;
    private ArrayAdapter<Product> adapter;
    private ArrayAdapter<String> stringAdapter;
    private ArrayAdapter<String> favoriteAdapter;
    private List<String> latestSearch = new ArrayList<>();
    private List<String> items = new ArrayList<>();
    private List<String> favorites = new ArrayList<>();

    private static final String MIN_ALCO = "min_alcohol";
    private static final String MAX_ALCO = "max_alcohol";
    private static final String MIN_PRICE = "min_price";
    private static final String MAX_PRICE = "max_price";
    // private static final String TYPE = "product_group";
    private static final String NAME = "name";



    private void setupListView() {
        // look up a reference to the ListView object
        listView = findViewById(R.id.product_list);
                     listView.invalidate();

        // create an adapter (with the faked products)
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                products);

        // Set listView's adapter to the new adapter
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long l){
                final Product selected = (Product) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Product Information")
                        .setMessage(selected.toString())
                        .setPositiveButton("ok", new DialogInterface.OnClickListener()   {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setNegativeButton("Add to favorites", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                favorites.add(selected.toString());
                            }
                        });
                builder.show();

                //Toast.makeText(getApplicationContext(),"This is"+selected,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupLatestSearchView() {
        //listView.invalidate();
        //listView = findViewById(R.id.latestsearch_list);
                listView = findViewById(R.id.product_list); 
        stringAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, items);
        Log.d(LOG_TAG, " IDIOT latest search:" + items);

        listView.setAdapter(stringAdapter);
    }

    private void setupFavoriteView() {
        listView = findViewById(R.id.product_list);
        stringAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, favorites);
        Log.d(LOG_TAG, "cliked favorites");
        listView.setAdapter(stringAdapter);

    }


    private List<Product> jsonToProducts(JSONArray array) {
        Log.d(LOG_TAG, "jsonToProducts()");
        List<Product> productList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject row = array.getJSONObject(i);
                String name = row.getString("name");
                double alcohol = row.getDouble("alcohol");
                double price = row.getDouble("price");
                int volume = row.getInt("volume");

                Product m = new Product(name, alcohol, price, volume);
                productList .add(m);
                Log.d(LOG_TAG, " * " + m);
            } catch (JSONException e) {
                 // is ok since this is debug
                Log.d(LOG_TAG, "Å nej, JSON sket sig. " + e);
            }
        }
        return productList;
    }

 /*   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.place_search:
                Log.d(LOG_TAG, "user presssed SEARCH");
                showSearchDialog(); //
                break;
            default:
                Log.d(LOG_TAG, "uh oh ;)");
                break;
        }
        return true;
    }    */

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_placement, menu);

        return true;
    } */
     protected void onResume() {
         super.onResume();

         loadLatestSearch();
     }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        products = new ArrayList<>();

        loadLatestSearch();




        // setup listview (and friends)
    //    setupListView();
        

        BottomNavigationView bottomNav = findViewById(R.id.bottom_placement);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;
            switch(menuItem.getItemId()) {
                case R.id.place_home:
                    //return true;
                    selectedFragment = new HomeFragment();

                    Log.d(LOG_TAG, "user pressed home");
                    setupLatestSearchView();
                    break;
                case R.id.place_favorites:
                    selectedFragment = new FavoritesFragment();
                    Log.d(LOG_TAG, "user pressed favorites");
                    setupFavoriteView();
                    break;
                case R.id.place_search:
                    selectedFragment = new SearchFragment();
                    Log.d(LOG_TAG, "user presssed SEARCH");
                    setupListView();
                    showSearchDialog();

                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        }
    };

    // get the entered text from a view
    private String valueFromView(View inflated, int viewId) {
        return ((EditText) inflated.findViewById(viewId)).getText().toString();
    }

    // if the value is valid, add it to the map
    private void addToMap(Map<String, String> map, String key, String value) {
        if (value!=null && !value.equals("")) {
            map.put(key, value);
        }
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search products");
        final View viewInflated = LayoutInflater
                .from(this).inflate(R.layout.search_dialog, null);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Create a map to pass to the search method
                // The map makes it easy to add more search parameters with no changes in method signatures
                Map<String, String> arguments = new HashMap<>();

                // Add user supplied argument (if valid) to the map
                addToMap(arguments, MIN_ALCO, valueFromView(viewInflated, R.id.min_alco_input));
                addToMap(arguments, MAX_ALCO, valueFromView(viewInflated, R.id.max_alco_input));
                addToMap(arguments, MIN_PRICE, valueFromView(viewInflated, R.id.min_price_input));
                addToMap(arguments, MAX_PRICE, valueFromView(viewInflated, R.id.max_price_input));
            // addToMap(arguments, TYPE, valueFromView(viewInflated, R.id.product_group));
                addToMap(arguments, NAME, valueFromView(viewInflated, R.id.name));

                // Given the map, search for products and update the listview
                searchProducts(arguments);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOG_TAG, " User cancelled search");
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void searchProducts(Map<String, String> arguments) {
        // empty search string will give a lot of products :)
        String argumentString = "";

        // iterate over the map and build up a string to pass over the network
        for (Map.Entry<String, String> entry : arguments.entrySet())
        {
            // If first arg use "?", otherwise use "&"
            // E g:    ?min_alcohol=4.4&max_alcohol=5.4
            argumentString += (argumentString.equalsIgnoreCase("")?"?":"&")
                    + entry.getKey()
                    + "="
                    + entry.getValue();

       //     Product lsProduct = new Product(entry.getValue());

            latestSearch.add(entry.getValue());
            Log.d(LOG_TAG, "latestSearch" + latestSearch);

        Log.d(LOG_TAG, " arguments: " + entry.getValue());

            Log.d(LOG_TAG, " items " + items);

        
        }
        // print argument
        Log.d(LOG_TAG, " arguments: " + argumentString);

        Log.d(LOG_TAG, "items" + items);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://rameau.sandklef.com:9090/search/products/all/" + argumentString;
        Log.d(LOG_TAG, "Searching using url: " + url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray array) {
                        Log.d(LOG_TAG, "onResponse()");
                        products.clear();
                        products.addAll(jsonToProducts(array));
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
               // Log.d(LOG_TAG, " cause: " + error.getCause().getMessage());

                ProductErrorDialog ped = new ProductErrorDialog();
                ped.show(getSupportFragmentManager(), "product error dialog");

                showSearchDialog();
                saveLatestSearch();
                loadLatestSearch();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);


    }

    public void saveLatestSearch() {
        StringBuilder stringBuilder = new StringBuilder();
        for(String s : latestSearch) {
            stringBuilder.append(s);
            stringBuilder.append(",");
            }

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latestSearch", stringBuilder.toString());
        editor.commit();
        }

    public void loadLatestSearch() {
        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        String latestSearchString = settings.getString("latestSearch", "");
        String[] itemValue = latestSearchString.split(",");

        for (int i = 0; i < itemValue.length; i++) {
            items.add(itemValue[i]);
        }

        for (int i=0; i<items.size(); i++) {
            Log.d("listItem", items.get(i));
        }

    }

}
