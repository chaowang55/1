package uk.ac.ncl.csc8019.team4.order;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Validates that a requested pick-up time falls within the kiosk's opening hours.
 * Hours are loaded from the {@code opening_hours} table so staff can update them
 * without a code change.
 */
@Service
public class OpeningHoursService {

    private final OpeningHoursRepository repo;

    public OpeningHoursService(OpeningHoursRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns {@code true} when the given date-time is within opening hours,
     * {@code false} if the kiosk is closed that day or the time is outside bounds.
     */
    public boolean isOpen(LocalDateTime dateTime) {
        int dow = dateTime.getDayOfWeek().getValue(); // 1=Mon … 7=Sun
        Optional<OpeningHours> hours = repo.findById((byte) dow);
        if (hours.isEmpty()) return false;

        OpeningHours oh = hours.get();
        if (oh.getOpenTime() == null || oh.getCloseTime() == null) return false;

        LocalTime t = dateTime.toLocalTime();
        return !t.isBefore(oh.getOpenTime()) && !t.isAfter(oh.getCloseTime());
    }
}
