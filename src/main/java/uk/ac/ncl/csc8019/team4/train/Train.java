package uk.ac.ncl.csc8019.team4.train;

public record Train(
        String serviceId,
        String origin,
        String destination,
        String scheduledTime,
        String estimatedTime,
        String platform,
        String operator,
        boolean cancelled,
        int delayMinutes,
        String delayReason) {}
