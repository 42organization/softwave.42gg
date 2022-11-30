package io.pp.arcade.v1.domain.rank.dto;

import io.pp.arcade.v1.domain.season.dto.SeasonDto;
import io.pp.arcade.v1.global.type.GameType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@Builder
public class RankFindListDto {
    private Pageable pageable;
    private GameType gameType;
    private SeasonDto seasonDto;
    private Integer count;

    @Builder
    public RankFindListDto(Pageable pageable, GameType gameType, SeasonDto seasonDto, Integer count) {
        this.pageable = pageable;
        this.gameType = gameType;
        this.seasonDto = seasonDto;
        this.count = count;
    }
}
