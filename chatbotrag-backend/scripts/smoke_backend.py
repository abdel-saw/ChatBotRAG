"""Check container startup/authentication without downloading Ollama models."""
import json
import time
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen
import uuid


def main():
    base = "http://127.0.0.1:8080"
    for attempt in range(60):
        try:
            with urlopen(base + "/api/documents", timeout=3):
                break
        except HTTPError as exc:
            if exc.code in (401, 403):
                break
        except (URLError, TimeoutError):
            pass
        if attempt == 59:
            raise SystemExit("Backend did not become ready")
        time.sleep(2)
    credentials = {"email": f"smoke-{uuid.uuid4().hex}@example.test",
                   "password": uuid.uuid4().hex, "name": "CI smoke check"}
    request = Request(base + "/api/auth/register", data=json.dumps(credentials).encode(),
                      headers={"Content-Type": "application/json"}, method="POST")
    with urlopen(request, timeout=15) as response:
        token = json.load(response)["token"]
    request = Request(base + "/api/documents", headers={"Authorization": "Bearer " + token})
    with urlopen(request, timeout=15) as response:
        assert json.load(response) == [], "Fresh user should have no documents"
    print("Backend registration, JWT authentication and database access passed; no model calls")


if __name__ == "__main__":
    main()
