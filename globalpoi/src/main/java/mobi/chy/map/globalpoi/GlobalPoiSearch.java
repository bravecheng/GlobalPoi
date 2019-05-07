package mobi.chy.map.globalpoi;

import android.content.Context;
import android.os.Handler;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import mobi.chy.map.globalpoi.entity.GlobalPoi;
import mobi.chy.map.globalpoi.util.AMapUtil;
import mobi.chy.map.globalpoi.util.FoursquareUtil;
import mobi.chy.map.globalpoi.util.LbsTool;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 全球POI搜索，包含点位搜索和关键字搜索
 * <p>
 * Created by @author chengyong on 2018/2/23.
 */

public class GlobalPoiSearch {

    private int radius = 3000;
    private PoiSearchListener mPoiSearchListener;
    private PoiDetailListener mPoiDetailListener;
    private Call mCall;
    private Handler mainHandler;

    public GlobalPoiSearch(Context context) {
        mainHandler = new Handler(context.getMainLooper());
        FoursquareUtil.init(context);
    }

    public void setOnPoiSearchListener(PoiSearchListener listener) {
        this.mPoiSearchListener = listener;
    }

    public void setOnPoiDetailListener(PoiDetailListener listener) {
        this.mPoiDetailListener = listener;
    }

    public interface PoiSearchListener {
        void onPoiSearchSuccess(int totalCount, List<GlobalPoi> poiList);
        void onPoiSearchFailed(int errCode, String errDesc);
        void onPoiSearchFinish();
    }

    public interface PoiDetailListener {
        void onPoiDetailSuccess(GlobalPoi globalPoi);
        void onPoiDetailFailed(int errCode, String errDesc);
        void onPoiDetailFinish();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     *  根据经纬度查询详细地址信息
     *
     * @param lat
     * @param lng
     */
    public void queryDetail(double lat, double lng) {
        //验证经纬度合法性
        if (!LbsTool.isVerifyPass(lat, lng)) {
            if (mPoiDetailListener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPoiDetailListener.onPoiDetailFailed(-300, "lat or lng error!");
                        mPoiDetailListener.onPoiDetailFinish();
                    }
                });
            }
            return;
        }
        if (mCall != null && mCall.isExecuted()) {
            mCall.cancel();
        }
        if (!LbsTool.isInChina(lat, lng)) {
            //如果点在国外，获取Foursquare
            getFoursquareDetail(lat, lng);
        } else {
            //如果点在国内，获取高德
            getAMapDetail(lat, lng);
        }
    }

    /**
     *  根据经纬度搜索周边Poi，pageIndex从0开始
     *
     * @param lat
     * @param lng
     * @param pageIndex
     */
    public void queryLatLng(double lat, double lng, int pageIndex) {
        //验证经纬度合法性
        if (!LbsTool.isVerifyPass(lat, lng)) {
            if (mPoiSearchListener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPoiSearchListener.onPoiSearchFailed(-300, "lat or lng error!");
                        mPoiSearchListener.onPoiSearchFinish();
                    }
                });
            }
            return;
        }
        if (mCall != null && mCall.isExecuted()) {
            mCall.cancel();
        }
        if (!LbsTool.isInChina(lat, lng)) {
            //如果点在国外，获取Foursquare
            getFoursquareVenues(lat, lng);
        } else {
            //如果点在国内，获取高德
            getAMapPoi(lat, lng, pageIndex);
        }
    }

    public List<GlobalPoi> queryAMapSync(String keywords, String city, int page){
        if (mCall != null && mCall.isExecuted()) {
            mCall.cancel();
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = AMapUtil.getKeywordsUrl(keywords, city, page);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        try {
            return parseAMapToList(mCall.execute(), null);
        } catch (IOException e) {
        }
        return null;
    }

    public void queryAMap(String keywords, String city, int page) {
        if (mCall != null && mCall.isExecuted()) {
            mCall.cancel();
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = AMapUtil.getKeywordsUrl(keywords, city, page);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mPoiSearchListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPoiSearchListener.onPoiSearchFailed(-200, "network error!");
                            mPoiSearchListener.onPoiSearchFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseAMapToList(response, mPoiSearchListener);
            }
        });
    }

    public List<GlobalPoi> queryFoursquareSync(String keywords, String city) {
        if (mCall != null && mCall.isExecuted()) {
            mCall.cancel();
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = FoursquareUtil.getKeywordsUrl(keywords, city);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        try {
            return parseFoursquareToList(mCall.execute(), null);
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * 地址使用Foursquare搜索
     */
    public void queryFoursquare(String keywords, String city) {
        if (mCall != null && mCall.isExecuted()) {
            mCall.cancel();
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = FoursquareUtil.getKeywordsUrl(keywords, city);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mPoiSearchListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPoiSearchListener.onPoiSearchFailed(-200, "network error!");
                            mPoiSearchListener.onPoiSearchFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseFoursquareToList(response, mPoiSearchListener);
            }
        });
    }

    /**
     * 国内坐标使用AMap搜索
     */
    private void getAMapPoi(double lat, double lng, int pageIndex) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = AMapUtil.getLatLngUrl(lat, lng, radius, pageIndex);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mPoiSearchListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPoiSearchListener.onPoiSearchFailed(-200, "network error!");
                            mPoiSearchListener.onPoiSearchFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseAMapToList(response, mPoiSearchListener);
            }
        });
    }

    /**
     * 国外坐标使用Foursquare搜索
     */
    private void getFoursquareVenues(double lat, double lng) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = FoursquareUtil.getLatLngUrl(lat, lng, radius);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mPoiSearchListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPoiSearchListener.onPoiSearchFailed(-200, "network error!");
                            mPoiSearchListener.onPoiSearchFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseFoursquareToList(response, mPoiSearchListener);
            }
        });
    }


    /**
     * 国内坐标使用AMap搜索逆地理
     */
    private void getAMapDetail(final double lat, final double lng) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = AMapUtil.getDetailUrl(lat, lng);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mPoiDetailListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPoiDetailListener.onPoiDetailFailed(-200, "network error!");
                            mPoiDetailListener.onPoiDetailFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseAMapToBean(lat, lng, response, mPoiDetailListener);
            }
        });
    }

    /**
     * 国外坐标使用Foursquare搜索逆地理
     */
    private void getFoursquareDetail(double lat, double lng) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = FoursquareUtil.getLatLngUrl(lat, lng, radius);
        Request request = new Request.Builder().url(url).method("GET", null).build();
        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mPoiDetailListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPoiDetailListener.onPoiDetailFailed(-200, "network error!");
                            mPoiDetailListener.onPoiDetailFinish();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseFoursquareToBean(response, mPoiDetailListener);
            }
        });
    }

    private GlobalPoi parseAMapToBean(double lat, double lng, Response response, final PoiDetailListener listener) throws IOException {
        String result = response.body().string();
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
            if (responseCode == 10000 && jsonResult.has("regeocode")) {
                final GlobalPoi globalPoi = AMapUtil.getRegeoFromAMap(jsonResult.optString("regeocode"), lat, lng);
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiDetailSuccess(globalPoi);
                            listener.onPoiDetailFinish();
                        }
                    });
                }
                return globalPoi;
            } else {
                if (listener != null) {
                    final int finalResponseCode = responseCode;
                    final String finalInfo = info;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiDetailFailed(finalResponseCode, finalInfo);
                            listener.onPoiDetailFinish();
                        }
                    });
                }
            }
        } catch (JSONException e) {
        }
        return null;
    }

    private List<GlobalPoi> parseAMapToList(Response response, final PoiSearchListener listener) throws IOException {
        String result = response.body().string();
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
            if (responseCode == 10000 && jsonResult.has("pois")) {
                final int totalCount = jsonResult.optInt("count") / 20;
                final List<GlobalPoi> poiList = AMapUtil.getBeanFromAmap(jsonResult.optString("pois"));
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiSearchSuccess(totalCount, poiList);
                            listener.onPoiSearchFinish();
                        }
                    });
                }
                return poiList;
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
        return null;
    }

    private GlobalPoi parseFoursquareToBean(Response response, final PoiDetailListener listener) throws IOException {
        String result = response.body().string();
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
            if (responseCode == 200 && jsonResult.has("response")) {
                JSONObject jsonResponse = jsonResult.getJSONObject("response");
                final List<GlobalPoi> poiList = FoursquareUtil.getBeanFromFoursquare(jsonResponse.optString("venues"));
                GlobalPoi globalPoi = null;
                for (GlobalPoi poi: poiList) {
                    if (globalPoi == null || globalPoi.getLocation().getDistance() > poi.getLocation().getDistance()) {
                        globalPoi = poi;
                    }
                }
                globalPoi.getLocation().setDistance(0);
                final GlobalPoi finalPoi = globalPoi;
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiDetailSuccess(finalPoi);
                            listener.onPoiDetailFinish();
                        }
                    });
                }
                return globalPoi;
            } else {
                if (listener != null) {
                    final int finalResponseCode = responseCode;
                    final String finalErrorType = errorType;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiDetailFailed(finalResponseCode, finalErrorType);
                            listener.onPoiDetailFinish();
                        }
                    });
                }
            }
        } catch (JSONException e) {
        }
        return null;
    }

    private List<GlobalPoi> parseFoursquareToList(Response response, final PoiSearchListener listener) throws IOException {
        String result = response.body().string();
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
            if (responseCode == 200 && jsonResult.has("response")) {
                JSONObject jsonResponse = jsonResult.getJSONObject("response");
                final List<GlobalPoi> poiList = FoursquareUtil.getBeanFromFoursquare(jsonResponse.optString("venues"));
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPoiSearchSuccess(1, poiList);
                            listener.onPoiSearchFinish();
                        }
                    });
                }
                return poiList;
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
        return null;
    }
}
