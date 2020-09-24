package com.rajaryan.internship;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadrosid.svgloader.SvgLoader;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener;
import com.leo.simplearcloader.SimpleArcLoader;
import com.squareup.picasso.Picasso;

import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    EditText edtSearch;
    ListView listView;
    public static List<CountryModel> countryModelsList = new ArrayList<>();
    CountryModel countryModel;
    MyCustomAdapter myCustomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtSearch = findViewById(R.id.edtSearch);
        listView = findViewById(R.id.listView);


        getSupportActionBar().setTitle("Affected Countries");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fetchData();
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                myCustomAdapter.getFilter().filter(s);
                myCustomAdapter.notifyDataSetChanged();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void fetchData() {

        String url  = "https://restcountries.eu/rest/v2/all";



        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for(int i=0;i<jsonArray.length();i++){

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String name = jsonObject.getString("name");
                                String capital = jsonObject.getString("capital");
                                String region = jsonObject.getString("region");
                                String subregion = jsonObject.getString("subregion");
                                String population = jsonObject.getString("population");
                                String borders = jsonObject.getString("borders");
                                String languages = jsonObject.getString("languages");



                                String flagUrl = jsonObject.getString("flag");

                                countryModel = new CountryModel(flagUrl,name,capital,region,subregion,population,borders,languages);
                                countryModelsList.add(countryModel);


                            }

                            myCustomAdapter = new MyCustomAdapter(MainActivity.this,countryModelsList);
                            listView.setAdapter(myCustomAdapter);







                        } catch (JSONException e) {
                            e.printStackTrace();

                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);


    }
    public class MyCustomAdapter extends ArrayAdapter<CountryModel> {

        private Context context;
        private List<CountryModel> countryModelsList;
        private List<CountryModel> countryModelsListFiltered;

        public MyCustomAdapter( Context context, List<CountryModel> countryModelsList) {
            super(context, R.layout.list_custom_item,countryModelsList);

            this.context = context;
            this.countryModelsList = countryModelsList;
            this.countryModelsListFiltered = countryModelsList;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_custom_item,null,true);
            TextView tvCountryName = view.findViewById(R.id.tvCountryName);
            ImageView imageView = view.findViewById(R.id.imageFlag);

            tvCountryName.setText(countryModelsListFiltered.get(position).getName());
            Uri uri = Uri.parse("https://de.wikipedia.org/wiki/Scalable_Vector_Graphics#/media/File:SVG_logo.svg");
            GlideToVectorYou
                    .init()
                    .with(MainActivity.this)
                    .withListener(new GlideToVectorYouListener() {
                        @Override
                        public void onLoadFailed() {
                            Toast.makeText(context, "Load failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResourceReady() {
                            Toast.makeText(context, "Image ready", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .load(uri, imageView);
            return view;
        }

        @Override
        public int getCount() {
            return countryModelsListFiltered.size();
        }

        @Nullable
        @Override
        public CountryModel getItem(int position) {
            return countryModelsListFiltered.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults filterResults = new FilterResults();
                    if(constraint == null || constraint.length() == 0){
                        filterResults.count = countryModelsList.size();
                        filterResults.values = countryModelsList;

                    }else{
                        List<CountryModel> resultsModel = new ArrayList<>();
                        String searchStr = constraint.toString().toLowerCase();

                        for(CountryModel itemsModel:countryModelsList){
                            if(itemsModel.getName().toLowerCase().contains(searchStr)){
                                resultsModel.add(itemsModel);

                            }
                            filterResults.count = resultsModel.size();
                            filterResults.values = resultsModel;
                        }


                    }

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    countryModelsListFiltered = (List<CountryModel>) results.values;
                    MainActivity.countryModelsList = (List<CountryModel>) results.values;
                    notifyDataSetChanged();

                }
            };
            return filter;
        }
    }

}
