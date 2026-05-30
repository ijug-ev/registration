-- In test mode the app runs on port 8081 (quarkus.http.test-port default),
-- not 8080, so the events.json URL seeded by V3 would fail to resolve.
UPDATE tenant SET events = 'http://localhost:8081/events.json' WHERE id = 'test';
