# GlobalPoi
Global Poi Search

## How to used

### 1. You need to register AMap key & Foursquare App.  

go to AMap Key.  
go to Foursquare App.  

### 2. Add gradle dependency.
```xml
compile 'mobi.chy:GlobalPoiSearch:0.2.1'
```

### 3. Add the corresponding key in the manifest file.
```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="your amap key" />
<meta-data
    android:name="FOURSQUARE_CLIENT_ID"
    android:value="your CLIENT_ID" />
<meta-data
    android:name="FOURSQUARE_CLIENT_SECRET"
    android:value="your CLIENT_SECRET" />
```

### 4. Add the following code where you need it. 
```java
GlobalPoiSearch globalPoiSearch = new GlobalPoiSearch(this);
globalPoiSearch.setOnPoiSearchListener(new GlobalPoiSearch.PoiSearchListener{

	@Override
    public void onPoiSearchSuccess(List<GlobalPoi> poiList) {
        //search success, do your work...
    }

    @Override
    public void onPoiSearchFailed(int errCode, String errDesc) {
    	//search failed, do your work...
    }

    @Override
    public void onPoiSearchFinish() {
    	//search finished.
    }

});

//search a latitude and longitude point.
globalPoiSearch.searchLatLngAsyn(lat, lng);

//search a nearby place by keywords.
globalPoiSearch.searchKeywordsAsyn(placeKeywords,city,pageIndex);
```
