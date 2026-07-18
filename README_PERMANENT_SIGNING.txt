Emotn Boot Helper — Permanent Signing Patch

Files in this patch:
1. .github/workflows/build-apk.yml
2. app/build.gradle
3. .gitignore

Purpose:
- Build v1.5.0 as a RELEASE APK.
- Restore one permanent signing key from GitHub Actions Secrets.
- Verify the APK signature before uploading the artifact.
- Avoid a new random Android Debug certificate on every GitHub Actions run.

Required repository secrets:
- ANDROID_KEYSTORE_BASE64
- ANDROID_KEYSTORE_PASSWORD
- ANDROID_KEY_ALIAS
- ANDROID_KEY_PASSWORD

Important:
- Keep the original .jks file permanently and make secure backups.
- Never commit the .jks file or its Base64 text to Git.
- The currently installed v1.4 uses a lost temporary debug key. Therefore,
  the first migration to this permanent key requires one uninstall/install.
- After that migration, all future versions can use adb install -r, provided
  they are signed with the same permanent key.
