package me.bkrmt.bkmito.api;

public interface MitoStats {
    int getKills();

    long getEndTime();

    void setEndTime(long endTime);

    void setKills(int kills);

    long getStartTime();

    void setStartTime(long startTime);

    int getMitoTimes();

    String getFormattedDuration();

    void setMitoTimes(int mitoTimes);
}
