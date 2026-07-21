package com.fatayriTech.avarESG.dto.response.ObjectResponse;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveStatisticsResponse {

    private long total;

    private long planned;

    private long inProgress;

    private long onHold;

    private long completed;

    private long cancelled;

    private long overdue;
}