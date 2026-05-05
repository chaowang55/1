package uk.ac.ncl.csc8019.team4.location;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ncl.csc8019.team4.auth.StaffLock;

@RestController
@RequestMapping("/api/locations")
public class KioskLocationController {

    private final KioskLocationRepository locations;

    public KioskLocationController(KioskLocationRepository locations) {
        this.locations = locations;
    }

    @GetMapping
    public List<KioskLocation> listOpenLocations() {
        return locations.findByOperatingStatusOrderByIdAsc(OperatingStatus.OPEN);
    }

    @PatchMapping("/{id}/status")
    @StaffLock
    public KioskLocation updateStatus(@PathVariable Long id, @RequestParam OperatingStatus status) {
        KioskLocation location = locations
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + id));
        location.setOperatingStatus(status);
        return locations.save(location);
    }
}
