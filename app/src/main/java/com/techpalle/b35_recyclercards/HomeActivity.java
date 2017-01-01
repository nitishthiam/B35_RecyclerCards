package com.techpalle.b35_recyclercards;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ArrayList<Hotel> hotels; //SOURCE
    RecyclerView recyclerView; //DESTINATION
    MyRecyclerAdapter myRecyclerAdapter; //ADAPTER
    MyTask myTask; //ASYNC TASK
    int pos;

    //USE THIS METHOD TO DISPLAY MENUS FROM CARD VIEW.
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.overflowmenu, popup.getMenu()); //1st par is ur menu xml
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }
    //menu items click listener
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener{
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.item1: //show restrnt on map
                    //OPEN MAP ACTIVITY, and pass latitude and longitude
                    Hotel hotel = hotels.get(pos);
                    Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
                    intent.putExtra("latitude", hotel.getLatitude());
                    intent.putExtra("longitude", hotel.getLongitude());
                    intent.putExtra("restaurant", hotel.getName());
                    startActivity(intent);
                    break;
                case R.id.item2: //search restrnt on google
                    //OPEN GOOGLE SEARCH ACTIVITY
                    break;
            }
            return true;
        }
    }

    public class MyRecyclerAdapter extends
            RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>{
        //create inner class here
        public class MyViewHolder extends RecyclerView.ViewHolder{
            public ImageView hotelImage, overflowImage;
            public TextView hotelName, hotelAddress, hotelDishes;
            public MyViewHolder(View itemView) {
                super(itemView);
                hotelImage = (ImageView) itemView.findViewById(R.id.imageView);
                overflowImage = (ImageView) itemView.findViewById(R.id.imageView2);
                hotelName = (TextView) itemView.findViewById(R.id.textView);
                hotelAddress = (TextView) itemView.findViewById(R.id.textView2);
                hotelDishes = (TextView) itemView.findViewById(R.id.textView3);
            }
        }
        @Override
        public MyRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.rox, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(v);
            return myViewHolder;
        }
        @Override
        public void onBindViewHolder(MyRecyclerAdapter.MyViewHolder holder,
                                     int position) {
            holder.overflowImage.setTag(position); //store postion
            //GET HOTEL OBJECT BASED ON POSITION FROM ARRAYLIST
            Hotel hotel = hotels.get(position);
            //APPLY DATA-BIND DATA ONTO HOLDER
            holder.hotelName.setText(hotel.getName());
            holder.hotelAddress.setText(hotel.getCity());
            holder.hotelDishes.setText(hotel.getCuisines());
            //SET OVERFLOW MENU
            holder.overflowImage.setImageResource(R.drawable.overflow);
            //on clicking overflow image, display popup
            holder.overflowImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get position of the item
                    //pos = recyclerView.getChildAdapterPosition(view);
                    pos = (int) ((ImageView)view).getTag();
                    Toast.makeText(HomeActivity.this,
                            "POSITION IS.."+pos, Toast.LENGTH_LONG).show();
                    //call show popup menu
                    showPopupMenu(view);
                }
            });
            //ASK GLIDER library to load hotel thumb nail image onto imagveiw
            Glide.with(HomeActivity.this)
                    .load(hotel.getImageUrl())
                    .into(holder.hotelImage);
        }
        @Override
        public int getItemCount() {
            return hotels.size();
        }
    }

    public class MyTask extends AsyncTask<String, Void, String>{
        //declare all variables
        URL myurl;
        HttpURLConnection connection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result;

        @Override
        protected String doInBackground(String... p1) {
            try {
                myurl = new URL(p1[0]);
                connection = (HttpURLConnection) myurl.openConnection();

                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("user-key", "8c35b43b80354924682997cff4a22a0b");
                connection.connect();

                inputStream = connection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                result = new StringBuilder();
                line = bufferedReader.readLine();
                while(line != null){
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            if(s == null){
                Toast.makeText(HomeActivity.this, "NETWORK ISSUE, FIX",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //WE WILL DO PARSING HERE
            try {
                JSONObject j = new JSONObject(s);
                JSONArray restuarants = j.getJSONArray("nearby_restaurants");
                for(int i=0; i<restuarants.length(); i++){
                    JSONObject temp = restuarants.getJSONObject(i);
                    JSONObject res = temp.getJSONObject("restaurant");
                    String name = res.getString("name");//gives hotel name
                    JSONObject location = res.getJSONObject("location");
                    String address = location.getString("address");//gives hotl addrs
                    String locality = location.getString("locality");//gvs locality
                    String city = location.getString("city"); //Gvs hotel city
                    String latitude = location.getString("latitude");//latitude
                    String longitude = location.getString("longitude");//longitude
                    String cuisines = res.getString("cuisines"); //famous dishes
                    String imageUrl = res.getString("thumb"); //icon of that restarnt
                    //PREPARE EMPTY HOTEL OBJECT - pass values to constructor
                    Hotel hotel = new Hotel(name,address,locality,city,
                                            latitude,longitude,cuisines,imageUrl);
                    //push this hotel object to arraylist
                    hotels.add(hotel);
                }
                //TELL TO ADAPTER
                myRecyclerAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            super.onPostExecute(s);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //intialize all variables
        hotels = new ArrayList<Hotel>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        //IMPROVE RECYCLERVIEW PERFORMANCE
        recyclerView.setHasFixedSize(true);

        myTask = new MyTask();
        myRecyclerAdapter = new MyRecyclerAdapter();

        //create grid layout manager
        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(this, 1); //for 2 cards per row
        //pass grid layout manager to recycler view
        recyclerView.setLayoutManager(gridLayoutManager);

        //set adapter to recycler view
        recyclerView.setAdapter(myRecyclerAdapter);

        //START asynctask, pass ZOMATO url
        myTask.execute("https://developers.zomato.com/api/v2.1/geocode?lat=12.8984&lon=77.6179");
    }
}
