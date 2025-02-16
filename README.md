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
java -jar target/server-manager.jar [command] [options]
```

### Available Commands

- Start the server:
```bash
java -jar target/server-manager.jar up [--before YYYY-MM-DD]
```

- Stop the server:
```bash
java -jar target/server-manager.jar down
```

- Check server status:
```bash
java -jar target/server-manager.jar status
```

- View event history:
```bash
java -jar target/server-manager.jar history [--from YYYY-MM-DD] [--to YYYY-MM-DD] [--sort asc|desc] [--status up|down|failed|starting|stopping]
```

### Running with Docker

1. Build the Docker image:
```bash
docker build -t server-manager .
```

2. Run commands using Docker:
```bash
# Start server
docker run -v server-data:/app/data server-manager up

# Start server with scheduled shutdown
docker run -v server-data:/app/data server-manager up --before 2024-03-20

# Stop server
docker run -v server-data:/app/data server-manager down

# Check status
docker run -v server-data:/app/data server-manager status

# View history
docker run -v server-data:/app/data server-manager history
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
