package se.juneday.thesystembolaget;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



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

import se.juneday.thesystembolaget.DB.DatabaseHelper;
import se.juneday.thesystembolaget.Fragments.FavoritesFragment;
import se.juneday.thesystembolaget.Fragments.HomeFragment;
import se.juneday.thesystembolaget.Fragments.SearchFragment;
import se.juneday.thesystembolaget.dialogs.ProductErrorDialog;
import se.juneday.thesystembolaget.domain.Product;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private List<Product> products;
    private ListView listView;
    private ArrayAdapter<Product> adapter;
    private ArrayAdapter<String> stringAdapter;
    private ArrayAdapter<Product> favoriteAdapter;
    private List<String> latestSearch;
    private List<Product> favorites;

    private static final String MIN_ALCO = "min_alcohol";
    private static final String MAX_ALCO = "max_alcohol";
    private static final String MIN_PRICE = "min_price";
    private static final String MAX_PRICE = "max_price";
    private static final String NAME = "name";
    private DatabaseHelper myDB;


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
                builder.setTitle(selected.name())
                        .setMessage(selected.price() + " SEK\n" + selected.alcohol() + " %\n" +
                                selected.volume() + " ml")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener()   {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setNegativeButton("Add to favorites", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addFavorites(selected.name(), selected.price(), selected.alcohol(), selected.volume());
                            }
                        });
                builder.show();
            }
        });
    }

    private void setupLatestSearchView() {
        listView = findViewById(R.id.product_list);
        stringAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, latestSearch);

        listView.setAdapter(stringAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long l){
                final String selected = (String) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Latest queries")
                        .setMessage(selected)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener()   {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setNegativeButton("Remove from latest search", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteLatestSearch(selected);
                                latestSearch.remove(selected);
                                setupLatestSearchView();
                            }
                        });
                builder.show();
            }
        });

    }

    private void setupFavoriteView() {
        listView = findViewById(R.id.product_list);
        favoriteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, favorites);
        Log.d(LOG_TAG, "clicked favorites");
        listView.setAdapter(favoriteAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long l){
                final Product selected = (Product) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(selected.name())
                        .setMessage(selected.price() + " SEK\n" + selected.alcohol() + " %\n" +
                                selected.volume() + " ml")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener()   {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(LOG_TAG, "favorites" + selected.nr());
                            }
                        })
                        .setNegativeButton("Remove from favorites", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFavorites(selected);
                                favorites.remove(selected);
                                setupFavoriteView();
                            }
                        });
                builder.show();
            }
        });

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        products = new ArrayList<>();

        myDB = new DatabaseHelper(this);
        getLatestSearch();

        setupLatestSearchView();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_menu);
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
                    getLatestSearch();
                    Log.d(LOG_TAG, "user pressed home");
                    setupLatestSearchView();
                    break;
                case R.id.place_favorites:
                    selectedFragment = new FavoritesFragment();
                    Log.d(LOG_TAG, "user pressed favorites");
                    getFavorites();
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

            String keyToUpperCase = entry.getKey().substring(0, 1).toUpperCase();
            String key = keyToUpperCase + entry.getKey().substring(1).replace("_", " ");
            String query = key + ": " + entry.getValue();
            addLatestSearch(query);
        }
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

                ProductErrorDialog ped = new ProductErrorDialog();
                ped.show(getSupportFragmentManager(), "product error dialog");

                showSearchDialog();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);


    }

    public void addLatestSearch(String entry){
        boolean isInserted = myDB.insertDataLatest(entry);
        if(isInserted = true){
            Log.d(LOG_TAG, "Saved to latest search");
        }else{
            Log.d(LOG_TAG, "Couldn't save to latest search");
        }
    }

    public void addFavorites(String name, double price, double alcohol, int volume){
        boolean isInserted = myDB.insertDataFavorites(name, price,
                alcohol, volume);
        if(isInserted = true){
            Toast.makeText(MainActivity.this, "Saved to favorites",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(MainActivity.this, "Something went wrong while saving",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void getFavorites(){
        favorites = new ArrayList<>();
        Cursor result = myDB.getAllDataFavorites();
        if(result.getCount() == 0) {
            Log.d(LOG_TAG, "The database was empty...");
        }else{
            while(result.moveToNext()){
                Product product = new Product.Builder()
                        .name(result.getString(0))
                        .price(result.getDouble(1))
                        .alcohol(result.getDouble(2))
                        .volume(result.getInt(3))
                        .nr(result.getInt(4))
                        .build();
                favorites.add(product);
            }
        }
    }

    public void getLatestSearch(){
        latestSearch = new ArrayList<>();
        Cursor result = myDB.getAllDataLatest();
        if(result.getCount() == 0) {
            Log.d(LOG_TAG, "The database was empty...");
        }else{
            while(result.moveToNext()){
                latestSearch.add(result.getString(0));
            }
        }
    }

    public void deleteFavorites(Product product){
        Integer deletedRows = myDB.deleteDataFavorites(product);
        if(deletedRows > 0){
            Toast.makeText(MainActivity.this, "Deleted from favorites",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(MainActivity.this, "Something went wrong while",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void deleteLatestSearch(String string){
        Integer deletedRows = myDB.deleteDataLatest(string);
        if(deletedRows > 0){
            Log.d(LOG_TAG, "Deleted from latest search");
        }else{
            Log.d(LOG_TAG, "Something went wrong when deleting");
        }
    }
}
