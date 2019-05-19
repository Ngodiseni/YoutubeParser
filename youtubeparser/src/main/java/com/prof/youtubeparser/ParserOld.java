/*
*   Copyright 2016 Marco Gomiero
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*   
*/

package com.prof.youtubeparser;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prof.youtubeparser.models.videos.High;
import com.prof.youtubeparser.models.videos.Id;
import com.prof.youtubeparser.models.videos.Item;
import com.prof.youtubeparser.models.videos.Main;
import com.prof.youtubeparser.models.videos.Snippet;
import com.prof.youtubeparser.models.videos.Thumbnails;
import com.prof.youtubeparser.models.videos.Video;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This class parses video data from Youtube
 * <p>
 * Created by Marco Gomiero on 6/9/16.
 */
public class ParserOld extends AsyncTask<String, Void, String> {

    private OnTaskCompleted onComplete;
    public static final int ORDER_DATE = 1;
    public static final int ORDER_VIEW_COUNT = 2;


/*    @Deprecated
    public String generateRequest(String channelID, int maxResult, String key) {

        String urlString = "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=";
        urlString = urlString + channelID + "&maxResults=" + maxResult + "&order=date&key=" + key;
        return urlString;
    }*/


/*    public String generateRequest(String channelID, int maxResult, int orderType, String key) {

        String urlString = "https://www.googleapis.com/youtube/v3/search?&part=snippet&channelId=";
        String order = "";
        switch (orderType) {

            case ORDER_DATE:
                order = "date";
                break;

            case ORDER_VIEW_COUNT:
                order = "viewcount";
                break;

            default:
                break;
        }

        urlString = urlString + channelID + "&maxResults=" + maxResult + "&order=" + order + "&key=" + key;
        return urlString;
    }*/


   /* public String generateMoreDataRequest(String channelID, int maxResult, int orderType, String key, String nextToken) {

        String urlString = "https://www.googleapis.com/youtube/v3/search?pageToken=";
        String order = "";
        switch (orderType) {

            case ORDER_DATE:
                order = "date";
                break;

            case ORDER_VIEW_COUNT:
                order = "viewcount";
                break;

            default:
                break;
        }

        urlString = urlString + nextToken + "&part=snippet&channelId=" + channelID + "&maxResults="
                + maxResult + "&order=" + order + "&key=" + key;
        return urlString;
    }
*/

    public void onFinish(OnTaskCompleted onComplete) {
        this.onComplete = onComplete;
    }

    @Override
    protected String doInBackground(String... ulr) {

        Response response;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ulr[0])
                .build();

        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful())
                return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            onComplete.onError();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        if (result != null) {

            try {

                ArrayList<Video> videos = new ArrayList<>();

                Gson gson = new GsonBuilder().create();
                Main data = gson.fromJson(result, Main.class);

                //Begin parsing Json data
                List<Item> itemList = data.getItems();
                String nextToken = data.getNextPageToken();

                for (int i = 0; i < itemList.size(); i++) {

                    Item item = itemList.get(i);
                    Snippet snippet = item.getSnippet();

                    Id id = item.getId();

                    Thumbnails image = snippet.getThumbnails();

                    High high = image.getHigh();

                    String title = snippet.getTitle();
                    String videoId = id.getVideoId();
                    String imageLink = high.getUrl();
                    String sDate = snippet.getPublishedAt();

                    Locale.setDefault(Locale.getDefault());
                    TimeZone tz = TimeZone.getDefault();
                    Calendar cal = Calendar.getInstance(tz);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    sdf.setCalendar(cal);
                    cal.setTime(sdf.parse(sDate));
                    Date date = cal.getTime();

                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMMM yyyy");
                    String pubDateString = sdf2.format(date);

                    Video tempVideo = new Video(title, videoId, imageLink, pubDateString);
                    videos.add(tempVideo);
                }

                Log.i("YoutubeParser", "Youtube data parsed correctly!");
                onComplete.onTaskCompleted(videos, nextToken);

            } catch (Exception e) {

                e.printStackTrace();
                onComplete.onError();
            }
        } else
            onComplete.onError();
    }


    public interface OnTaskCompleted {
        void onTaskCompleted(ArrayList<Video> list, String nextPageToken);
        void onError();
    }
}