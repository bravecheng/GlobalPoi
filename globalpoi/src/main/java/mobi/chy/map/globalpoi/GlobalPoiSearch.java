package mobi.chy.map.globalpoi;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.chy.map.globalpoi.entity.AmapPoi;
import mobi.chy.map.globalpoi.entity.GlobalPoi;
import mobi.chy.map.globalpoi.entity.Location;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 全球POI搜索
 * <p>
 * Created by @author chengyong on 2018/2/23.
 */

public class GlobalPoiSearch {

    private int radius = 5000;
    private Context context;
    private PoiSearchListener listener;
    private Handler mainHandler;

    public GlobalPoiSearch(Context context) {
        this.context = context;
        mainHandler = new Handler(context.getMainLooper());
    }

    public void setOnPoiSearchListener(PoiSearchListener listener) {
        this.listener = listener;
    }

    public interface PoiSearchListener {
        void onPoiSearchSuccess(List<GlobalPoi> poiList);

        void onPoiSearchFailed(int errCode, String errDesc);

        void onPoiSearchFinish();
    }

    public void searchKeywordsAsyn(String keywords, String city, int page) {
        if (TextUtils.isEmpty(keywords)) {
            if (listener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPoiSearchFailed(-400, "keyword is null!");
                        listener.onPoiSearchFinish();
                    }
                });
            }
            return;
        }
        searchKeywords(keywords, city, page);
    }

    public void searchLatLngAsyn(double lat, double lng, int radius) {
        this.radius = radius;
        searchLatLngAsyn(lat, lng);
    }

    public void searchLatLngAsyn(double lat, double lng) {
        //验证经纬度合法性
        if (!LbsTool.isVerifyPass(lat, lng)) {
            if (listener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPoiSearchFailed(-300, "lat or lng error!");
                        listener.onPoiSearchFinish();
                    }
                });
            }
            return;
        }
        //判断经纬度是否在中国范围内
        if (LbsTool.isInChina(lat, lng)) {
            //如果点在国内，获取高德
            getAMapPoi(lat, lng, radius);
        } else {
            //如果点在国外，获取Foursquare
            getFoursquareVenues(lat, lng, radius);
        }
    }

    private void searchKeywords(String keywords, String city, int page) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = AMapUtil.getRequestUrl(context, keywords, city, page);
        Log.e("AMap request", url);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiSearchFailed(-200, "network error!");
                            listener.onPoiSearchFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("AMap response", result);
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    int responseCode = 0;
                    String info = "OK";
                    if (jsonResult.has("infocode")) {
                        responseCode = jsonResult.optInt("infocode");
                    }
                    if (jsonResult.has("info")) {
                        info = jsonResult.optString("info");
                    }
                    //状态码 = 200 表示可用
                    if (responseCode == 10000 && jsonResult.has("pois") && listener != null) {
                        List<AmapPoi> pois = JSON.parseArray(jsonResult.optString("pois"), AmapPoi.class);
                        final ArrayList<GlobalPoi> poiList = new ArrayList<>();
                        //将AmapPoi转换为GlobalPoi
                        for (AmapPoi amap : pois) {
                            GlobalPoi globalPoi = new GlobalPoi();
                            globalPoi.setId(amap.getId());
                            globalPoi.setName(amap.getName());
                            Location location = new Location();
                            location.setAddress(amap.getAddress());
                            location.setCitycode(amap.getCitycode());
                            location.setCity(amap.getCityname());
                            location.setState(amap.getPname());
                            location.setCountry("中国");
                            location.setPostalCode(amap.getAdcode());
                            location.setLat(amap.getLatitude());
                            location.setLng(amap.getLongitude());
                            globalPoi.setLocation(location);
                            poiList.add(globalPoi);
                        }
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPoiSearchSuccess(poiList);
                                listener.onPoiSearchFinish();
                            }
                        });
                    } else {
                        if (listener != null) {
                            final int finalResponseCode = responseCode;
                            final String finalInfo = info;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onPoiSearchFailed(finalResponseCode, finalInfo);
                                    listener.onPoiSearchFinish();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                }
            }
        });
    }

    /**
     * 国内坐标使用AMap搜索
     */
    private void getAMapPoi(double lat, double lng, int radius) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = AMapUtil.getRequestUrl(context, lat, lng, radius);
        Log.e("AMap request", url);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiSearchFailed(-200, "network error!");
                            listener.onPoiSearchFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("AMap response", result);
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    int responseCode = 0;
                    String info = "OK";
                    if (jsonResult.has("infocode")) {
                        responseCode = jsonResult.optInt("infocode");
                    }
                    if (jsonResult.has("info")) {
                        info = jsonResult.optString("info");
                    }
                    //状态码 = 200 表示可用
                    if (responseCode == 10000 && jsonResult.has("pois") && listener != null) {
                        List<AmapPoi> pois = JSON.parseArray(jsonResult.optString("pois"), AmapPoi.class);
                        final ArrayList<GlobalPoi> poiList = new ArrayList<>();
                        //将AmapPoi转换为GlobalPoi
                        for (AmapPoi amap : pois) {
                            GlobalPoi globalPoi = new GlobalPoi();
                            globalPoi.setId(amap.getId());
                            globalPoi.setName(amap.getName());
                            Location location = new Location();
                            location.setAddress(amap.getAddress());
                            location.setCitycode(amap.getCitycode());
                            location.setCity(amap.getCityname());
                            location.setState(amap.getPname());
                            location.setCountry("中国");
                            location.setPostalCode(amap.getAdcode());
                            location.setLat(amap.getLatitude());
                            location.setLng(amap.getLongitude());
                            globalPoi.setLocation(location);
                            poiList.add(globalPoi);
                        }
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPoiSearchSuccess(poiList);
                                listener.onPoiSearchFinish();
                            }
                        });
                    } else {
                        if (listener != null) {
                            final int finalResponseCode = responseCode;
                            final String finalInfo = info;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onPoiSearchFailed(finalResponseCode, finalInfo);
                                    listener.onPoiSearchFinish();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                }
            }
        });
    }

    /**
     * 国外坐标使用Foursquare搜索
     */
    private void getFoursquareVenues(double lat, double lng, int radius) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = FoursquareUtil.getRequestUrl(context, lat, lng, radius);
        Log.e("Foursquare request", url);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiSearchFailed(-200, "network error!");
                            listener.onPoiSearchFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("Foursquare response", result);
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    int responseCode = 0;
                    String errorType = "OK";
                    if (jsonResult.has("meta")) {
                        JSONObject meta = jsonResult.getJSONObject("meta");
                        if (meta.has("code")) {
                            responseCode = meta.optInt("code");
                        }
                        if (meta.has("errorType")) {
                            errorType = meta.optString("errorType");
                        }
                    }
                    //状态码 = 200 表示可用
                    if (responseCode == 200 && jsonResult.has("response") && listener != null) {
                        JSONObject jsonResponse = jsonResult.getJSONObject("response");
                        final List<GlobalPoi> poiList = JSON.parseArray(jsonResponse.optString("venues"), GlobalPoi.class);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPoiSearchSuccess(poiList);
                                listener.onPoiSearchFinish();
                            }
                        });
                    } else {
                        if (listener != null) {
                            final int finalResponseCode = responseCode;
                            final String finalErrorType = errorType;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onPoiSearchFailed(finalResponseCode, finalErrorType);
                                    listener.onPoiSearchFinish();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                }
            }
        });
    }
}
