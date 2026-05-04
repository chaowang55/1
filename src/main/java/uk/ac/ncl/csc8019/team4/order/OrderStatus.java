package uk.ac.ncl.csc8019.team4.order;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    READY,
    COLLECTED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (next == CANCELLED) return this != COLLECTED && this != CANCELLED;
        return switch (this) {
            case PENDING -> next == ACCEPTED;
            case ACCEPTED -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == READY;
            case READY -> next == COLLECTED;
            case COLLECTED, CANCELLED -> false;
        };
    }
}
