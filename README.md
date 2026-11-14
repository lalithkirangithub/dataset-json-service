# Dataset JSON Service

A Spring Boot REST service that stores schema-less JSON records in named datasets
(PostgreSQL) and lets you query them with dynamic **group-by** and **sort-by** operations.

## Live API

- **Base URL:** https://dataset-json-service.onrender.com
- **Example:** https://dataset-json-service.onrender.com/api/dataset/employees/query?groupBy=department
- **Group By:** https://dataset-json-service.onrender.com/api/dataset/employees/query?groupBy=department
- **Sort By:** https://dataset-json-service.onrender.com/api/dataset/employees/query?sortBy=age&order=asc


## API Documentation (Swagger UI)

- **Swagger UI (live):** https://dataset-json-service.onrender.com/swagger-ui/index.html
- **OpenAPI JSON (live):** https://dataset-json-service.onrender.com/v3/api-docs
- **Swagger UI (local):** http://localhost:8080/swagger-ui/index.html

### How to use Swagger UI
1. Open the Swagger UI link in a browser.
2. Click an endpoint, then **Try it out**.
3. Fill in the parameters (for example `datasetName` = `employees`), then click **Execute**.
4. The response code and body appear below.

> Hosted on a free plan, so the first request after a period of inactivity can take up to a minute to wake the server.

## Tech stack

Java 17, Spring Boot 4, Spring Data JPA, PostgreSQL, Maven, JUnit 5, Mockito, Docker (for deployment)

## Project structure

```
src/main/java/com/example/dataset
├── controller   DatasetController
├── service      DatasetService
├── repository   DatasetRepository
├── entity       DatasetRecord
├── dto          InsertRecordResponse, GroupedResponse, SortedResponse, ErrorResponse
└── exception    GlobalExceptionHandler, DatasetNotFoundException, DuplicateRecordException
```

## How to run locally

### Prerequisites
- JDK 17 or higher
- PostgreSQL running locally
- Maven (or use the included Maven wrapper)

### Steps
1. Clone the repository:
   ```
   git clone https://github.com/lalithkirangithub/dataset-json-service.git
   cd dataset-json-service
   ```
2. Create the database:
   ```sql
   CREATE DATABASE dataset_db;
   ```
3. Open `src/main/resources/application.properties` and set your PostgreSQL password:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/dataset_db
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_PASSWORD
   ```
4. Start the application:
   ```
   ./mvnw spring-boot:run
   ```
   On Windows use `.\mvnw spring-boot:run`.
5. The API is now available at `http://localhost:8080`. The table is created automatically.

### Run the tests
```
./mvnw test -Dtest=DatasetServiceTest
```

## API documentation

### 1. Insert a record

`POST /api/dataset/{datasetName}/record`

The body can be any JSON object, as long as it contains an `id` field.

**Request**
```json
{
  "id": 1,
  "name": "John Doe",
  "age": 30,
  "department": "Engineering"
}
```

**Response: `201 Created`**
```json
{
  "message": "Record added successfully",
  "dataset": "employees",
  "recordId": "1"
}
```

### 2. Query records

`GET /api/dataset/{datasetName}/query`

| Parameter | Required | Description |
|---|---|---|
| `groupBy` | No | Field to group records by |
| `sortBy` | No | Field to sort records by |
| `order` | No | `asc` (default) or `desc` |

#### No parameters: all records
`GET /api/dataset/employees/query`
```json
{
  "records": [
    { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
  ]
}
```

#### Group by
`GET /api/dataset/employees/query?groupBy=department`
```json
{
  "groupedRecords": {
    "Engineering": [
      { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" },
      { "id": 2, "name": "Jane Smith", "age": 25, "department": "Engineering" }
    ],
    "Marketing": [
      { "id": 3, "name": "Alice Brown", "age": 28, "department": "Marketing" }
    ]
  }
}
```

#### Sort by
`GET /api/dataset/employees/query?sortBy=age&order=asc`
```json
{
  "sortedRecords": [
    { "id": 2, "name": "Jane Smith", "age": 25, "department": "Engineering" },
    { "id": 3, "name": "Alice Brown", "age": 28, "department": "Marketing" },
    { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
  ]
}
```

#### Group by and sort by together
`GET /api/dataset/employees/query?groupBy=department&sortBy=age&order=desc`

Records are sorted first and then grouped, so each group is sorted.

### Error responses

All errors use the same format:
```json
{
  "timestamp": "2026-11-14T13:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Dataset not found: xyz"
}
```

| Status | When it happens |
|---|---|
| `400 Bad Request` | Missing `id`, invalid JSON body, or `order` is not `asc` or `desc` |
| `404 Not Found` | The dataset does not exist |
| `409 Conflict` | A record with the same `id` already exists in that dataset |

## Design decisions

- **Generic storage:** every record is stored as JSON text in one table (`dataset_name`, `record_id`, `data`), so any dataset can have any fields.
- **In-memory operations:** records are loaded for a dataset, then grouped with `Collectors.groupingBy()` and sorted with a `Comparator`. For very large datasets this could be moved into SQL using PostgreSQL `jsonb`.
- **Sorting:** numbers are compared as numbers (9 before 10), and text is compared ignoring case. Records missing the sort field are placed last.
- **Grouping:** records missing the group field are placed under `"UNKNOWN"`.
- **Layered architecture:** controller, service and repository layers, with a global exception handler.

## Deployment

The app is deployed on Render using the included `Dockerfile`, with a Render PostgreSQL database.
The database connection is configured through environment variables:

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/<database>` |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |

## Testing with Postman

Import the collection from `postman/Dataset-API.postman_collection.json` and set the `baseUrl` variable to
`http://localhost:8080` (local) or `https://dataset-json-service.onrender.com` (live).