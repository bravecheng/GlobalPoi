package mobi.chy.map.globalpoi.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mobi.chy.map.globalpoi.entity.GlobalPoi;
import mobi.chy.map.globalpoi.entity.Location;

/**
 * Google Maps工具类
 * <p>
 * Created by @author chengyong on 2019/3/2.
 */

public class GoogleMapUtil {

    private GoogleMapUtil(){}

    private static final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static String GMAP_KEY;

    public static boolean init(Context context){
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            GMAP_KEY = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            GMAP_KEY = "ERROR_KEY";
            return false;
        }
    }

    public static String getKeywordsUrl(String keywords, String city){
        TreeMap<String, String> tm = new TreeMap<>();
        tm.put("keywords", keywords);
        tm.put("key", "" + GMAP_KEY);
        return TEXT_SEARCH_URL + treeMapStr(tm);
    }

    public static String getLatLngUrl(double lat, double lng, int radius) {
        TreeMap<String, String> tm = new TreeMap<>();
        tm.put("radius", "" + radius);
        tm.put("location", lat + "," + lng);
        tm.put("key", "" + GMAP_KEY);
        return NEARBY_SEARCH_URL + treeMapStr(tm);
    }

    /**
     * 将TreeMap转换为字符串的形式 <br>
     *
     * @param treeMap parmas
     * @return String
     */
    private static String treeMapStr(TreeMap treeMap) {
        StringBuilder sb = new StringBuilder();
        if (treeMap != null) {
            Iterator<Map.Entry> it = treeMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = it.next();
                String key = entry.getKey().toString();
                Object valueObj = entry.getValue();
                String value;
                if (valueObj instanceof Object[]) {
                    Object[] valueArr = (Object[]) valueObj;
                    value = valueArr[0].toString();
                } else {
                    value = valueObj.toString();
                }
                sb.append(key + "=" + value + "&");
            }
        }
        String str = sb.toString();
        return str.substring(0,str.length()-1);
    }

    public static List<GlobalPoi> getBeanFromGoogleMaps(String result){
        ArrayList<GlobalPoi> globalPois = new ArrayList<>();
        try {
            JSONArray pois = new JSONArray(result);
            for (int i = 0; i < pois.length(); i++) {
                JSONObject poiJsonResult = pois.getJSONObject(i);
                GlobalPoi globalPoi = new GlobalPoi();
                globalPoi.setId(poiJsonResult.optString("place_id"));
                globalPoi.setName(poiJsonResult.optString("name"));
                Location location = new Location();
                location.setAddress(poiJsonResult.optString("vicinity"));
                location.setLat(poiJsonResult.optJSONObject("geometry").optJSONObject("location").optDouble("lat"));
                location.setLng(poiJsonResult.optJSONObject("geometry").optJSONObject("location").optDouble("lng"));
                globalPoi.setLocation(location);
                globalPois.add(globalPoi);
            }
        } catch (JSONException e) {
        }
        return globalPois;
    }
}
