package org.hugoandrade.euro2016.predictor.admin.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

public final class NetworkUtils {

    /**
     * Ensure this class is only used as a utility.
     */
    private NetworkUtils() {
        throw new AssertionError();
    }

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr == null)
            return false;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Network[] networks = connMgr.getAllNetworks();


            boolean wifiAvailability = false;
            boolean mobileAvailability = false;

            for (Network network : networks) {
                if (connMgr.getNetworkInfo(network).getType() == ConnectivityManager.TYPE_WIFI &&
                        connMgr.getNetworkInfo(network) != null &&
                        connMgr.getNetworkInfo(network).isAvailable()) {
                    wifiAvailability = true;
                }
                else if (connMgr.getNetworkInfo(network).getType() == ConnectivityManager.TYPE_MOBILE &&
                        connMgr.getNetworkInfo(network) != null &&
                        connMgr.getNetworkInfo(network).isAvailable()) {
                    mobileAvailability = true;
                }
            }

            return wifiAvailability || mobileAvailability;
        }
        else {

            NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
            boolean wifiAvailability = false;
            boolean mobileAvailability = false;
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    wifiAvailability = true;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    mobileAvailability = true;
                }
            }

            return wifiAvailability || mobileAvailability;
        }
    }
}
