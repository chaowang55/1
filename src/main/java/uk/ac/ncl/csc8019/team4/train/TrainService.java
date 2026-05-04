package uk.ac.ncl.csc8019.team4.train;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Fetches live train information at Cramlington (CRS: CRA) from the
 * Huxley2 JSON proxy, which sits in front of National Rail's Darwin SOAP API.
 *
 * Darwin token obtained from: https://realtime.nationalrail.co.uk/OpenLDBWSRegistration/
 */
@Service
public class TrainService {

    private static final String CRAMLINGTON_CRM = "CRM";

    private static final String HUXLEY2_BASE = "https://huxley2.azurewebsites.net";

    private final RestTemplate restTemplate;

    @Value("${darwin.access-token:}")
    private String darwinToken;

    public TrainService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Returns upcoming arrivals at Cramlington station.
     *
     * @param count how many services to return, max 20
     */
    public List<Train> getArrivalsAtCramlington(int count) {
        if (darwinToken == null || darwinToken.isBlank()) {
            return Collections.emptyList();
        }

        String url = UriComponentsBuilder.fromUriString(HUXLEY2_BASE)
                .pathSegment("arrivals", CRAMLINGTON_CRM, String.valueOf(count))
                .queryParam("accessToken", darwinToken)
                .toUriString();

        try {
            HuxleyResponse response = restTemplate.getForObject(url, HuxleyResponse.class);

            if (response == null || response.trainServices() == null) {
                return Collections.emptyList();
            }

            return response.trainServices().stream()
                    .map(TrainService::toArrival)
                    .toList();

        } catch (RestClientException | IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns upcoming departures from Cramlington station.
     *
     * @param count how many services to return, max 20
     */
    public List<Train> getDeparturesFromCramlington(int count) {
        if (darwinToken == null || darwinToken.isBlank()) {
            return Collections.emptyList();
        }

        String url = UriComponentsBuilder.fromUriString(HUXLEY2_BASE)
                .pathSegment("departures", CRAMLINGTON_CRM, String.valueOf(count))
                .queryParam("accessToken", darwinToken)
                .toUriString();

        try {
            HuxleyResponse response = restTemplate.getForObject(url, HuxleyResponse.class);

            if (response == null || response.trainServices() == null) {
                return Collections.emptyList();
            }

            return response.trainServices().stream()
                    .map(TrainService::toDeparture)
                    .toList();

        } catch (RestClientException | IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the current status of a specific train service by its service ID.
     * Used to check whether a customer's chosen train is delayed or cancelled.
     */
    public TrainServiceStatus getServiceStatus(String serviceId) {
        if (darwinToken == null || darwinToken.isBlank()) {
            return TrainServiceStatus.unknown(serviceId);
        }

        String url = UriComponentsBuilder.fromUriString(HUXLEY2_BASE)
                .pathSegment("service", serviceId)
                .queryParam("accessToken", darwinToken)
                .toUriString();

        try {
            HuxleyServiceResponse response = restTemplate.getForObject(url, HuxleyServiceResponse.class);

            if (response == null) {
                return TrainServiceStatus.unknown(serviceId);
            }

            return toStatus(response);

        } catch (RestClientException | IllegalArgumentException e) {
            return TrainServiceStatus.unknown(serviceId);
        }
    }

    private static Train toArrival(HuxleyResponse.Service service) {
        String origin = firstLocationName(service.origin());
        String destination = firstLocationName(service.destination());

        boolean cancelled = Boolean.TRUE.equals(service.isCancelled());
        String eta = service.eta() != null ? service.eta() : (cancelled ? "Cancelled" : "Unknown");
        int delayMinutes = calculateDelayMinutes(service.sta(), eta);

        return new Train(
                service.serviceID(),
                origin,
                destination,
                service.sta(),
                eta,
                service.platform(),
                service.operator(),
                cancelled,
                delayMinutes,
                service.delayReason());
    }

    private static Train toDeparture(HuxleyResponse.Service service) {
        String origin = firstLocationName(service.origin());
        String destination = firstLocationName(service.destination());

        boolean cancelled = Boolean.TRUE.equals(service.isCancelled());
        String etd = service.etd() != null ? service.etd() : (cancelled ? "Cancelled" : "Unknown");
        int delayMinutes = calculateDelayMinutes(service.std(), etd);

        return new Train(
                service.serviceID(),
                origin,
                destination,
                service.std(),
                etd,
                service.platform(),
                service.operator(),
                cancelled,
                delayMinutes,
                service.delayReason());
    }

    private static String firstLocationName(List<HuxleyResponse.Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return "Unknown";
        }

        return locations.get(0).locationName();
    }

    private static TrainServiceStatus toStatus(HuxleyServiceResponse response) {
        boolean cancelled = Boolean.TRUE.equals(response.isCancelled());

        return new TrainServiceStatus(response.serviceID(), cancelled, response.delayReason(), response.cancelReason());
    }

    /**
     * Parses scheduled and estimated times to work out the delay in minutes.
     * Returns 0 if on time, unknown, or cancelled.
     */
    private static int calculateDelayMinutes(String scheduledTime, String estimatedTime) {
        if (scheduledTime == null || estimatedTime == null) {
            return 0;
        }

        if (estimatedTime.equalsIgnoreCase("On time")
                || estimatedTime.equalsIgnoreCase("Cancelled")
                || estimatedTime.equalsIgnoreCase("Unknown")) {
            return 0;
        }

        try {
            String[] scheduledParts = scheduledTime.split(":");
            String[] estimatedParts = estimatedTime.split(":");

            int scheduledMinutes = Integer.parseInt(scheduledParts[0]) * 60 + Integer.parseInt(scheduledParts[1]);
            int estimatedMinutes = Integer.parseInt(estimatedParts[0]) * 60 + Integer.parseInt(estimatedParts[1]);

            return Math.max(0, estimatedMinutes - scheduledMinutes);

        } catch (RestClientException | IllegalArgumentException e) {
            return 0;
        }
    }

    record HuxleyResponse(String locationName, String crs, List<Service> trainServices) {

        record Service(
                String serviceID,
                List<Location> origin,
                List<Location> destination,
                String sta,
                String eta,
                String std,
                String etd,
                String platform,
                String operator,
                Boolean isCancelled,
                String delayReason) {}

        record Location(String locationName, String via) {}
    }

    record HuxleyServiceResponse(String serviceID, Boolean isCancelled, String delayReason, String cancelReason) {}
}
