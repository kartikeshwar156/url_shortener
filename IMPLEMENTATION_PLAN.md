# Implementation Plan — URL Shortener & Link Analytics

Feed this into Cursor one step at a time (not all at once). Each step is
scoped to produce one meaningful commit, which is exactly what the
take-home is evaluating. After each step: review the diff yourself, adjust
anything you'd have done differently, run the relevant tests, *then* commit
with the suggested message (edit it to reflect what you actually changed).

Don't paste "do everything" into Cursor — the whole point of the exercise
is showing where you steered it. Work through this incrementally.

---

## Step 0 — Project scaffold

**Prompt for Cursor:**
> Set up a new Spring Boot 3.3 Maven project in Java 17 called
> `url-shortener`, group `com.example`. Add dependencies: spring-boot-starter-web,
> spring-boot-starter-data-mongodb, spring-boot-starter-validation,
> spring-boot-starter-test. Add a `.gitignore` for a standard Maven/Java
> project that also excludes `.env` and any `application-secrets.yml`.
> Don't add any business logic yet — just get it building and running with
> the default Spring Boot health check.

**Acceptance criteria:** `mvn spring-boot:run` starts cleanly on port 8080.

**Commit:** `chore: scaffold Spring Boot project`

---

## Step 1 — MongoDB Atlas connection, externalized

**Prompt for Cursor:**
> Configure `application.yml` so the MongoDB URI comes from an
> `MONGODB_URI` environment variable (fallback to a local
> `mongodb://localhost:27017/urlshortener` for dev), never hardcoded.
> Also externalize a `PORT` and an `APP_BASE_URL` property (used later to
> build full short links). Add a `README.md` section explaining how to set
> `MONGODB_URI` for MongoDB Atlas.

**Your manual check:** confirm no credentials made it into any tracked
file. Set `MONGODB_URI` locally and confirm the app connects to your Atlas
cluster on startup.

**Commit:** `feat: externalize MongoDB Atlas connection config`

---

## Step 2 — Data model

**Prompt for Cursor:**
> Create a `UrlMapping` MongoDB document with fields: id, shortCode (unique
> indexed), longUrl (indexed, not unique), customAlias (boolean),
> createdAt, clickCount. Also create a `SequenceCounter` document
> (`sequence_counters` collection) used as an atomic counter — single field
> `value` — for generating non-colliding short codes later.

**Acceptance criteria:** entities compile, no repository/service logic yet.

**Commit:** `feat: add UrlMapping and SequenceCounter document models`

---

## Step 3 — Short-code generation strategy

This is the step to think hardest about yourself before prompting — it's
the one the exercise explicitly asks you to be ready to defend.

**Decide first:** counter-based (atomic, no collisions by construction) vs.
random-string-with-retry (simpler, probabilistic). Pick one and know why.

**Prompt for Cursor (counter-based example):**
> Implement a `SequenceGeneratorService` using `MongoOperations.findAndModify`
> with `$inc` and `upsert=true` on the `SequenceCounter` document to
> atomically hand out a strictly increasing long value. Implement a
> `Base62Codec` utility that encodes a long into a URL-safe Base62 string,
> with a padded variant so early codes have a consistent minimum length.
> Add unit tests proving distinct inputs never produce the same output
> across a large range of values.

**Commit:** `feat: add collision-safe short code generation (atomic counter + Base62)`

---

## Step 4 — URL validation

**Prompt for Cursor:**
> Add a `UrlValidator` utility that accepts only http/https URLs with a
> non-blank host, rejecting everything else (including schemes like
> javascript:/file:) with a custom `InvalidUrlException`. Add unit tests
> for valid URLs, blank/null input, non-http schemes, and malformed input.

**Commit:** `feat: add strict URL validation`

---

## Step 5 — Repository layer

**Prompt for Cursor:**
> Add a Spring Data `UrlMappingRepository` with: findByShortCode,
> existsByShortCode, and a lookup for an existing non-custom-alias mapping
> by longUrl (to support idempotent duplicate handling in the next step).

**Commit:** `feat: add UrlMappingRepository`

---

## Step 6 — Core service: shorten + resolve

**Decide first (again, know your own answer before prompting):** what
happens when the same URL is shortened twice without a custom alias?
Reuse the existing code, or always mint a new one? What happens when a
custom alias is requested for a URL that's already been shortened?

**Prompt for Cursor:**
> Implement `UrlShortenerService`/`UrlShortenerServiceImpl` with:
> - `shorten(url, customAlias)`: validates the URL; if a custom alias is
>   given, checks availability and creates a new mapping (409-worthy
>   exception if taken, with a DuplicateKeyException fallback for races);
>   otherwise checks for an existing non-aliased mapping for that URL and
>   reuses it if found, else generates a new code via the sequence service.
> - `resolve(shortCode)`: looks up the mapping, increments click count,
>   throws a not-found exception if missing.
> Add unit tests with Mockito covering: new code generation, duplicate URL
> reuse, custom alias creation, alias conflict, invalid URL rejection,
> resolve success, resolve not-found.

**Commit:** `feat: implement core shorten/resolve service logic`

---

## Step 7 — REST layer

**Prompt for Cursor:**
> Add `ShortenRequest`/`ShortenResponse` DTOs (validate `url` as non-blank,
> `customAlias` as an optional 3-32 char [A-Za-z0-9_-] pattern). Add
> `UrlShortenerController` with `POST /shorten` (201 on new, 200 on reused)
> and `GET /{code}` (301 redirect via Location header, or propagate 404).
> Add a `@RestControllerAdvice` mapping InvalidUrlException→400,
> AliasAlreadyTakenException→409, ShortCodeNotFoundException→404,
> validation errors→400.

**Commit:** `feat: add REST endpoints for shorten and redirect`

---

## Step 8 — Integration tests

**Prompt for Cursor:**
> Add an embedded MongoDB test dependency
> (de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x) scoped to test.
> Write a `@SpringBootTest(webEnvironment=RANDOM_PORT)` integration test
> covering: shorten→redirect round trip (assert 301 + Location header,
> using a redirect-disabled HTTP client so the test doesn't actually follow
> the link out), unknown code→404, duplicate URL→same code reused,
> custom alias conflict→409, invalid URL→400.

**Acceptance criteria:** `mvn test` passes fully offline (no real Atlas
connection required).

**Commit:** `test: add end-to-end integration tests with embedded MongoDB`

---

## Step 9 — Polish pass (do this one yourself, lightly assisted)

This is a good step to *not* hand entirely to Cursor — it's where "what you
overrode and why" in the write-up should have the most material.

- Re-read every class. Anywhere Cursor's naming, error messages, or
  structure doesn't match how you'd explain it live, change it.
- Confirm the `.gitignore` actually keeps secrets out — check
  `git status` after setting `MONGODB_URI` locally.
- Run `mvn test` one more time end to end.

**Commit:** `refactor: polish and review pass`

---

## Step 10 — README and write-up

**Prompt for Cursor (README only):**
> Write a README covering: what the service does, the two API endpoints,
> how to set MONGODB_URI and run it, how to run tests, and a short section
> explaining the short-code-generation and duplicate-URL design decisions
> and why.

**Do not let Cursor write `WRITEUP.md` for you.** That's the one piece
that has to be in your own words — it's literally asking what you did and
where you disagreed with the AI's output. Write it last, once everything
above is fresh in your mind, referencing actual commits by message/hash
where useful.

**Commit:** `docs: add README and write-up`

---

## Before you submit

- [ ] `mvn clean test` passes with no external network/Atlas dependency.
- [ ] `git log --oneline` shows the incremental story above, not one squashed commit.
- [ ] No secrets anywhere in the repo (`git log -p | grep -i mongodb+srv` should return nothing from real credentials).
- [ ] Atlas password rotated if it was ever pasted into a chat, doc, or committed file at any point.
- [ ] `WRITEUP.md` is filled in, in your own words, referencing real trade-offs you made.
- [ ] You can explain every file well enough to extend it live in the follow-up round — that's explicitly what they're screening for.
