package uk.ac.ncl.csc8019.team4.order;

import java.time.LocalTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ncl.csc8019.team4.auth.StaffLock;

@RestController
@RequestMapping("/api/opening-hours")
public class OpeningHoursController {

    private final OpeningHoursRepository repo;

    public OpeningHoursController(OpeningHoursRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<OpeningHours> list() {
        return repo.findAll();
    }

    @PutMapping("/{dayOfWeek}")
    @StaffLock
    public OpeningHours update(@PathVariable Byte dayOfWeek, @RequestBody UpdateRequest req) {
        OpeningHours hours = repo.findById(dayOfWeek)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid day: " + dayOfWeek));

        hours.setOpenTime(req.openTime());
        hours.setCloseTime(req.closeTime());
        return repo.save(hours);
    }

    public record UpdateRequest(LocalTime openTime, LocalTime closeTime) {}
}
