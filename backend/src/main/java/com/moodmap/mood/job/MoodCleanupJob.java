package com.moodmap.mood.job;

import com.moodmap.mood.service.MoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MoodCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(MoodCleanupJob.class);

    private final MoodService moodService;

    public MoodCleanupJob(MoodService moodService) {
        this.moodService = moodService;
    }

    @Scheduled(cron = "0 20 * * * *")
    public void purgeExpired() {
        int removed = moodService.purgeExpired();
        if (removed > 0) {
            log.info("Purged {} expired moods", removed);
        }
    }
}
