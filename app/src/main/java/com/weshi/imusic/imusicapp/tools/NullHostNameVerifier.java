package com.weshi.imusic.imusicapp.tools;

/**
 * Created by apple28 on 15/11/17.
 */

import javax.net.ssl.HostnameVerifier;
        import javax.net.ssl.SSLSession;

        import android.util.Log;

public class NullHostNameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        //Log.i("RestUtilImpl", "Approving certificate for " + hostname);
        return true;
    }

}