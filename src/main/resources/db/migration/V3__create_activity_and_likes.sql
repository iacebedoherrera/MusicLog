CREATE TABLE activity_entries (
    id UUID PRIMARY KEY,
    actor_id UUID NOT NULL,
    activity_type VARCHAR(30) NOT NULL,
    target_id UUID,
    target_mbid VARCHAR(64),
    target_type VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_activity_type CHECK (activity_type IN ('REVIEW_CREATED', 'REVIEW_UPDATED', 'TRACK_LOGGED', 'USER_FOLLOWED'))
);

CREATE TABLE review_likes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    review_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_review_likes_user_review UNIQUE (user_id, review_id)
);

CREATE INDEX idx_activity_actor_created_at ON activity_entries (actor_id, created_at DESC);
CREATE INDEX idx_activity_created_at ON activity_entries (created_at DESC);
CREATE INDEX idx_activity_target_id ON activity_entries (target_id);
CREATE INDEX idx_review_likes_review_id ON review_likes (review_id);
CREATE INDEX idx_review_likes_user_id ON review_likes (user_id);
