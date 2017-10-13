package de.mediathekview.mserver.base.config;

import java.util.concurrent.TimeUnit;

public class ScheduleDTO {
    private TimeUnit unit;
    private Long duration;
    
    public ScheduleDTO() {
        unit=TimeUnit.MINUTES;
        duration=0l;
    }
    
    public void setDuration(Long duration) {
        this.duration = duration;
    }
    
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }
    
    public Long getDuration() {
        return duration;
    }
    
    public TimeUnit getUnit() {
        return unit;
    }
}
