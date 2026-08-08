CREATE TABLE event_publication (
    id               UUID        NOT NULL,
    listener_id      TEXT        NOT NULL,
    event_type       TEXT        NOT NULL,
    serialized_event TEXT        NOT NULL,
    publication_date TIMESTAMPTZ NOT NULL,
    completion_date  TIMESTAMPTZ,
    PRIMARY KEY (id)
);

CREATE INDEX event_publication_incomplete
    ON event_publication (publication_date)
    WHERE completion_date IS NULL;