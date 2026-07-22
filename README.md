# URL Shortener

A small Spring Boot service that turns long URLs into short codes and redirects visitors to the original link. Mappings are stored in MongoDB.

## API

### POST /shorten

Create or reuse a short link.

**Request body:**

```json
{
  "url": "https://example.com/long/path",
  "customAlias": "optional-alias"
}
```

- `url` (required): must be a valid `http` or `https` URL with a host
- `customAlias` (optional): 3–32 characters, `[A-Za-z0-9_-]` only

**Responses:**

- `201 Created` — new short code generated
- `200 OK` — existing non-custom mapping reused for the same URL
- `400 Bad Request` — invalid URL or validation error
- `409 Conflict` — custom alias already taken

**Example response:**

```json
{
  "shortCode": "000001",
  "shortUrl": "http://localhost:8080/000001",
  "longUrl": "https://example.com/long/path",
  "reused": false
}
```

### GET /{code}

Redirects (`301 Moved Permanently`) to the original URL and increments the click count.

- `404 Not Found` — unknown short code

## Configuration

| Variable       | Default                              | Description                          |
|----------------|--------------------------------------|--------------------------------------|
| `MONGODB_URI`  | `mongodb://localhost:27017/urlshortener` | MongoDB connection string        |
| `PORT`         | `8080`                               | HTTP port                            |
| `APP_BASE_URL` | `http://localhost:8080`              | Base URL used in `shortUrl` responses |

### MongoDB Atlas

1. Create a cluster in [MongoDB Atlas](https://cloud.mongodb.com).
2. Create a database user and add your IP to the network access list.
3. Copy the connection string (SRV format) and set the database name, e.g. `urlshortener`:

```bash
export MONGODB_URI="mongodb+srv://<user>:<password>@cluster0.example.mongodb.net/urlshortener?retryWrites=true&w=majority"
export APP_BASE_URL="http://localhost:8080"
```

**Never commit credentials.** Use environment variables or a local untracked `.env` file.

On Windows PowerShell:

```powershell
$env:MONGODB_URI="mongodb+srv://<user>:<password>@cluster0.example.mongodb.net/urlshortener?retryWrites=true&w=majority"
$env:APP_BASE_URL="http://localhost:8080"
```

## Run

**Prerequisites:** Java 17+, Maven 3.9+

```bash
mvn spring-boot:run
```

The service listens on port 8080 by default.

## Test

Tests use embedded MongoDB and do not require Atlas or network access:

```bash
mvn test
```

## Design decisions

### Short-code generation

Short codes are produced by an **atomic MongoDB counter** (`findAndModify` with `$inc` and `upsert`) followed by **Base62 encoding** with 6-character minimum padding. This avoids collisions by construction — each code maps to a unique monotonically increasing integer — rather than relying on random strings and retry logic.

### Duplicate URLs

When the same URL is shortened again **without** a custom alias, the service **reuses** the existing mapping and returns `200 OK` with `reused: true`. Custom aliases always create a new mapping (if the alias is available), even when the URL was shortened before.
