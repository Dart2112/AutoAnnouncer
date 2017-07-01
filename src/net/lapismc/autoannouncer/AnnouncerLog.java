package net.lapismc.autoannouncer;

import java.util.logging.Level;
import java.util.logging.Logger;

class AnnouncerLog {

    private Logger logger;

    AnnouncerLog(Announcer an) {
        logger = an.getLogger();
    }

    void info(String msg) {
        logger.log(Level.INFO, msg);
    }

    void warning(String msg) {
        logger.log(Level.WARNING, msg);
    }

    void severe(Throwable e) {
        logger.log(Level.SEVERE, "Failed to copy default config!", e);
    }
}
