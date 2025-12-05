# Syncause Demo Project

A Spring Boot demonstration project showcasing various testing scenarios and data-driven behavior patterns. This project illustrates how environmental configurations and database states can affect application logic across multiple business scenarios.

## Overview

This demo application is built with Spring Boot 3.3.0 and demonstrates six different testing scenarios that highlight the importance of proper data configuration and validation:

1. **Multi-condition Coupon Validation** - Complex business logic with multiple validation criteria
2. **Order ID Format Validation** - Pattern-based validation with configurable prefixes
3. **User Login Authorization** - Mock-based authentication with ban status checks
4. **VIP Access Control** - JSON deserialization and boolean field handling
5. **Locale-dependent Report Generation** - Number formatting based on system locale
6. **Bank Balance Verification** - Dynamic proxy-based service mocking

## Tech Stack

- **Java**: 17
- **Spring Boot**: 3.3.0
- **Database**: H2 (embedded, file-based)
- **Build Tool**: Maven
- **Additional Libraries**:
  - Spring JDBC
  - Jackson (JSON processing)
  - Lombok
  - H2 Database

## Project Structure

```
src/main/
├── java/com/syncause/demo/
│   ├── dao/                    # Data Access Objects
│   │   ├── CouponsDao.java
│   │   ├── MocksDao.java
│   │   └── SysConfigDao.java
│   ├── dto/                    # Data Transfer Objects
│   │   └── UserProfile.java
│   ├── DataLoader.java         # Database initialization utility
│   ├── DemoApplication.java    # Main application with API endpoints
│   └── TestController.java     # Debug endpoints for testing
└── resources/
    ├── static/
    │   └── index.html
    └── application.properties
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- An encrypted configuration file (`config.enc`) with the decryption key

## Getting Started

### 1. Build the Application

Before running the Spring Boot application, you must initialize the H2 database with the required data:

```bash
mvn clean package
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

or 

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The application will start on the default port (8080).

### 3. Access the Application

Open your browser and navigate to:
```
http://localhost:8080
```

## API Endpoints

### Scenario 1: Coupon Validation
```
GET /api/apply-coupon?code=SUMMER_2024&category=FOOD&amount=100
```
Validates a coupon based on:
- Status (must be ACTIVE)
- Expiry date (must not be expired)
- Minimum amount requirement
- Category matching

### Scenario 2: Order Creation
```
GET /api/create-order?userId=12345
```
Creates an order ID with a configurable prefix and validates the format.

### Scenario 3: User Login
```
GET /api/login?userId=u_992
```
Checks if a user is banned based on mock data from the database.

### Scenario 4: VIP Access Check
```
GET /api/check-vip
```
Verifies VIP status using JSON deserialization.

### Scenario 5: Report Generation
```
GET /api/generate-report?amount=1234.56
```
Generates a formatted report based on the configured system locale.

### Scenario 6: Bank Transfer
```
GET /api/bank-transfer
```
Checks bank balance using a dynamically proxied service.

## Debug Endpoints

### Get Coupon Data
```
GET /test/coupon-data
```
Returns raw coupon data from the database for the "SUMMER_2024" code.

### Debug Coupon Validation
```
GET /test/debug-coupon?code=SUMMER_2024&category=FOOD&amount=100
```
Provides detailed debugging information for coupon validation, including:
- Input vs database values
- Normalized strings for comparison
- Individual validation checks

## Database Configuration

The application uses an H2 file-based database (`demo_db`) with the following tables:

- **sys_config**: System configuration key-value pairs
- **upstream_mock**: Mock API responses for user services
- **mocks**: Generic mock data for various scenarios
- **coupons**: Coupon information with validation criteria

Database connection details:
```properties
spring.datasource.url=jdbc:h2:file:./demo_db;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=
```

## Configuration File

The `config.enc` file contains encrypted configuration data including:
- `sys_locale`: System locale setting (e.g., "en", "de")
- `order_prefix`: Prefix for order IDs
- `login_json`: Mock login response data
- `bank_balance`: Mock bank balance value
- `vip_json`: VIP status JSON
- `coupon_status`: Coupon activation status
- `coupon_category`: Coupon category
- `coupon_min_amount`: Minimum purchase amount for coupon
- `coupon_expiry_date`: Coupon expiration date

## Development

### Building the Project
```bash
mvn clean install
```

### Running Tests
```bash
mvn test
```

## Key Features

- **Data-Driven Testing**: All test scenarios are configurable through the database
- **Encrypted Configuration**: Sensitive data is stored in encrypted format
- **H2 File Database**: Persistent storage with file-based H2 database
- **Dynamic Mocking**: Service behavior can be modified through database configuration
- **Locale Support**: Demonstrates locale-dependent behavior
- **RESTful API**: Clean REST endpoints for each scenario

## Troubleshooting

### Database Lock Issues
If you encounter database lock errors, ensure:
- The `AUTO_SERVER=TRUE` flag is set in the connection URL
- No other processes are accessing the database file
- Previous database connections were properly closed

### Configuration Decryption Errors
If decryption fails:
- Verify you have the correct decryption key
- Ensure `config.enc` file exists in the project root directory
- Check that the file has not been corrupted

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

This is a demo project. For questions or issues, please contact the project maintainer.
