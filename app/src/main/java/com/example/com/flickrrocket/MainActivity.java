package com.example.com.flickrrocket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    static String json = "";
    HorizontalScrollView thumbnails;
    ImageSwitcher imageSwitcher;
    Button searchButton;
    EditText searchBar;
    JSONArray photos;
    int ref = 0;
    Context globalContext;

    public String readInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream, "UTF-8"));
        String tmp;
        StringBuilder sb = new StringBuilder();
        while ((tmp = reader.readLine()) != null) {
            sb.append(tmp).append("\n");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        reader.close();
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        searchBar = (EditText) findViewById(R.id.editText);
        searchButton = (Button) findViewById(R.id.button);
        final InputMethodManager imManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        globalContext = this;
        searchBar.setText("Rocket");        //setting initial search to rocket
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);        //animation set from left to right
        imageSwitcher.setInAnimation(in);

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView myView = new ImageView(getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);        //aligning image
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
                return myView;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    imManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);// hiding keyboard
                    ref = 0;
                    photos = null;
                    getImageURLS();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        imageSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                imManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                ref++;
                convertJSON();
            }
        });

        try {
            ref =0;
            getImageURLS();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertJSON(){
        try {
            if(photos == null){
                JSONObject root = new JSONObject(json);
                JSONObject data = root.getJSONObject("photos");
                photos = data.getJSONArray("photo");
            }

            JSONObject image = photos.getJSONObject(ref);
            String imgurl = "http://farm" + image.get("farm") + ".static.flickr.com/" + image.get("server") + "/" + image.get("id") + "_" + image.get("secret") + "_m.jpg";
            Log.d("Varun", "The response is: " + imgurl);
            new DownloadImageTask().execute(imgurl);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("Varun", "The response is: " + urls[0]);
                InputStream is = conn.getInputStream();
                String json1 = readInputStream(is);
                Log.d("Varun", "The response is: " + json1);
                return json1;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
            json = feed;
            convertJSON();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        /*public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }*/

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                InputStream is = conn.getInputStream();
                mIcon11 = BitmapFactory.decodeStream(is);//response to image
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Drawable d = new BitmapDrawable(getResources(), result);
            imageSwitcher.setImageDrawable(d);
        }
    }

    public void getImageURLS() throws IOException {
        String searchText = searchBar.getText().toString().replaceAll("[\\s]","");
        String myurl = "https://api.flickr.com/services/rest/?format=json&sort=random&method=flickr.photos.search&tags="+searchText+"&tag_mode=all&api_key=0e2b6aaf8a6901c264acb91f151a3350&nojsoncallback=1";
        new RetrieveFeedTask().execute(myurl);
        Log.d("Varun", "The response is: " + json);
    }
}
