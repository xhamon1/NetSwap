package com.example.netspoof;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Makes every app in this module's LSPosed scope see the active network as
 * mobile data and metered. Only enable the module for the app you want to
 * spoof; do not scope it to the System Framework.
 *
 * Each distinct hook call is written once to the LSPosed log (Logs > Modules,
 * search for "NetSpoof") so you can see which checks the app actually makes.
 */
public class Hook implements IXposedHookLoadPackage {

    private static final Set<String> SEEN = Collections.synchronizedSet(new HashSet<>());

    // Logs each distinct message only once, because apps poll these methods constantly.
    private static void log(String msg) {
        if (SEEN.add(msg)) XposedBridge.log("NetSpoof: " + msg);
    }

    private static XC_MethodReplacement constant(String name, Object value) {
        return new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam p) {
                log(name + " -> " + value);
                return value;
            }
        };
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) {
        log("loaded in " + lp.packageName);

        // NetworkCapabilities: transport type and metered flag.
        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "hasTransport", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        int transport = (int) p.args[0];
                        if (transport == NetworkCapabilities.TRANSPORT_CELLULAR) {
                            p.setResult(true);
                            log("hasTransport(CELLULAR) -> true");
                        } else if (transport == NetworkCapabilities.TRANSPORT_WIFI) {
                            p.setResult(false);
                            log("hasTransport(WIFI) -> false");
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "hasCapability", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        if ((int) p.args[0] == NetworkCapabilities.NET_CAPABILITY_NOT_METERED) {
                            p.setResult(false);
                            log("hasCapability(NOT_METERED) -> false");
                        }
                    }
                });

        // Legacy NetworkInfo API.
        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getType",
                constant("NetworkInfo.getType", ConnectivityManager.TYPE_MOBILE));
        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getTypeName",
                constant("NetworkInfo.getTypeName", "MOBILE"));

        XposedHelpers.findAndHookMethod(ConnectivityManager.class, "isActiveNetworkMetered",
                constant("isActiveNetworkMetered", true));
    }
}
