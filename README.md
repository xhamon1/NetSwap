# NetSpoof

LSPosed module that makes an app see Wi-Fi as mobile data (cellular and metered).

## Build (no PC needed)

1. Create a new GitHub repository and upload everything in this folder, including the hidden `.github` folder.
2. Open the **Actions** tab and wait for the **Build APK** run to finish. You can also start it with **Run workflow**.
3. Open the finished run and download the **netspoof-apk** artifact. Unzip it to get `app-debug.apk`.

## Use

1. Install `app-debug.apk` on a rooted device with LSPosed.
2. In LSPosed, open **Modules**, enable **NetSpoof**, and select **only** the target app in its scope.
3. Force stop the target app and open it again while connected to Wi-Fi.

## What it hooks

- `NetworkCapabilities.hasTransport()`: cellular is true, Wi-Fi is false
- `NetworkCapabilities.hasCapability(NOT_METERED)`: false
- `NetworkInfo.getType()` and `getTypeName()`: mobile
- `ConnectivityManager.isActiveNetworkMetered()`: true

## Limits

- Only the app side is spoofed. The real connection is still Wi-Fi.
- It does not help if the app checks the network interface name, `TelephonyManager`, or if the server checks the carrier IP or traffic stats.
- The app may detect the spoof and ban the account. Use at your own risk.
