package com.example.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "spare_part")
public class Spare_part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int part_id;
    @Column(name = "part_name")  // 映射数据库字段
    private String partName;
    String part_model;
    String category;
    String manufacturer;
    String unit;
    String number;
    String anquan;

    public Spare_part() {
    }

    public Spare_part(int part_id, String anquan, String number, String unit, String manufacturer, String category, String part_model, String part_name) {
        this.part_id = part_id;
        this.anquan = anquan;
        this.number = number;
        this.unit = unit;
        this.manufacturer = manufacturer;
        this.category = category;
        this.part_model = part_model;
        this.partName = partName;
    }

    public int getPart_id() {
        return part_id;
    }

    public void setPart_id(int part_id) {
        this.part_id = part_id;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String part_name) {
        this.partName = part_name;
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAnquan() {
        return anquan;
    }

    public void setAnquan(String anquan) {
        this.anquan = anquan;
    }
}