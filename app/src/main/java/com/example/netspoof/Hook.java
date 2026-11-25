package com.example.netspoof;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Makes every app in this module's LSPosed scope see the active network as
 * mobile data and metered. Only enable the module for the app you want to
 * spoof; do not scope it to the System Framework.
 */
public class Hook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) {
        // NetworkCapabilities: transport type and metered flag.
        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "hasTransport", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        int transport = (int) p.args[0];
                        if (transport == NetworkCapabilities.TRANSPORT_CELLULAR) p.setResult(true);
                        else if (transport == NetworkCapabilities.TRANSPORT_WIFI) p.setResult(false);
                    }
                });

        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "hasCapability", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        if ((int) p.args[0] == NetworkCapabilities.NET_CAPABILITY_NOT_METERED) {
                            p.setResult(false);
                        }
                    }
                });

        // Legacy NetworkInfo API.
        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getType",
                XC_MethodReplacement.returnConstant(ConnectivityManager.TYPE_MOBILE));
        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getTypeName",
                XC_MethodReplacement.returnConstant("MOBILE"));

        XposedHelpers.findAndHookMethod(ConnectivityManager.class, "isActiveNetworkMetered",
                XC_MethodReplacement.returnConstant(true));
    }
}
