package io.pp.arcade.v1.global.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum MatchStatus implements Constant{
    TIME_UP("live"),
    WAIT("wait"),
    END("end");

    private final String code;

    @JsonCreator
    public static MatchStatus getEnumFromValue(String value) {
        for(MatchStatus e : values()) {
            if(e.name().equals(value)) {
                return e;
            }
            else if (e.code.toUpperCase(Locale.ROOT).equals(value.toUpperCase(Locale.ROOT))) {
                return e;
            }
        }
        return null;
    }
}
