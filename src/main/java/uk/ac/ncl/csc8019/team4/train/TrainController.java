package uk.ac.ncl.csc8019.team4.train;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(originPatterns = "*")
@Validated
@RestController
@RequestMapping("/api/trains")
    @CrossOrigin(origins = "*") 
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping("/arrivals")
    public List<Train> arrivalsAtCramlington(@RequestParam(defaultValue = "3") @Min(1) @Max(20) int count) {
        return trainService.getArrivalsAtCramlington(count);
    }

    @GetMapping("/departures")
    public List<Train> departuresFromCramlington(@RequestParam(defaultValue = "3") @Min(1) @Max(20) int count) {
        return trainService.getDeparturesFromCramlington(count);
    }

    @GetMapping("/services/{serviceId}")
    public TrainServiceStatus serviceStatus(@PathVariable String serviceId) {
        return trainService.getServiceStatus(serviceId);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleConstraintViolation() {
        // Converts invalid request parameters into HTTP 400 responses.
    }
}
