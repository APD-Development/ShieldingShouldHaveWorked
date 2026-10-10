"""Regressions for SDK signer output observed during TEST22 delivery."""
import unittest
from apd_existing_build_evidence import certificates


class SignerOutputTest(unittest.TestCase):
    def test_numbered_and_v2_signers_report_the_same_certificate(self):
        digest = '067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e'
        for label in ('Signer #1', 'Signer #2:', 'V2 Signer:', 'V3 Signer:'):
            self.assertEqual([digest], certificates(f'{label} certificate SHA-256 digest: {digest}\n'))

    def test_missing_malformed_and_source_stamp_certificates_do_not_pass(self):
        self.assertEqual([], certificates('APK is verified'))
        self.assertEqual([], certificates('V2 Signer: certificate SHA-256 digest: 123\n'))
        self.assertEqual([], certificates('Source Stamp Signer: certificate SHA-256 digest: ' + 'a' * 64))

    def test_distinct_signers_are_preserved_and_cannot_match_a_single_signer(self):
        first = 'V2 Signer: certificate SHA-256 digest: ' + 'a' * 64
        second = 'Signer #2 certificate SHA-256 digest: ' + 'b' * 64
        self.assertNotEqual(certificates(first), certificates(first + '\n' + second))
        self.assertEqual(['a' * 64], certificates(first + '\n' + first))


if __name__ == '__main__':
    unittest.main()
