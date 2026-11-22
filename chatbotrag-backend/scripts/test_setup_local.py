import contextlib
import io
import os
from pathlib import Path
import tempfile
import unittest

from setup_local import create_env


class LocalSetupTests(unittest.TestCase):
    def test_creates_distinct_credentials_without_printing_them(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / ".env"
            output = io.StringIO()
            with contextlib.redirect_stdout(output):
                create_env(path)
            values = dict(line.split("=", 1) for line in path.read_text().splitlines() if "=" in line)
            self.assertEqual(len(values["DB_PASSWORD"]), 48)
            self.assertEqual(len(values["JWT_SECRET"]), 64)
            self.assertNotEqual(values["DB_PASSWORD"], values["JWT_SECRET"])
            self.assertEqual(output.getvalue(), "")

    def test_preserves_existing_credentials(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / ".env"
            path.write_text("fixture-existing")
            with self.assertRaises(FileExistsError):
                create_env(path)
            self.assertEqual(path.read_text(), "fixture-existing")

    def test_separate_setups_get_different_values(self):
        with tempfile.TemporaryDirectory() as folder:
            a, b = Path(folder) / "a.env", Path(folder) / "b.env"
            create_env(a)
            create_env(b)
            self.assertNotEqual(a.read_text(), b.read_text())

    @unittest.skipIf(os.name == "nt", "POSIX permission bits")
    def test_new_file_has_owner_only_permissions(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / ".env"
            create_env(path)
            self.assertEqual(path.stat().st_mode & 0o777, 0o600)


if __name__ == "__main__":
    unittest.main()
