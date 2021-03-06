package project.terminalv2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.terminalv2.dto.bustime.NewBusTimeSaveRequest;
import project.terminalv2.vo.bustime.BusTimeInfoVo;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;


@Data
@Table(name = "bus_time")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class BusTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_time_no")
    private Long busTimeNo;

    @NotBlank
    @Column(name = "start_target")
    private String startTarget;

    @NotBlank
    @Column(name = "arrived_target")
    private String arrivedTarget;

    @NotBlank
    @Column(name = "start_date")
    private String startDate;

    // 버스 회사
    @NotBlank
    @Column(name = "bus_corp")
    private String busCorp;

    // 비고
    @Column(name = "note")
    private String note;

    // 경유지
    @Column(name = "layover")
    private String layover;

    public BusTime makeBusTime(NewBusTimeSaveRequest request) {
        return BusTime.builder()
                .startTarget(request.getStartTarget())
                .arrivedTarget(request.getEndTarget())
                .startDate(request.getStartDate())
                .busCorp(request.getBusCorp())
                .layover(request.getLayover())
                .note(request.getNote())
                .build();
    }

    public BusTimeInfoVo toBusTimeInfoVo(BusTime busTime) {
        return BusTimeInfoVo.builder()
                .startTarget(busTime.getStartTarget())
                .arrivedTarget(busTime.getArrivedTarget())
                .startTime(busTime.getStartDate())
                .busCorp(busTime.getBusCorp())
                .layover(busTime.getLayover())
                .note(busTime.getNote())
                .build();
    }
}
