package com.example.yura9.littletwitter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TimelineResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yura9 on 5/23/2017.
 */

class TweetTimelineAdapter extends RecyclerView.Adapter<TweetTimelineAdapter.TweetViewHolder>{
    private final Context context;
    static long CAPACITY = 200L;
    // timeline that next and previous items are loaded from
    final Timeline<Tweet> timeline;
    final TimelineHolder timelineHolder;
    List<Tweet> itemList;


    public TweetTimelineAdapter(Context context, Timeline<Tweet> timeline){
        if (timeline == null)
            throw new IllegalArgumentException("Timeline must not be null");
        this.context = context;
        this.timeline = timeline;
        timelineHolder = new TimelineHolder();
        itemList = new ArrayList<>();
        refresh(null);

    }


    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Tweet tweet = getItem(0);
        CompactTweetView tweetView = new CompactTweetView(context, tweet);
        return new TweetViewHolder(tweetView);
    }

    @Override
    public void onBindViewHolder(TweetViewHolder holder, int position) {
        Tweet tweet = getItem(position);
        CompactTweetView tweetView = (CompactTweetView) holder.itemView;
        tweetView.setTweet(tweet);

    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }


    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    static final class TweetViewHolder extends RecyclerView.ViewHolder{

        public TweetViewHolder(CompactTweetView tweetView){
            super(tweetView);
        }
    }

    boolean withinMaxCapacity(){return itemList.size() < CAPACITY;}

    /**
     * Triggers loading the latest items. If items are
     * received, they replace existing items.
     */
    public void refresh(Callback<TimelineResult<Tweet>> developerCb){

        //reset timelineHolder cursors to be null, loadNext will get latest items
        timelineHolder.resetCursors();
        loadNext(timelineHolder.positionForNext(), new RefreshCallback(developerCb, timelineHolder));
    }

    /**
     * Triggers loading previous items.
     */
    public void previous(){
        loadPrevious(timelineHolder.positionForPrevious(), new PreviousCallback(timelineHolder));
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

    /**
     * Gets the data item associated with the specified position in the data set.
     */
    public Tweet getItem(int position){
        if (isLastPosition(position)) {
            previous();
        }
        return itemList.get(position);}

    boolean isLastPosition(int position){ return position == (itemList.size() - 1);}


    /**
     * TimelineDelegate.DefaultCallback is a Callback which handles setting requestInFlight to
     * false on both success and failure and calling through to a wrapped developer Callback.
     * Subclass methods must call through to the parent method after their custom implementation.
     */
    class DefaultCallback extends Callback<TimelineResult<Tweet>>{
        final Callback<TimelineResult<Tweet>> developerCallback;
        final TimelineHolder timelineHolder;

        DefaultCallback(Callback<TimelineResult<Tweet>> developerCb, TimelineHolder timelineHolder){
            this.developerCallback = developerCb;
            this.timelineHolder = timelineHolder;
        }

        @Override
        public void success(Result<TimelineResult<Tweet>> result) {
            timelineHolder.finishTimeLineRequest();
            if (developerCallback != null){
                developerCallback.success(result);
            }
        }

        @Override
        public void failure(TwitterException exception) {
            timelineHolder.finishTimeLineRequest();
            if (developerCallback != null){
                developerCallback.failure(exception);
            }
        }
    }

    /**
     * Handles receiving next timeline items. Prepends received items to listItems, updates the
     * scrollStateHolder nextCursor, and calls notifyDataSetChanged.
     */
    class NextCallback extends DefaultCallback {

        NextCallback(Callback<TimelineResult<Tweet>> developerCb, TimelineHolder timelineHolder){
            super(developerCb, timelineHolder);
        }

        @Override
        public void success(Result<TimelineResult<Tweet>> result) {
            if (result.data.items.size() > 0){
                final ArrayList<Tweet> receivedItems = new ArrayList<>(result.data.items);
                receivedItems.addAll(itemList);
                itemList = receivedItems;
                notifyDataSetChanged();
                timelineHolder.setNextCursor(result.data.timelineCursor);
            }

            super.success(result);
        }
    }

    /**
     * Handles receiving latest timeline items, clears listItems, and sets received items.
     */
    class RefreshCallback extends NextCallback {

        RefreshCallback(Callback<TimelineResult<Tweet>> developerCb,
                        TimelineHolder timelineStateHolder) {
            super(developerCb, timelineStateHolder);
        }

        @Override
        public void success(Result<TimelineResult<Tweet>> result) {
            if (result.data.items.size() > 0) {
                itemList.clear();
            }
            super.success(result);
        }
    }

    /**
     * Handles appending listItems and updating the scrollStateHolder previousCursor.
     */
    class PreviousCallback extends DefaultCallback {

        PreviousCallback(TimelineHolder timelineStateHolder) {
            super(null, timelineStateHolder);
        }

        @Override
        public void success(Result<TimelineResult<Tweet>> result) {
            if (result.data.items.size() > 0) {
                itemList.addAll(result.data.items);
                notifyDataSetChanged();
                timelineHolder.setPreviousCursor(result.data.timelineCursor);
            }
            // do nothing when zero items are received. Subsequent 'next' call does not change.
            super.success(result);
        }
    }
}