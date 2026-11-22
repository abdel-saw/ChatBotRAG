from unittest.mock import Mock
from urllib.error import HTTPError, URLError
import unittest

from smoke_backend import wait_for_backend


class StartupReadinessTests(unittest.TestCase):
    def test_retries_a_reset_before_the_authenticated_endpoint_is_ready(self):
        opener = Mock(side_effect=[ConnectionResetError("starting"),
                      HTTPError("fixture", 401, "unauthorized", {}, None)])
        pause = Mock()
        wait_for_backend("http://fixture", opener=opener, pause=pause, attempts=2)
        self.assertEqual(opener.call_count, 2)
        pause.assert_called_once_with(2)

    def test_unavailable_backend_has_a_bounded_wait(self):
        opener = Mock(side_effect=URLError("not ready"))
        pause = Mock()
        with self.assertRaisesRegex(SystemExit, "did not become ready"):
            wait_for_backend("http://fixture", opener=opener, pause=pause, attempts=2)
        self.assertEqual(opener.call_count, 2)
        pause.assert_called_once_with(2)

    def test_public_document_listing_is_not_accepted_as_healthy_authentication(self):
        response = Mock()
        response.__enter__ = Mock(return_value=response)
        response.__exit__ = Mock(return_value=False)
        with self.assertRaisesRegex(AssertionError, "must be denied"):
            wait_for_backend("http://fixture", opener=Mock(return_value=response), pause=Mock())


if __name__ == "__main__":
    unittest.main()
