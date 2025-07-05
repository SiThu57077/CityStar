package com.cityStar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyAppointmentStatsDTO {
    private String month; 
    private int completed;
    private int pending;
}
