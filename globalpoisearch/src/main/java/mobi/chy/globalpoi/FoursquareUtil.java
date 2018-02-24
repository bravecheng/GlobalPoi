package mobi.chy.globalpoi;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by @author chengyong on 2018/2/23.
 */

public class FoursquareUtil {

    private FoursquareUtil(){}

    private static final String BASE_URL = "https://api.foursquare.com/v2/venues/search?v=20161016";

    public static String getRequestUrl(Context context, double lat, double lng, int radius){
        String clientId,clientSecret;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            clientId = appInfo.metaData.getString("FOURSQUARE_CLIENT_ID");
            clientSecret = appInfo.metaData.getString("FOURSQUARE_CLIENT_SECRET");
        } catch (PackageManager.NameNotFoundException e) {
            clientId = "ERROR_KEY";
            clientSecret = "ERROR_KEY";
        }
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append("&radius=");
        sb.append(radius);
        sb.append("&client_id=");
        sb.append(clientId);
        sb.append("&client_secret=");
        sb.append(clientSecret);
        sb.append("&ll=");
        sb.append(lat);
        sb.append(",");
        sb.append(lng);
        return sb.toString();
    }
}
