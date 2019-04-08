package mobi.chy.map.globalpoi.entity;


/**
 * 国内 GCJ02坐标，国外 WGS84坐标
 * <p>
 * Created by @author chengyong on 2018/2/23.
 */

public class Location {

    private double lat;
    private double lng;
    /**
     * 简单地址描述
     */
    private String address;
    /**
     * 格式化地址输出
     */
    private String formattedAddress = "";
    /**
     * 邮政编码，国内基本为空
     */
    private String postalCode = "";
    /**
     * 城市代码，国外基本为空
     */
    private String cityCode = "";
    /**
     * 国家代码
     */
    private String countryCode = "";
    /**
     * 区，县一级名称，国外基本为空
     */
    private String district = "";
    /**
     * 城市，地级市
     */
    private String city;
    /**
     * 州，省
     */
    private String state;
    /**
     * 国家
     */
    private String country;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
