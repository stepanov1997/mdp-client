package model;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    private String _id;
    private String token;
    private int _long;
    private int _lat;

    public Location(String _id, String token, int _long, int _lat) {
        this._id = _id;
        this.token = token;
        this._long = _long;
        this._lat = _lat;
    }

    public Location() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int get_long() {
        return _long;
    }

    public void set_long(int _long) {
        this._long = _long;
    }

    public int get_lat() {
        return _lat;
    }

    public void set_lat(int _lat) {
        this._lat = _lat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return get_id() == location.get_id() &&
                get_long() == location.get_long() &&
                get_lat() == location.get_lat() &&
                Objects.equals(getToken(), location.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get_id(), getToken(), get_long(), get_lat());
    }

    @Override
    public String toString() {
        return "Location{" +
                "_id=" + _id +
                ", token='" + token + '\'' +
                ", _long=" + _long +
                ", _lat=" + _lat +
                '}';
    }
}
