package me.bkrmt.bkmito;

import java.util.concurrent.TimeUnit;

public class MitoStats implements me.bkrmt.bkmito.api.MitoStats {
    private int kills;
    private long startTime;
    private long endTime;
    private int mitoTimes;

    MitoStats(int kills, long startTime, long endTime, int mitoTimes) {
        this.kills = kills;
        this.endTime = endTime;
        this.startTime = startTime;
        this.mitoTimes = mitoTimes;
    }

    @Override
    public int getKills() {
        return kills;
    }

    @Override
    public String getFormattedDuration() {
        BkMito plugin = BkMito.getInstance();
        long hours = startTime <= 0 ? 0 : TimeUnit.MILLISECONDS.toHours((endTime > 0 ? endTime : System.currentTimeMillis()) - startTime);
        long days = 0;
        if (hours > 24) {
            days = hours / 24;
            hours = hours % 24;
        }

        return (days > 0 ?
                ((days + " " + plugin.getLangFile().getTimeLocale().getDays(false, (days > 1))) + (hours > 0 ? (" " +plugin.getLangFile().getTimeLocale().getAnd() + " " + hours + " " + plugin.getLangFile().getTimeLocale().getHours(false, hours > 1)) : "")) :
                (hours + " " + plugin.getLangFile().getTimeLocale().getHours(false, (hours == 0 || hours > 1)))
        );
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public void setKills(int kills) {
        this.kills = kills;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public int getMitoTimes() {
        return mitoTimes;
    }

    @Override
    public void setMitoTimes(int mitoTimes) {
        this.mitoTimes = mitoTimes;
    }
}
