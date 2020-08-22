package model;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    private String _id;
    private String token;
    private String from;
    private String to;
    private int _long;
    private int _lat;
    private String dateTime;

    public Location() {
    }

    public Location(String id, String token, int _long, int _lat, String from, String to, String dateTime) {
        this._id = id;
        this.token = token;
        this._long = _long;
        this._lat = _lat;
        this.from = from;
        this.to = to;
        this.dateTime = dateTime;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return get_long() == location.get_long() &&
                get_lat() == location.get_lat() &&
                Objects.equals(get_id(), location.get_id()) &&
                Objects.equals(getToken(), location.getToken()) &&
                Objects.equals(getFrom(), location.getFrom()) &&
                Objects.equals(getTo(), location.getTo()) &&
                Objects.equals(getDateTime(), location.getDateTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get_id(), getToken(), get_long(), get_long(), getFrom(), getTo(), get_long(), get_lat(), getDateTime());
    }

    @Override
    public String toString() {
        return "Location{" +
                "_id='" + _id + '\'' +
                ", token='" + token + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", _long=" + _long +
                ", _lat=" + _lat +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
