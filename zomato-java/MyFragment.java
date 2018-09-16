package com.example.rakesh.zomato;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

public class MyFragment extends Fragment{

    RecyclerView recyclerView;
    LinearLayoutManager manager;
    MyAdapter myAdapter;
    MyTask myTask;
    ArrayList <Data> arrayList = new ArrayList <>();
    Data data;

    public class MyAdapter extends RecyclerView.Adapter <MyAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.row, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Data data = arrayList.get(position);
            holder.textView1.setText(data.getName());
            holder.textView2.setText(data.getLocality());
            holder.textView3.setText(data.getOffers());
            holder.ratingBar.setRating(Float.parseFloat(data.getRating()));
            Glide.with(getActivity()).load(data.getUrl()).into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView1, textView2, textView3;
            public RatingBar ratingBar;
            public CardView cardView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView1);
                textView1 = itemView.findViewById(R.id.textView1);
                textView2 = itemView.findViewById(R.id.textView2);
                textView3 = itemView.findViewById(R.id.textView3);
                ratingBar = itemView.findViewById(R.id.ratingBar1);
                cardView = itemView.findViewById(R.id.card);

                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        data = arrayList.get(getAdapterPosition());
                        String lat = data.getLat();
                        String lon = data.getLon();
                        String locality = data.getLocality();

                        Intent intent = new Intent(getActivity(), MapsActivity.class);
                        intent.putExtra("lat", lat);
                        intent.putExtra("lon", lon);
                        intent.putExtra("locality", locality);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    /*----------------------------------------------------------------------------*/

    public class MyTask extends android.os.AsyncTask <String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("A", "1");
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.i("A", "2");
            try {
                URL url = new URL(strings[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("user-key", "c1155ad27000c51b6c17302398646b7e");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String s = bufferedReader.readLine();
                StringBuilder sb = new StringBuilder();

                while (s != null) {
                    sb.append(s);
                    s = bufferedReader.readLine();
                }
                return sb.toString();

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("A", "IO exception" + e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Log.i("A", "3");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("A", "server responce = " + s);

            if (isConnected(getActivity())) {

                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray array = object.getJSONArray("nearby_restaurants");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject temp = array.getJSONObject(i);
                        JSONObject restaurant = temp.getJSONObject("restaurant");
                        String name = restaurant.getString("name");

                        JSONObject location = restaurant.getJSONObject("location");
                        String locality = location.getString("locality");
                        String lat = location.getString("latitude");
                        String lon = location.getString("longitude");

                        String offers = restaurant.getString("cuisines");
                        String url = restaurant.getString("thumb");

                        JSONObject usr = restaurant.getJSONObject("user_rating");
                        String rating = usr.getString("aggregate_rating");

                        // insert in array
                        data = new Data(url, name, locality, offers, rating, lat, lon);
                        arrayList.add(data);
                        myAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                buildDialog(getActivity()).show();
            }

        }

    }

    /*----------------------------------------------------------------------------*/

    public MyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(manager);

        // start async task
        myTask = new MyTask();
        // pass zomato web service url to async task for restaurents
        myTask.execute("https://developers.zomato.com/api/v2.1/geocode?lat=12.8984&lon=77.6179"); //zomato api documentation

        return view;
    }


    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else
            return false;
    }

    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please connect to the Internet");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        return builder;
    }




} // ends here