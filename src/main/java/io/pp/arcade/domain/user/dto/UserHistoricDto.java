package io.pp.arcade.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class UserHistoricDto {
    /*
    private Integer gameId;
    private Integer userId;
    private Integer pppChange;
    private Integer pppResult;
    private LocalDateTime time;
    */
    private Integer ppp;

    private LocalDateTime date;
}
