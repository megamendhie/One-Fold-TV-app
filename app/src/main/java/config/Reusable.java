package config;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.format.DateFormat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Reusable {

    public static int getType(String type){
        switch (type){
            case "all":
                return 1;
            case "Word":
                return 2;
            case "praise":
                return 3;
            case "youth":
                return 4;
            case "gifted":
                return 5;
            case "report":
                return 6;
            default:
                return 0;
        }
    }

    public static int getField(String field){
        switch (field){
            case "uyo":
                return 1;
            case "ikot_ekpene":
                return 2;
            case "eket":
                return 3;
            case "oron":
                return 4;
            case "ikot_abasi":
                return 5;
            case "etinan":
                return 6;
            case "ikono":
                return 7;
            default:
                return 0;
        }
    }

    public static void grabImage(String photoUrl){
        ImageDownloader getBitmapfromUrl = new ImageDownloader();
        getBitmapfromUrl.execute(photoUrl);
    }


    static class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //uploadImage(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... imageUrl) {
            //link to image
            String link = imageUrl[0].replace("s96-c/photo.jpg", "s400-c/photo.jpg");

            //open Http connection to image and retrieve image
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                //convert image to bitmap
                return BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public  static  boolean getNetworkAvailability(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


}
