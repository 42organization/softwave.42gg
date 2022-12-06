package io.pp.arcade.v1.domain.opponent;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
@NoArgsConstructor
public class Opponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String intraId;
    private String nick;
    private String imageUrl;
    private String detail1;
    private String detail2;
    private String detail3;
    private Boolean isReady;

    public Opponent(String intraId, String nick, String imageUrl, String detail1, String detail2, String detail3, Boolean isReady) {
        this.intraId = intraId;
        this.nick = nick;
        this.imageUrl = imageUrl;
        this.detail1 = detail1;
        this.detail2 = detail2;
        this.detail3 = detail3;
        this.isReady = isReady;
    }
}
