package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoResponse {
    private String status;
    private String info;
    private String infocode;
    private Regeocode regeocode;

    // 无参构造
    public GeoResponse() {}

    // 全参构造
    public GeoResponse(String status, String info, String infocode, Regeocode regeocode) {
        this.status = status;
        this.info = info;
        this.infocode = infocode;
        this.regeocode = regeocode;
    }

    // Getter/Setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public String getInfocode() { return infocode; }
    public void setInfocode(String infocode) { this.infocode = infocode; }

    public Regeocode getRegeocode() { return regeocode; }
    public void setRegeocode(Regeocode regeocode) { this.regeocode = regeocode; }

    @Override
    public String toString() {
        return "GeoResponse{" +
                "status='" + status + '\'' +
                ", info='" + info + '\'' +
                ", infocode='" + infocode + '\'' +
                ", regeocode=" + (regeocode != null ? regeocode.toString() : "null") +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Regeocode {
        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("addressComponent")
        private AddressComponent addressComponent;

        public Regeocode() {}

        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }

        public AddressComponent getAddressComponent() { return addressComponent; }
        public void setAddressComponent(AddressComponent addressComponent) { this.addressComponent = addressComponent; }

        @Override
        public String toString() {
            return "Regeocode{" +
                    "formattedAddress='" + formattedAddress + '\'' +
                    ", addressComponent=" + (addressComponent != null ? addressComponent.toString() : "null") +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressComponent {
        private String province;

        @JsonProperty("city")
        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String city;

        private String district;
        private String adcode;
        private String towncode;
        private String country;
        private String township;
        private String citycode;

        @JsonProperty("streetNumber")
        private StreetNumber streetNumber;

        @JsonProperty("businessAreas")
        private List<BusinessArea> businessAreas;

        @JsonProperty("building")
        private Building building;

        @JsonProperty("neighborhood")
        private Neighborhood neighborhood;

        public AddressComponent() {}

        // Getter/Setter
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public String getAdcode() { return adcode; }
        public void setAdcode(String adcode) { this.adcode = adcode; }

        public String getTowncode() { return towncode; }
        public void setTowncode(String towncode) { this.towncode = towncode; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getTownship() { return township; }
        public void setTownship(String township) { this.township = township; }

        public String getCitycode() { return citycode; }
        public void setCitycode(String citycode) { this.citycode = citycode; }

        public StreetNumber getStreetNumber() { return streetNumber; }
        public void setStreetNumber(StreetNumber streetNumber) { this.streetNumber = streetNumber; }

        public List<BusinessArea> getBusinessAreas() { return businessAreas; }
        public void setBusinessAreas(List<BusinessArea> businessAreas) { this.businessAreas = businessAreas; }

        public Building getBuilding() { return building; }
        public void setBuilding(Building building) { this.building = building; }

        public Neighborhood getNeighborhood() { return neighborhood; }
        public void setNeighborhood(Neighborhood neighborhood) { this.neighborhood = neighborhood; }

        @Override
        public String toString() {
            return "AddressComponent{" +
                    "province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", district='" + district + '\'' +
                    ", adcode='" + adcode + '\'' +
                    ", towncode='" + towncode + '\'' +
                    ", country='" + country + '\'' +
                    ", township='" + township + '\'' +
                    ", citycode='" + citycode + '\'' +
                    ", streetNumber=" + (streetNumber != null ? streetNumber.toString() : "null") +
                    ", businessAreas=" + businessAreas +
                    ", building=" + (building != null ? building.toString() : "null") +
                    ", neighborhood=" + (neighborhood != null ? neighborhood.toString() : "null") +
                    '}';
        }
    }

    // 其他嵌套类实现（StreetNumber、BusinessArea等）...

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StreetNumber {
        private String number;
        private String location;
        private String direction;
        private String distance;
        private String street;

        public StreetNumber() {}

        // Getter/Setter
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }

        public String getDistance() { return distance; }
        public void setDistance(String distance) { this.distance = distance; }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        @Override
        public String toString() {
            return "StreetNumber{" +
                    "number='" + number + '\'' +
                    ", location='" + location + '\'' +
                    ", direction='" + direction + '\'' +
                    ", distance='" + distance + '\'' +
                    ", street='" + street + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusinessArea {
        private String location;
        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String name;
        private String id;

        public BusinessArea() {}

        // Getter/Setter
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        @Override
        public String toString() {
            return "BusinessArea{" +
                    "location='" + location + '\'' +
                    ", name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Building {
        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String name;
        @JsonDeserialize(using = StringOrArrayDeserializer.class)// 改为String类型
        private String type;

        public Building() {}

        // Getter/Setter


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Building{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Neighborhood {
        @JsonDeserialize(using = StringOrArrayDeserializer.class) // 处理字符串或数组
        private String name;

        @JsonDeserialize(using = StringOrArrayDeserializer.class) // 处理字符串或数组
        private String type;

        public Neighborhood() {}

        // Getter/Setter


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Neighborhood{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}