# Backend regression checks

From `chatbotrag-backend`, use Java 21 and the included Maven wrapper:

```bash
bash mvnw -B -Dtest=DocumentServiceTest,ChatServiceTest,PgVectorDocumentDeletionTest test
```

On Windows PowerShell, use `./mvnw.cmd` instead of `bash mvnw`.

These checks cover:

- Deleting only the selected document's vectors for its authenticated owner.
- Preserving files and metadata when vector deletion fails.
- Rejecting deletion and conversation access by another user.
- Removing indexed content even when the original file is already absent.
- Keeping retrieved document text and questions out of console output.
- Avoiding model calls when retrieval returns no context.

The unit tests mock external services. `PgVectorDocumentDeletionTest` also
exercises the real Spring AI PostgreSQL metadata-delete filter against a test
database. It runs when `PGVECTOR_TEST_URL` is set; otherwise it is skipped.
It uses a uniquely named temporary table, removed after the test. Use a test
database where the configured user may create the vector extension and tables.

Optional database settings:

- `PGVECTOR_TEST_URL`, for example `jdbc:postgresql://localhost:5432/chatbotrag_test`.
- `PGVECTOR_TEST_USER` and `PGVECTOR_TEST_PASSWORD` (default: `postgres`).

GitHub Actions provisions an isolated pgvector/PostgreSQL 16 service and runs
all nine checks with Java 21. No Ollama instance or model download is required.
The existing application-context scaffold is separate from this targeted suite;
it still needs its own application/database configuration.

Document files and database records do not share an atomic transaction. These
checks establish that vector deletion happens first and its failure does not
delete the file or metadata; they do not establish recovery from every later
filesystem or database failure.
