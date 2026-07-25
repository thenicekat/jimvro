# Privacy Policy

Effective date: 26 July 2026

Jimvro is a local-first fitness diary. It does not require an account and does
not operate an application server. This policy explains what data the Android
app handles and when data may leave your device.

## Data stored on your device

Jimvro may store information you choose to enter, including:

- workouts, exercises, sets, weights, repetitions, and workout duration;
- body weight, body-fat percentage, body measurements, notes, and progress
  photos;
- foods, barcodes, calories, and macronutrients;
- workout templates, saved foods, preferences, and goals.

This information is stored in the app's private local storage. Jimvro does not
send it to the developer, use it for advertising, or use it for analytics.
Android cloud backup is disabled for the app.

## Barcode and food lookup

Barcode recognition uses Google Play services' on-device code scanner. When
you look up an uncached barcode, Jimvro sends that barcode to the Open Food
Facts API to retrieve product and nutrition information. Open Food Facts and
network providers may receive standard request information such as the
barcode, IP address, and request metadata. Their handling of that information
is governed by their own privacy terms.

- [Google privacy policy](https://policies.google.com/privacy)
- [Open Food Facts privacy policy](https://world.openfoodfacts.org/privacy)

Successful product lookups may be cached locally for offline reuse.

## Health Connect

Health Connect integration is optional and runs only after you select the sync
action and grant permission. Jimvro may write these records to Health Connect:

- completed strength-training sessions;
- body weight and body-fat percentage;
- food energy, protein, carbohydrates, and fat.

Jimvro requests write access only. It does not read Health Connect records or
upload them to a Jimvro server. Records written to Health Connect are managed
by Android and the Health Connect settings on your device. You can revoke
permission or delete records there at any time.

## Photos, exports, and backups

Selected progress photos are copied into the app's private local storage.
When you create an export or database backup, you choose its destination using
Android's system file picker. Files you share, copy, or store outside Jimvro
are controlled by you and by the destination provider. Current database
backups do not include progress-photo files or app preferences.

## Notifications

Jimvro may request notification permission for local workout and rest-timer
alerts. Notification data is generated on device and is not sent to the
developer.

## Retention and deletion

Data remains on your device until you delete individual entries, clear the
app's storage, or uninstall the app. Data exported to other locations and data
written to Health Connect must be deleted from those locations separately.

## Security

Jimvro relies on Android app sandboxing and private app storage. No storage
system can be guaranteed completely secure, so protect access to your device
and handle exported files carefully.

## Changes and contact

Material changes to this policy will be published in this repository with an
updated effective date. Questions or privacy requests can be submitted through
[the project's GitHub issue tracker](https://github.com/thenicekat/jimvro/issues).
