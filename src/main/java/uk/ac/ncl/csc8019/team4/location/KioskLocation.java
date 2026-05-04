package uk.ac.ncl.csc8019.team4.location;

import jakarta.persistence.*;

@Entity
@Table(name = "kiosk_locations")
public class KioskLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "station_name", nullable = false, length = 120)
    private String stationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "operating_status", nullable = false, length = 20)
    private OperatingStatus operatingStatus = OperatingStatus.OPEN;

    protected KioskLocation() {}

    public KioskLocation(String name, String stationName) {
        this.name = name;
        this.stationName = stationName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStationName() {
        return stationName;
    }

    public OperatingStatus getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(OperatingStatus operatingStatus) {
        this.operatingStatus = operatingStatus;
    }
}
