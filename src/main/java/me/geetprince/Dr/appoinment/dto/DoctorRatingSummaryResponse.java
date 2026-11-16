package me.geetprince.Dr.appoinment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRatingSummaryResponse {

    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Double averageRating;
    private long totalReviews;
}
