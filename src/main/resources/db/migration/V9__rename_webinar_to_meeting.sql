-- The webinar page became the meeting page (see MeetingResource). The two stored keys carry the old
-- name, so they are renamed here rather than read under both names in code.
--
-- Deliberately not edited into V4/V8: Flyway checksums migrations that already ran, and changing one
-- makes a running instance refuse to start.

update public.event_data set key = 'meetingLink' where key = 'webinarLink';

update public.content set key = 'meeting.tools' where key = 'webinar.tools';
