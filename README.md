# Server Manager

A simple server manager application that simulates server operations with event logging.

## Building and Running

### Running as JAR

1. Build the application:
```bash
mvn clean package
```

2. Run the JAR:
```bash
java -jar target/servermanager-1.0-SNAPSHOT.jar
```

### Available Commands

- Start the server:
```bash
up [--before YYYY-MM-DD]
```

- Stop the server:
```bash
down
```

- Check server status:
```bash
status
```

- View event history:
```bash
history [--from YYYY-MM-DD] [--to YYYY-MM-DD] [--sort asc|desc] [--status up|down|failed|starting|stopping]
```

### Running with Docker

- Pull & Run the Docker image:
```bash
docker pull pari3en/servermanager:latest
docker run -it pari3en/servermanager:latest
```

## Notes

- The events are logged to `events.log` file
- When using Docker, the events log is persisted in a Docker volume
- The server simulates random delays between 3-10 seconds for operations
- Status changes can randomly succeed or fail

## Requirements

- Java 21 or higher
- Maven 3.6 or higher
- Docker (optional, for container deployment)
