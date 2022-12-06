package io.pp.arcade.v1.domain.rank.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.pp.arcade.v1.domain.rank.entity.Rank;
import io.pp.arcade.v1.domain.rank.entity.RankRedis;
import io.pp.arcade.v1.domain.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankUserDto {
    private String intraId;
    private Integer rank;
    private Integer ppp;
    private Integer wins;
    private Integer losses;
    private String statusMessage;
    @JsonSerialize(using = DtoSerialize.CustomDoubleSerializer.class)
    private Double winRate;

    public static RankUserDto from (RankRedis userRank, Integer ranking){
        RankUserDto dto = RankUserDto.builder()
                .intraId(userRank.getIntraId())
                .ppp(userRank.getPpp())
                .wins(userRank.getWins())
                .losses(userRank.getLosses())
                .winRate((userRank.getWins() + userRank.getLosses()) == 0 ? 0 : (double)(userRank.getWins() * 10000 / (userRank.getWins() + userRank.getLosses())) / 100)
                .rank(ranking == null ? -1 : ranking.intValue())
                .statusMessage(userRank.getStatusMessage())
                .build();
        return dto;
    }

    public static RankUserDto from (Rank rank) {
        Integer wins = rank.getWins();
        Integer losses = rank.getLosses();
        RankUserDto dto = RankUserDto.builder()
                .intraId(rank.getUser().getIntraId())
                .ppp(rank.getPpp())
                .wins(rank.getWins())
                .losses(rank.getLosses())
                .winRate((wins + losses) == 0 ? 0 : (double)(wins * 10000 / (wins + losses)) / 100)
                .rank(rank.getRanking())
                .statusMessage(rank.getUser().getStatusMessage())
                .build();
        return dto;
    }

    public static RankUserDto from (Rank rank, Integer ranking, Integer seasonStartPpp) {
        Integer wins = rank.getWins();
        Integer losses = rank.getLosses();

        Integer totalGame = wins + losses;
        Integer ppp = (totalGame == 0) ? seasonStartPpp : rank.getPpp();
        Integer userRanking = (totalGame == 0) ? -1 : ranking;

        RankUserDto dto = RankUserDto.builder()
                .intraId(rank.getUser().getIntraId())
                .ppp(ppp)
                .wins(wins)
                .losses(losses)
                .winRate((wins + losses) == 0 ? 0 : (double)(wins * 10000 / (wins + losses)) / 100)
                .rank(userRanking)
                .statusMessage(rank.getUser().getStatusMessage())
                .build();
        return dto;
    }

    public static RankUserDto createDummy(UserDto user) {
        return RankUserDto.builder()
                .intraId(user.getIntraId())
                .rank(-1)
                .wins(0)
                .losses(0)
                .ppp(0)
                .statusMessage(user.getStatusMessage())
                .winRate(0.0)
                .build();
    }

    @Override
    public String toString() {
        return "RankUserDto{" +
                "intraId='" + intraId + '\'' +
                ", ranking=" + rank +
                ", ppp=" + ppp +
                ", wins=" + wins +
                ", losses=" + losses +
                ", statusMessage='" + statusMessage + '\'' +
                ", winRate=" + winRate +
                '}';
    }
}
