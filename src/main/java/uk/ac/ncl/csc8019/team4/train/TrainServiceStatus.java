package uk.ac.ncl.csc8019.team4.train;

public record TrainServiceStatus(String serviceId, boolean cancelled, String delayReason, String cancelReason) {
    public static TrainServiceStatus unknown(String serviceId) {
        return new TrainServiceStatus(serviceId, false, null, null);
    }
}
