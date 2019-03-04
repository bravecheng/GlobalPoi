package mobi.chy.map.globalpoi;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import mobi.chy.map.globalpoi.entity.GlobalPoi;
import mobi.chy.map.globalpoi.util.AMapUtil;
import mobi.chy.map.globalpoi.util.FoursquareUtil;
import mobi.chy.map.globalpoi.util.GoogleMapUtil;
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

    private int radius = 5000;
    private Context context;
    private PoiSearchListener listener;
    private Handler mainHandler;
    private boolean isFsqeEnable = false;
    private boolean isGMapEnable = false;

    public GlobalPoiSearch(Context context) {
        this.context = context;
        mainHandler = new Handler(context.getMainLooper());
        isFsqeEnable = FoursquareUtil.init(context);
        isGMapEnable = GoogleMapUtil.init(context);
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
        if (!LbsTool.isInChina(lat, lng) && isFsqeEnable) {
            //如果点在国外，获取Foursquare
            getFoursquareVenues(lat, lng, radius);
        } else if (!LbsTool.isInChina(lat, lng) && isGMapEnable) {
            String url = GoogleMapUtil.getLatLngUrl(lat, lng, radius);
            getGoogleMaps(url, lat, lng, radius);
        } else {
            //如果点在国内，获取高德
            getAMapPoi(lat, lng, radius);
        }
    }

    private void searchKeywords(String keywords, String city, int page) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = AMapUtil.getKeywordsUrl(context, keywords, city, page);
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
                        final List<GlobalPoi> poiList = GlobalPoi.getBeanFromAmap(jsonResult.optString("pois"));
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
        String url = AMapUtil.getLatLngUrl(context, lat, lng, radius);
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
                        final List<GlobalPoi> poiList = GlobalPoi.getBeanFromAmap(jsonResult.optString("pois"));
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
        String url = FoursquareUtil.getLatLngUrl(lat, lng, radius);
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
                        final List<GlobalPoi> poiList = GlobalPoi.getBeanFromFoursquare(jsonResponse.optString("venues"));
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

    /**
     * 国外坐标使用Foursquare搜索
     */
    private void getGoogleMaps(String url, double lat, double lng, int radius) {
        OkHttpClient okHttpClient = new OkHttpClient();
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
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    int responseCode = 0;
                    String errorType = "OK";
                    if (jsonResult.has("status")) {
                        String status = jsonResult.optString("status");
                        if ("OK".equals(status)) {
                            responseCode = 200;
                        } else {
                            responseCode = 0;
                        }
                    }
                    //状态码 = 200 表示可用
                    if (responseCode == 200 && jsonResult.has("results") && listener != null) {
                        final List<GlobalPoi> poiList = GlobalPoi.getBeanFromGoogleMaps(jsonResult.optString("results"));
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
