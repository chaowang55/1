# Whistlestop Coffee Hut Backend

Team 4 backend system for the CSC8019 Software Engineering team project.

This project provides the backend API for a pre-ordering web application for **Whistlestop Coffee Hut**, a small kiosk based at **Cramlington Station**. The system allows customers to place coffee orders, choose pickup times, view order status, and allows staff to manage active and archived orders.

The backend is built with **Java Spring Boot**, uses **MySQL** for persistent storage, **Flyway** for database migrations, and includes integration with live train data through the National Rail Darwin/OpenLDBWS service via Huxley2.

---

## Project Features

### Customer Features

- View menu items.
- Place an order with:
  - customer name
  - customer email
  - pickup time
  - one or more menu items
  - size and quantity
- Validate pickup times against kiosk opening hours.
- Reject invalid orders, such as:
  - past pickup times
  - empty item lists
  - invalid email addresses
  - quantities below 1 or above 20
  - unavailable or unknown menu items
- View live order status.

### Staff Features

- View active orders on a dashboard.
- Update order statuses through the order lifecycle:
  - `PENDING`
  - `ACCEPTED`
  - `IN_PROGRESS`
  - `READY`
  - `COLLECTED`
  - `CANCELLED`
- Cancel orders with a reason.
- View archived completed/cancelled orders.

### Train Data Features

- View live train arrivals at Cramlington Station.
- View live train departures from Cramlington Station.
- Fetch train service status, including delay/cancellation information.
- If no train API token is supplied, train endpoints return an empty list rather than crashing.

---

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| Database | MySQL |
| Migrations | Flyway |
| Build Tool | Gradle |
| Testing | JUnit, MockMvc |
| Containerisation | Docker / Docker Compose |
| External Train API | National Rail Darwin/OpenLDBWS via Huxley2 |

---

## Project Structure

Package structure:

```text
src/
├── main/
│   ├── java/
│   │   └── uk/ac/ncl/csc8019/team4/
│   │       ├── auth/
│   │       ├── location/
│   │       ├── menu/
│   │       ├── order/
│   │       └── train/
│   └── resources/
│       ├── application.properties
│       └── db/migration/
└── test/
    ├── java/
    │   └── uk/ac/ncl/csc8019/team4/
    │       ├── menu/
    │       ├── order/
    │       └── train/
    └── resources/
        └── application.properties
