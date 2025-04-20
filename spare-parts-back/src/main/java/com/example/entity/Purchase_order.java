package com.example.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "purchase_order")
public class Purchase_order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int order_id;
    int applicant_id;
    String station;
    String workshop;
    String status;
    String spare_part_name;
    String spare_part_model;
    String number;
    String created_at;
    String completed_at;
    public int getQuantityAsInt() {
        try {
            return Integer.parseInt(this.number.trim());
        } catch (Exception e) {
            return 0; // 或抛出异常
        }
    }
    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getApplicant_id() {
        return applicant_id;
    }

    public void setApplicant_id(int applicant_id) {
        this.applicant_id = applicant_id;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getWorkshop() {
        return workshop;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpare_part_name() {
        return spare_part_name;
    }

    public void setSpare_part_name(String spare_part_name) {
        this.spare_part_name = spare_part_name;
    }

    public String getSpare_part_model() {
        return spare_part_model;
    }

    public void setSpare_part_model(String spare_part_model) {
        this.spare_part_model = spare_part_model;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCompleted_at() {
        return completed_at;
    }

    public void setCompleted_at(String completed_at) {
        this.completed_at = completed_at;
    }
}
