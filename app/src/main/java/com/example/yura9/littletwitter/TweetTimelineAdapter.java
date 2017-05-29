package com.example.yura9.littletwitter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.UserUtils;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TimelineResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yura9 on 5/23/2017.
 */

abstract class TweetTimelineAdapter extends RecyclerView.Adapter<TweetTimelineAdapter.TweetViewHolder>{
    private final Context context;
    List<Tweet> itemList;


    public TweetTimelineAdapter(Context context, Timeline<Tweet> timeline, List<Tweet> tweets){
        if (timeline == null)
            throw new IllegalArgumentException("Timeline must not be null");
        this.context = context;
        itemList = tweets;
    }


    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.tweet_custom, parent, false);
        TweetViewHolder holder = new TweetViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(TweetViewHolder holder, int position) {
        Tweet tweet = getItem(position);
        holder.author_full_name.setText(tweet.user.name);
        holder.tweet_text.setText(tweet.text);
        holder.setAuthorPhoto(tweet);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public abstract Tweet getItem(int position);

    public  class TweetViewHolder extends RecyclerView.ViewHolder{

        public ImageView author_avatar;
        public TextView author_full_name;
        public TextView tweet_text;
        public TweetViewHolder(View tweetView){
            super(tweetView);
            author_avatar = (ImageView) tweetView.findViewById(R.id.tweet_author_avatar);
            author_full_name = (TextView) tweetView.findViewById(R.id.tweet_author_full_name);
            tweet_text = (TextView) tweetView.findViewById(R.id.tweet_text);
        }

        public void setAuthorPhoto(Tweet tweet){
            final String url = UserUtils.getProfileImageUrlHttps(tweet.user, UserUtils.AvatarSize.REASONABLY_SMALL);
            if (url != null) {
                Picasso.with(context).load(url).transform(new PhotoTransformation()).into(author_avatar);
            }
        }
    }


}