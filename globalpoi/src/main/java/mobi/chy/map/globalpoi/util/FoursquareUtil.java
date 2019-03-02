package mobi.chy.map.globalpoi.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * Foursuqare工具类
 *
 * Created by @author chengyong on 2018/2/23.
 */

public class FoursquareUtil {

    private FoursquareUtil(){}

    private static final String BASE_URL = "https://api.foursquare.com/v2/venues/search?v=20161016";
    private static String PKG_NAME, CLIENT_ID, CLIENT_SECRET;

    public static boolean init(Context context){
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(getPkgName(context), PackageManager.GET_META_DATA);
            CLIENT_ID = appInfo.metaData.getString("FOURSQUARE_CLIENT_ID");
            CLIENT_SECRET = appInfo.metaData.getString("FOURSQUARE_CLIENT_SECRET");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            CLIENT_ID = "ERROR_KEY";
            CLIENT_SECRET = "ERROR_KEY";
            return false;
        }
    }

    public static String getLatLngUrl(double lat, double lng, int radius){
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append("&radius=");
        sb.append(radius);
        sb.append("&client_id=");
        sb.append(CLIENT_ID);
        sb.append("&client_secret=");
        sb.append(CLIENT_SECRET);
        sb.append("&ll=");
        sb.append(lat);
        sb.append(",");
        sb.append(lng);
        return sb.toString();
    }

    public static String getPkgName(Context context) {
        if (TextUtils.isEmpty(PKG_NAME)) {
            PKG_NAME = context.getPackageName();
        }
        return PKG_NAME;
    }
}
