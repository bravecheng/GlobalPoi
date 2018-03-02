package mobi.chy.map.globalpoi.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 全球Poi搜索，国内传入GCJ02坐标，国外传入WGS84坐标，返回数据坐标同传入坐标
 * <p>
 * Created by @author chengyong on 2018/2/12.
 */

public class GlobalPoi {

    private String id;
    private String name;
    private Location location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static List<GlobalPoi> getBeanFromAmap(String result) {
        ArrayList<GlobalPoi> globalPois = new ArrayList<>();
        try {
            JSONArray pois = new JSONArray(result);
            for (int i = 0; i < pois.length(); i++) {
                JSONObject poiJsonResult = pois.getJSONObject(i);
                GlobalPoi globalPoi = new GlobalPoi();
                globalPoi.setId(poiJsonResult.optString("id"));
                globalPoi.setName(poiJsonResult.optString("name"));
                Location location = new Location();
                location.setAddress(poiJsonResult.optString("address"));
                location.setCitycode(poiJsonResult.optString("citycode"));
                location.setCity(poiJsonResult.optString("cityname"));
                location.setState(poiJsonResult.optString("pname"));
                location.setCountry("中国");
                location.setPostalCode(poiJsonResult.optString("adcode"));
                try {
                    String[] latLng = poiJsonResult.optString("location").split(",");
                    location.setLat(Double.valueOf(latLng[1]));
                    location.setLng(Double.valueOf(latLng[0]));
                } catch (Exception e) {
                    location.setLat(0F);
                    location.setLng(0F);
                }
                globalPoi.setLocation(location);
                globalPois.add(globalPoi);
            }
        } catch (JSONException e) {
        }
        return globalPois;
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
