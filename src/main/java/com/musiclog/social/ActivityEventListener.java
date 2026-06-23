package com.musiclog.social;

import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.review.events.TrackLoggedEvent;
import com.musiclog.user.events.UserFollowedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ActivityEventListener {

    private final FeedService feedService;

    public ActivityEventListener(FeedService feedService) {
        this.feedService = feedService;
    }

    @EventListener
    public void onReviewCreated(ReviewCreatedEvent event) {
        feedService.handleReviewCreated(event);
    }

    @EventListener
    public void onTrackLogged(TrackLoggedEvent event) {
        feedService.handleTrackLogged(event);
    }

    @EventListener
    public void onUserFollowed(UserFollowedEvent event) {
        feedService.handleUserFollowed(event);
    }
}
