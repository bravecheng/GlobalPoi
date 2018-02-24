package mobi.chy.map.globalpoi;


public class AmapPoi {

    private String id;
    private String name;
    private String address;
    private String location;
    private String cityname;
    private String pname;
    private String adcode;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getAdcode() {
        return adcode;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public double getLatitude() {
        try {
            String[] latLng = location.split(",");
            return Double.valueOf(latLng[1]);
        } catch (Exception e) {
            return 0.0F;
        }
    }

    public double getLongitude() {
        try {
            String[] latLng = location.split(",");
            return Double.valueOf(latLng[0]);
        } catch (Exception e) {
            return 0.0F;
        }
    }
}
