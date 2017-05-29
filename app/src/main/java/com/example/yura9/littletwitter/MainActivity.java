package com.example.yura9.littletwitter;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    /**
     * @param TWITTER_KEY a Consumer Key (API Key)
     * @param TWITTER_SECRET a Consumer Secret (API Secret)
     */
    private static final String TWITTER_KEY = "	wNCNyDrvtjAiSkygZhFUKlLKL";
    private static final String TWITTER_SECRET = "d28iZAHo9XfbcDyLseesRrB7QsDmqxGd0DiDXB82ryBXSogTkR";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
   // private final Context context;
    static long CAPACITY = 200L;
    // timeline that next and previous items are loaded from
    //private Timeline<Tweet> timeline;
    private  UserTimeline timeline;
    private TimelineHolder timelineHolder;
    private TweetTimelineAdapter adapter;
    private List<Tweet> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));


        /**
         * @param userTimeLine which shows the @QWERTY user’s timeline of Tweets
         */
        timeline = new UserTimeline.Builder()
                .screenName("QWERTY")
                .build();

        timelineHolder = new TimelineHolder();
        itemList = new ArrayList<>();
        refresh();

        adapter = new TweetTimelineAdapter(MainActivity.this, timeline, itemList){
            @Override
            public Tweet getItem(int position) {
                if (isLastPosition(position)) {
                    previous();
                }
                return itemList.get(position);}
            };

        mRecyclerView.setAdapter(adapter);


    }

    boolean withinMaxCapacity(){return itemList.size() < CAPACITY;}

    /**
     * Triggers loading the latest items. If items are
     * received, they replace existing items.
     */
    public void refresh(){
        //reset timelineHolder cursors to be null, loadNext will get latest items
        timelineHolder.resetCursors();
        loadNext(timelineHolder.positionForNext(), new RefreshCallback());
    }

    /**
     * Triggers loading previous items.
     */
    public void previous(){
        loadPrevious(timelineHolder.positionForPrevious(), new PreviousCallback());
    }

    /**
     * Checks the capacity and sets requestInFlight before calling timeline.next.
     */
    void loadNext(Long minPosition, Callback<TimelineResult<Tweet>> callback){
        if (withinMaxCapacity()){
            if (timelineHolder.startTimelineRequest()){
                timeline.next(minPosition, callback);
            }else {
                callback.failure(new TwitterException("Request already in flight"));
            }
        }else {
            callback.failure(new TwitterException("Max capacity reached"));
        }
    }

    /**
     * Checks the capacity and sets requestInFlight before calling timeline.previous.
     */
    void loadPrevious(Long maxPosition, Callback<TimelineResult<Tweet>> callback){
        if (withinMaxCapacity()){
            if (timelineHolder.startTimelineRequest()){
                timeline.previous(maxPosition, callback);
            }else {
                callback.failure(new TwitterException("Request already in flight"));
            }
        }else {
            callback.failure(new TwitterException("Max capacity reached"));
        }
    }
    boolean isLastPosition(int position){ return position == (itemList.size() - 1);}

    /**
     * Handles receiving latest timeline items, clears listItems, and sets received items.
     */
    class RefreshCallback extends Callback<TimelineResult<Tweet>> {

        @Override
        public void success(Result<TimelineResult<Tweet>> result) {
            if (result.data.items.size() > 0) {
                timelineHolder.finishTimeLineRequest();
                itemList.clear();
                itemList.addAll(result.data.items);
                adapter.notifyDataSetChanged();
                timelineHolder.setNextCursor(result.data.timelineCursor);

            }

        }

        @Override
        public void failure(TwitterException exception) {
            timelineHolder.finishTimeLineRequest();
        }
    }

    /**
     * Handles appending listItems and updating the scrollStateHolder previousCursor.
     */
    class PreviousCallback extends Callback<TimelineResult<Tweet>> {

        @Override
        public void success(Result<TimelineResult<Tweet>> result) {
            if (result.data.items.size() > 0) {
                timelineHolder.finishTimeLineRequest();
                itemList.addAll(result.data.items);
                adapter.notifyDataSetChanged();
                timelineHolder.setPreviousCursor(result.data.timelineCursor);
            }
        }

        @Override
        public void failure(TwitterException exception) {
            timelineHolder.finishTimeLineRequest();
        }
    }



}
