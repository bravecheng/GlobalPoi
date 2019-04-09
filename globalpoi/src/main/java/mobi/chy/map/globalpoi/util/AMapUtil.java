package mobi.chy.map.globalpoi.util;

import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import mobi.chy.map.globalpoi.entity.GlobalPoi;
import mobi.chy.map.globalpoi.entity.Location;

/**
 * 高德工具类
 * <p>
 * Created by @author chengyong on 2018/2/3.
 */

public class AMapUtil {

    private AMapUtil(){}

    private static final String AROUND_URL = "http://restapi.amap.com/v3/place/around?";
    private static final String TEXT_URL = "http://restapi.amap.com/v3/place/text?";
    private static final String SHA1_VALUE = "92:88:41:C9:3F:02:61:10:14:C5:AB:3B:2F:7C:F9:8B:49:9B:70:FB";
    private static final String PKG_NAME = "com.iwear.mytracks";
    private static final String AMAP_KEY = "08ef30b749bff6abee13b141ef020b0d";

    public static String getKeywordsUrl(String keywords, String city, int page){
        TreeMap<String, String> tm = new TreeMap<>();
        tm.put("output", "json");
        tm.put("sortrule", "distance");
        tm.put("offset", "20");
        tm.put("extensions", "all");
        tm.put("citylimit", "false");
        tm.put("children", "0");
        tm.put("language", "zh-CN");
        tm.put("keywords", keywords);
        if (!TextUtils.isEmpty(city)) {
            tm.put("city", city);
        }
        tm.put("page", "" + page);
        tm.put("key", "" + AMAP_KEY);
        //生成ts 倒数第1位0~9，倒数第2位0~1
        String ts = System.currentTimeMillis() + "";
        Random random = new Random();
        ts = ts.substring(0, ts.length() - 2) + random.nextInt(2) + random.nextInt(10);
        //计算scode
        String scode = getMD5(SHA1_VALUE + ":" + PKG_NAME + ":" + ts.substring(0, ts.length() - 3) + ":" + treeMapStr(tm));
        tm.put("ts", ts);
        tm.put("scode", scode);
        return TEXT_URL + treeMapStr(tm);
    }

    public static String getLatLngUrl(double lat, double lng, int radius, int pageIndex) {
        TreeMap<String, String> tm = new TreeMap<>();
        tm.put("output", "json");
        //主要显示大型地点，例如商场、公司、银行、加油站、大型餐厅、政府机关、医院等等
        tm.put("types","010100|050100|050200|060100|070400|070500|070600|080100|080600|090100|100100|110000|120000|130100|130500|140000|150104|150200|150300|160100|160400|170000|180200|180300");
        tm.put("sortrule", "distance");
        tm.put("offset", "20");
        tm.put("extensions", "all");
        tm.put("citylimit", "false");
        tm.put("children", "0");
        tm.put("language", "zh-CN");
        tm.put("page", "" + (pageIndex +1));
        tm.put("radius", "" + radius);
        tm.put("location", lat + "," + lng);
        tm.put("key", "" + AMAP_KEY);
        //生成ts 倒数第1位0~9，倒数第2位0~1
        String ts = System.currentTimeMillis() + "";
        Random random = new Random();
        ts = ts.substring(0, ts.length() - 2) + random.nextInt(2) + random.nextInt(10);
        //计算scode
        String scode = getMD5(SHA1_VALUE + ":" + PKG_NAME + ":" + ts.substring(0, ts.length() - 3) + ":" + treeMapStr(tm));
        tm.put("ts", ts);
        tm.put("scode", scode);
        return AROUND_URL + treeMapStr(tm);
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

    /**
     * 获取MD5的值
     *
     * @param str
     * @return
     */
    private static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(str.getBytes());
            byte[] byteArray = md.digest();
            StringBuffer md5StrBuff = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
                }
            }
            str = md5StrBuff.toString();
            if (str == null || str.length() == 0) {
                return "";
            } else {
                return str.toLowerCase();
            }
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
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
                JSONArray postCode = poiJsonResult.optJSONArray("postcode");
                if (postCode != null && postCode.length() > 0) {
                    location.setPostalCode("" + postCode.optString(0));
                }
                location.setCityCode(poiJsonResult.optString("citycode"));
                location.setCountryCode("0086");
                location.setDistrict(poiJsonResult.optString("adname"));
                location.setCity(poiJsonResult.optString("cityname"));
                location.setState(poiJsonResult.optString("pname"));
                location.setCountry("中国");
                location.setFormattedAddress(location.getState()+location.getCity()+location.getDistrict()+location.getAddress());
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

}
