package mobi.chy.map.globalpoi.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.chy.map.globalpoi.entity.GlobalPoi;
import mobi.chy.map.globalpoi.entity.Location;

/**
 * Foursuqare工具类
 *
 * Created by @author chengyong on 2018/2/23.
 */

public class FoursquareUtil {

    private FoursquareUtil(){}

    private static final String BASE_URL = "https://api.foursquare.com/v2/venues/search?v=20161016";
    private static String CLIENT_ID = "TESBVHX5NCPPLUO51RZ15IVXZITDSFTXXEWHQ2UGTEP5VGB3";
    private static String CLIENT_SECRET = "VK1PN0LUSLXTQSHSPEYX4222P0SBEXWQIGOETBHQTBXWEOHQ";

    public static void init(Context context){
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            CLIENT_ID = appInfo.metaData.getString("FOURSQUARE_CLIENT_ID");
            CLIENT_SECRET = appInfo.metaData.getString("FOURSQUARE_CLIENT_SECRET");
        } catch (Exception e) {
        }
    }

    public static String getKeywordsUrl(String keywords, String city){
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append("&limit=50");
        sb.append("&query=");
        sb.append(keywords);
        sb.append("&client_id=");
        sb.append(CLIENT_ID);
        sb.append("&client_secret=");
        sb.append(CLIENT_SECRET);
        sb.append("&near=");
        sb.append(city);
        return sb.toString();
    }

    public static String getLatLngUrl(double lat, double lng, int radius){
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append("&limit=50");
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

    public static List<GlobalPoi> getBeanFromFoursquare(String result) {
        ArrayList<GlobalPoi> globalPois = new ArrayList<>();
        try {
            JSONArray pois = new JSONArray(result);
            for (int i = 0; i < pois.length(); i++) {
                JSONObject poiJsonResult = pois.getJSONObject(i);
                GlobalPoi globalPoi = new GlobalPoi();
                globalPoi.setId(poiJsonResult.optString("id"));
                globalPoi.setName(poiJsonResult.optString("name"));
                JSONObject locationJsonResult = poiJsonResult.getJSONObject("location");
                Location location = new Location();
                location.setAddress(locationJsonResult.optString("address"));
                location.setCitycode(locationJsonResult.optString("citycode"));
                location.setCity(locationJsonResult.optString("city"));
                location.setState(locationJsonResult.optString("state"));
                location.setCountry(locationJsonResult.optString("country"));
                location.setPostalCode(locationJsonResult.optString("postalCode"));
                location.setLat(locationJsonResult.optDouble("lat"));
                location.setLng(locationJsonResult.optDouble("lng"));
                globalPoi.setLocation(location);
                globalPois.add(globalPoi);
            }
        } catch (JSONException e) {
        }
        return globalPois;
    }
}
