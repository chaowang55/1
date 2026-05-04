package uk.ac.ncl.csc8019.team4.location;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KioskLocationRepository extends JpaRepository<KioskLocation, Long> {

    List<KioskLocation> findByOperatingStatusOrderByIdAsc(OperatingStatus status);

    Optional<KioskLocation> findFirstByOperatingStatusOrderByIdAsc(OperatingStatus status);
}
