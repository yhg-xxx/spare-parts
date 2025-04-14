package com.example.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "spare_part")
public class Spare_part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int part_id;
    String part_name;
    String part_model;
    String category;
    String manufacturer;
    String unit;
    LocalDate warranty_until;
    String status;
    String type;

    public Spare_part() {
    }

    public Spare_part(int part_id, String part_name, String part_model, String category, String manufacturer, String unit, LocalDate warranty_until, String status, String type) {
        this.part_id = part_id;
        this.part_name = part_name;
        this.part_model = part_model;
        this.category = category;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.warranty_until = warranty_until;
        this.status = status;
        this.type = type;
    }

    public int getPart_id() {
        return part_id;
    }

    public void setPart_id(int part_id) {
        this.part_id = part_id;
    }

    public String getPart_name() {
        return part_name;
    }

    public void setPart_name(String part_name) {
        this.part_name = part_name;
    }

    public String getPart_model() {
        return part_model;
    }

    public void setPart_model(String part_model) {
        this.part_model = part_model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getWarranty_until() {
        return warranty_until;
    }

    public void setWarranty_until(LocalDate warranty_until) {
        this.warranty_until = warranty_until;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}