package com.example.yura9.littletwitter;

import com.twitter.sdk.android.tweetui.TimelineCursor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yura9 on 5/25/2017.
 */

class TimelineHolder {

    TimelineCursor nextCursor;
    TimelineCursor previousCursor;

    public final AtomicBoolean requestInFlight = new AtomicBoolean();

    public TimelineHolder(){

    }

    public void resetCursors(){
        nextCursor = null;
        previousCursor = null;
    }

    public Long positionForNext(){return nextCursor == null ? null : nextCursor.maxPosition; }

    public Long positionForPrevious(){ return  previousCursor == null? null : previousCursor.minPosition; }

    public void setNextCursor(TimelineCursor timelineCursor){
        nextCursor = timelineCursor;
        setCursorsIfNull(timelineCursor);
    }

    public void setPreviousCursor(TimelineCursor timelineCursor){
        previousCursor = timelineCursor;
        setCursorsIfNull(timelineCursor);
    }

    public void setCursorsIfNull(TimelineCursor timelineCursor){
        if (nextCursor == null){
            nextCursor = timelineCursor;
        }
        if (previousCursor == null){
            previousCursor = timelineCursor;
        }
    }

    public boolean startTimelineRequest(){ return requestInFlight.compareAndSet(false, true); }

    public void finishTimeLineRequest(){ requestInFlight.set(false); }
}
