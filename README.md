# GlobalPoi
[ ![Download](https://api.bintray.com/packages/bravecheng/maven/GlobalPoiSearch/images/download.svg) ](https://bintray.com/bravecheng/maven/GlobalPoiSearch/_latestVersion)

Android版基于高德地图和Foursquare的全球Poi搜索工具，整合后实现类似于微信的搜索效果，国内显示从高德搜索到的数据，在国外显示从Foursquare搜索到的结果，使得结果尽可能的准确。

## How to used

### 1. You need to register AMap key & Foursquare App.  

~~go to [AMap Console](http://lbs.amap.com/dev/)~~ Already Built in.
go to [Foursquare Developers](https://foursquare.com/developers/apps).  

### 2. Add gradle dependency.
```xml
compile 'mobi.chy:GlobalPoiSearch:0.5.0'
```

### 3. Add the corresponding key in the manifest file.
```xml
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
