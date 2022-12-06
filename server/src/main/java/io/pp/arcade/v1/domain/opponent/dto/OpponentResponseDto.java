package io.pp.arcade.v1.domain.opponent.dto;

import io.pp.arcade.v1.domain.opponent.Opponent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class OpponentResponseDto {

    private String intraId;
    private String nick;
    private String imageUrl;
    private List<String> detail;
    private Boolean isReady;

    @Builder
    public OpponentResponseDto(Opponent opponent) {
        List<String> detail = new ArrayList<>();
        detail.add(opponent.getDetail1());
        detail.add(opponent.getDetail2());
        detail.add(opponent.getDetail3());
        this.intraId = opponent.getIntraId();
        this.nick = opponent.getNick();
        this.imageUrl = opponent.getImageUrl();
        this.detail = detail;
        this.isReady = opponent.getIsReady();
    }

    @Override
    public String toString() {
        return "OpponentResponseDto{" +
                "intraId='" + intraId + '\'' +
                ", nick='" + nick + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", detail1='" + detail + '\'' +
                ", isReady=" + isReady +
                '}';
    }

    public static OpponentResponseDto from(Opponent opponent) {
        return new OpponentResponseDto(opponent);
    }
}
