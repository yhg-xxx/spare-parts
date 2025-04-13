package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int inventory_id;
    int part_id;
    int location_id;
    String sn;
    String status;
    String inbound_time;
    String outbound_time;

    public int getInventory_id() {
        return inventory_id;
    }

    public void setInventory_id(int inventory_id) {
        this.inventory_id = inventory_id;
    }

    public int getPart_id() {
        return part_id;
    }

    public void setPart_id(int part_id) {
        this.part_id = part_id;
    }

    public int getLocation_id() {
        return location_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInbound_time() {
        return inbound_time;
    }

    public void setInbound_time(String inbound_time) {
        this.inbound_time = inbound_time;
    }

    public String getOutbound_time() {
        return outbound_time;
    }

    public void setOutbound_time(String outbound_time) {
        this.outbound_time = outbound_time;
    }
}
