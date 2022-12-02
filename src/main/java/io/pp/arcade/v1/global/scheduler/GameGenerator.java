package io.pp.arcade.v1.global.scheduler;

import io.pp.arcade.v1.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchRemoveDto;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchSaveGameDto;
import io.pp.arcade.v1.domain.game.GameService;
import io.pp.arcade.v1.domain.game.dto.GameAddDto;
import io.pp.arcade.v1.domain.game.dto.GameDto;
import io.pp.arcade.v1.domain.game.dto.GameModifyStatusDto;
import io.pp.arcade.v1.domain.noti.dto.NotiCanceledTypeDto;
import io.pp.arcade.v1.domain.slot.SlotService;
import io.pp.arcade.v1.domain.slot.dto.SlotDto;
import io.pp.arcade.v1.domain.user.dto.UserDto;
import io.pp.arcade.v1.global.exception.BusinessException;
import io.pp.arcade.v1.global.type.GameType;
import io.pp.arcade.v1.global.type.NotiType;
import io.pp.arcade.v1.global.type.StatusType;
import io.pp.arcade.v1.global.util.NotiGenerater;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.List;

//private String cron = "0 */" + intervalTime + " " + startTime + "-" + endTime + " * * *";
//private String cron = "0 */10 * * * *";
@Component
public class GameGenerator /* extends AbstractScheduler */{
    private final GameService gameService;
    private final SlotService slotService;
    private final CurrentMatchService currentMatchService;
    private final NotiGenerater notiGenerater;

    public GameGenerator(GameService gameService, SlotService slotService, CurrentMatchService currentMatchService, NotiGenerater notiGenerater) {
        this.gameService = gameService;
        this.slotService = slotService;
        this.currentMatchService = currentMatchService;
        this.notiGenerater = notiGenerater;
    }

    public void gameGenerator(Integer slotId) {
        SlotDto slotDto = slotService.findSlotById(slotId);
        checkIfGameExist(slotDto);
        List<GameDto> beforeGames = gameService.findGameByStatusType(StatusType.LIVE);
        for (GameDto beforeGame : beforeGames) {
            GameModifyStatusDto modifyStatusDto = GameModifyStatusDto.builder()
                    .game(beforeGame)
                    .status(StatusType.WAIT)
                    .build();
            gameService.modifyGameStatus(modifyStatusDto);
        }
        if (slotDto != null && slotDto.getHeadCount().equals(getMaxHeadCount(slotDto.getType()))) {
            addGame(slotDto);
        }
    }

    private Integer getMaxHeadCount(GameType type) {
        Integer maxHeadCount = 2;
        if (GameType.DOUBLE.equals(type)) {
            maxHeadCount = 4;
        }
        return maxHeadCount;
    }

    private void addGame(SlotDto slotDto) {
        GameAddDto gameAddDto = GameAddDto.builder()
                .slotDto(slotDto)
                .build();
        gameService.addGame(gameAddDto);
        GameDto game = gameService.findBySlot(slotDto.getId());

        CurrentMatchSaveGameDto matchSaveGameDto = CurrentMatchSaveGameDto.builder()
                .game(game)
                .build();
        currentMatchService.saveGameInCurrentMatch(matchSaveGameDto);
    }

    private void checkIfGameExist(SlotDto slotDto) {
        GameDto game = gameService.findBySlot(slotDto.getId());
        if (game != null) {
            throw new BusinessException("E0001");
        }
    }
}
