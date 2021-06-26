package com.example.flixster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixster.models.Movie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;
import okhttp3.Headers;


public class MovieDetailsActivity extends AppCompatActivity {
    public static final String TAG = "MovieDetailsActivity";
    Movie movie;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView backdrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        rbVoteAverage = findViewById(R.id.rbVoteAverage);
        backdrop = findViewById(R.id.backdrop);

        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        Glide.with(this).load(movie.getBackdropPath()).into(backdrop);
        backdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Use async HTTP client to extract JSON array information from the database of movie videos
                AsyncHttpClient client = new AsyncHttpClient();
                final String VIDEOS_URL = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/videos?api_key="+ getResources().getString(R.string.moviedb_api_key) +"&language=en-US";
                client.get(VIDEOS_URL, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Headers headers, JSON json) {
                        Log.d(TAG, "onSuccess");
                        JSONObject jsonObject = json.jsonObject;
                        try {
                            JSONArray results = jsonObject.getJSONArray("results");
                            JSONObject firstVideo = results.getJSONObject(0);
                            final String videoId = firstVideo.getString("key");
                            Log.i(TAG, "videoId: " + videoId);

                            //Play video as movie trailer if a video id was found
                            Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                            intent.putExtra("key", videoId);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Log.e(TAG, "Hit json exception", e);
                        }
                    }

                    @Override
                    public void onFailure(int i, Headers headers, String s, Throwable throwable) {
                        Log.d(TAG, "onFailure");
                    }
                });

            }
        });

        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage / 2.0f);
    }
}