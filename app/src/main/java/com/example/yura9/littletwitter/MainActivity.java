package com.example.yura9.littletwitter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    /**
     * @param TWITTER_KEY a Consumer Key (API Key)
     * @param TWITTER_SECRET a Consumer Secret (API Secret)
     */
    private static final String TWITTER_KEY = "	wNCNyDrvtjAiSkygZhFUKlLKL";
    private static final String TWITTER_SECRET = "d28iZAHo9XfbcDyLseesRrB7QsDmqxGd0DiDXB82ryBXSogTkR";

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        listView = (ListView) findViewById(R.id.list);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        /**
         * @param userTimeLine which shows the @QWERTY user’s timeline of Tweets
         */
        final UserTimeline userTimeline = new UserTimeline.Builder()
                .screenName("QWERTY")
                .build();

        /**
         * @instance TweetTimelineListAdapter accepts any #Timeline and handles loading older Tweets and recycling views.
         * #Timelines use guest authentication automatically so no auth setup is needed.
         */
        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(this)
                .setTimeline(userTimeline)
                .build();

        listView.setAdapter(adapter);
    }
}
