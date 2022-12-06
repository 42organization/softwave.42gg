package io.pp.arcade.v1.domain.game.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameExpAndPppResultResponseDto {
    private Integer beforeExp;
    private Integer beforeMaxExp;
    private Integer beforeLevel;
    private Integer increasedExp;
    private Integer increasedLevel;
    private Integer afterMaxExp;
    private Integer changedPpp;
    private Integer beforePpp;
}
