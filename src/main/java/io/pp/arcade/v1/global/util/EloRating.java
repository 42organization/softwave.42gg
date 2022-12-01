package io.pp.arcade.v1.global.util;

public class EloRating {
    public static Integer pppChange(Integer myPPP, Integer opponentPPP, Boolean isWin, Integer difference) {
        Double we = 1.0 / (Math.pow(10.0, (opponentPPP - myPPP) / 400.0) + 1.0);
        Double change = 40 * ((isWin ? 1 : 0) - we);
        change = change + (change * difference * 0.1);

        if (change < 0) {
            change = change * 0.9;
        }
        return change.intValue();
    }
}