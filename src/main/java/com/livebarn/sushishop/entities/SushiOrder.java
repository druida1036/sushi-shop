package com.livebarn.sushishop.entities;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sushi_order")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SushiOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "time_spent")
    private int timeSpent;
    @Column(name = "createdAt")
    private OffsetDateTime createdAt;

    @OneToOne
    private Status status;
    @OneToOne
    private Sushi sushi;

}
