package berial.geolocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginRequestCodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@NativePlugin(
        permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },
        permissionRequestCode = PluginRequestCodes.GEOLOCATION_REQUEST_PERMISSIONS
)
public class BerialGeolocation extends Plugin {

    private final Map<String, PluginCall> watchingCalls = new HashMap<>();

    private LocationManager lm;
    private LocationListener listener;

    private void sendLocation(PluginCall call) {
        requestLocationUpdates(call);
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void watchPosition(PluginCall call) {
        call.save();
        if (!hasRequiredPermissions()) {
            saveCall(call);
            pluginRequestAllPermissions();
        } else {
            startWatch(call);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startWatch(PluginCall call) {
        requestLocationUpdates(call);
        watchingCalls.put(call.getCallbackId(), call);
    }

    @SuppressWarnings("MissingPermission")
    @PluginMethod()
    public void clearWatch(PluginCall call) {
        String callbackId = call.getString("id");
        if (callbackId != null) {
            PluginCall removed = watchingCalls.remove(callbackId);
            if (removed != null) {
                removed.release(bridge);
            }
        }
        if (watchingCalls.size() == 0) {
            clearLocationUpdates();
        }
        call.success();
    }

    /**
     * Process a new location item and send it to any listening calls
     *
     * @param location
     */
    private void processLocation(Location location) {
        for (Map.Entry<String, PluginCall> watch : watchingCalls.entrySet()) {
            watch.getValue().success(getJSObjectForLocation(location));
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied location permission");
                return;
            }
        }

        if (savedCall.getMethodName().equals("getCurrentPosition")) {
            sendLocation(savedCall);
        } else if (savedCall.getMethodName().equals("watchPosition")) {
            startWatch(savedCall);
        } else {
            savedCall.resolve();
            savedCall.release(bridge);
        }
    }

    private JSObject getJSObjectForLocation(Location location) {
        JSObject ret = new JSObject();
        JSObject coords = new JSObject();
        ret.put("coords", coords);
        ret.put("timestamp", location.getTime());
        coords.put("latitude", location.getLatitude());
        coords.put("longitude", location.getLongitude());
        coords.put("accuracy", location.getAccuracy());
        coords.put("altitude", location.getAltitude());
        if (Build.VERSION.SDK_INT >= 26) {
            coords.put("altitudeAccuracy", location.getVerticalAccuracyMeters());
        }
        coords.put("speed", location.getSpeed());
        coords.put("heading", location.getBearing());
        return ret;
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates(final PluginCall call) {
        clearLocationUpdates();

        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = lm.getAllProviders();
        System.out.println(providers);

        String provider = LocationManager.GPS_PROVIDER;

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                call.success(getJSObjectForLocation(location));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                call.error("location unavailable");
                clearLocationUpdates();
            }
        };

        if (providers.contains(provider)) {
            lm.requestLocationUpdates(provider, 5000, 0, listener);
            System.out.println("geo: requestLocationUpdates");
        }
    }

    @SuppressLint("MissingPermission")
    private void clearLocationUpdates() {
        if (listener != null && lm != null) {
            lm.removeUpdates(listener);
            listener = null;
        }
    }
}
