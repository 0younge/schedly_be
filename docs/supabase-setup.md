# Supabase Setup

## Goal

Create a Supabase PostgreSQL database for Schedly BE and connect the Spring Boot app through local environment variables.

Do not paste real passwords, JWT secrets, API keys, or connection strings into GitHub, Markdown files, issues, PRs, or chat.

## Create Database

1. Open the Supabase dashboard.
2. Create a new project.
3. Choose a project name such as `schedly`.
4. Choose the closest region.
5. Save the database password in a password manager.
6. Wait until the project finishes provisioning.

## Get Connection Values

In the Supabase project dashboard:

1. Click `Connect`.
2. Use the `Session pooler` connection string for local and backend app traffic.
3. Copy the host, port, database, user, and password values.
4. Convert the connection string to JDBC format.

Use this shape:

```text
jdbc:postgresql://aws-0-[REGION].pooler.supabase.com:5432/postgres?sslmode=require
```

Use this username shape:

```text
postgres.[PROJECT_REF]
```

Use the password you created when making the Supabase project.

## Local .env

Copy the example file:

```bash
cp .env.example .env
```

Then edit `.env` locally:

```bash
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-[REGION].pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.[PROJECT_REF]
SPRING_DATASOURCE_PASSWORD=your-real-database-password
JWT_SECRET=your-long-random-local-jwt-secret
```

Keep `.env` local. It is ignored by Git.

## Run With .env

Spring Boot does not automatically read a plain `.env` file by itself. For local terminal runs, export the file before starting the app:

```bash
set -a
source .env
set +a
./gradlew bootRun
```

Before Supabase is ready, use the committed `dev` profile to run against an in-memory H2 database:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Health check:

```bash
curl http://localhost:8080/api/health
```

Expected response shape:

```json
{
  "status": "ok",
  "checkedAt": "2026-05-23T00:00:00Z"
}
```

## IntelliJ Setup

1. Open `/Users/gwon-yeonghyeon/schedly/schedly_be` in IntelliJ.
2. Open `Run > Edit Configurations...`.
3. Select or create the Spring Boot run configuration.
4. Add environment variables from `.env`.
5. Run `SchedlyBeApplication`.
6. Open `http://localhost:8080/api/health`.

## Next BE Work After DB Is Ready

After the Supabase project is created and `.env` is filled locally, continue with:

1. Verify BE starts against Supabase.
2. Add migration tooling and initial schema.
3. Implement `User` and `Schedule` domains.
4. Implement auth endpoints.
5. Connect FE login/signup screens to BE.
