#!/usr/bin/env python3
"""Integration checks for the real settings script, isolated from Android and remote repositories."""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SENTINEL = 'auth-regression-token-do-not-log'


class CodeArtifactAuthenticationTests(unittest.TestCase):
    """Run Gradle settings with a controlled AWS executable; never contact AWS in these cases."""

    def run_gradle(self, mode='success', offline=False, profile=None):
        with tempfile.TemporaryDirectory(prefix='paguito-auth-test-') as directory:
            root = Path(directory)
            settings = (ROOT / 'settings.gradle.kts').read_text().replace('include(":app")', '')
            (root / 'settings.gradle.kts').write_text(settings)
            trace = root / 'aws-arguments.txt'
            aws = root / 'aws-test'
            aws.write_text('''#!/bin/sh
printf '%s\\n' "$@" > "$AWS_TEST_TRACE"
if [ "$AWS_TEST_MODE" = expired ]; then
  echo 'SSO session expired: private-diagnostic-marker' >&2
  exit 255
fi
if [ "$AWS_TEST_MODE" = empty ]; then
  echo None
  exit 0
fi
printf '%s\\n' "$AWS_TEST_TOKEN"
''')
            aws.chmod(0o700)
            environment = dict(os.environ, AWS_TEST_TRACE=str(trace), AWS_TEST_MODE=mode, AWS_TEST_TOKEN=SENTINEL)
            command = ['bash', str(ROOT / 'gradlew'), '-p', str(root), 'help', '--console=plain',
                       '--no-configuration-cache', f'-PnexusAwsExecutable={aws}',
                       '-PnexusCodeArtifactToken=obsolete-property', '-PnexusCodeArtifactUrl=']
            if offline:
                command.append('--offline')
            if profile:
                command.append(f'-PnexusAwsProfile={profile}')
            result = subprocess.run(command, env=environment, stdout=subprocess.PIPE,
                                    stderr=subprocess.STDOUT, text=True, timeout=90)
            arguments = trace.read_text().splitlines() if trace.exists() else []
            self.assertNotIn(SENTINEL, result.stdout)
            self.assertNotIn('private-diagnostic-marker', result.stdout)
            return result, arguments

    def test_online_ignores_old_token_and_passes_repository_and_profile(self):
        result, args = self.run_gradle(profile='test-profile')
        self.assertEqual(0, result.returncode, result.stdout)
        self.assertEqual(['codeartifact', 'get-authorization-token'], args[:2])
        for key, value in [('domain', 'nexus'), ('domain-owner', '279391564691'),
                           ('region', 'us-east-2'), ('duration-seconds', '43200'), ('profile', 'test-profile')]:
            self.assertEqual(value, args[args.index('--' + key) + 1])

    def test_expired_aws_session_explains_interactive_login(self):
        result, _ = self.run_gradle(mode='expired', profile='test-profile')
        self.assertNotEqual(0, result.returncode)
        self.assertIn('aws sso login --profile test-profile', result.stdout)

    def test_empty_token_is_rejected_before_dependency_resolution(self):
        result, _ = self.run_gradle(mode='empty')
        self.assertNotEqual(0, result.returncode)
        self.assertIn('AWS CLI no devolvió un token válido', result.stdout)

    def test_offline_does_not_start_aws_cli(self):
        result, args = self.run_gradle(mode='expired', offline=True)
        self.assertEqual(0, result.returncode, result.stdout)
        self.assertEqual([], args)


if __name__ == '__main__':
    unittest.main(verbosity=2)
