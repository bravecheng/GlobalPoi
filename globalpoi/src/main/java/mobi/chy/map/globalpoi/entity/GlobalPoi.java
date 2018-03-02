package mobi.chy.map.globalpoi.entity;

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
}
