package io.pp.arcade.v1.domain.admin.controller;

import io.pp.arcade.v1.domain.admin.dto.create.GameCreateDto;
import io.pp.arcade.v1.domain.admin.dto.create.GameCreateRequestDto;
import io.pp.arcade.v1.domain.admin.dto.delete.GameDeleteDto;
import io.pp.arcade.v1.domain.admin.dto.update.GameUpdateRequestDto;
import io.pp.arcade.v1.domain.admin.dto.update.PChangeUpdateDto;
import io.pp.arcade.v1.domain.game.GameService;
import io.pp.arcade.v1.domain.game.dto.GameDto;
import io.pp.arcade.v1.domain.pchange.PChangeService;
import io.pp.arcade.v1.domain.pchange.dto.PChangeDto;
import io.pp.arcade.v1.domain.pchange.dto.PChangeFindDto;
import io.pp.arcade.v1.domain.rank.dto.RankRedisModifyPppDto;
import io.pp.arcade.v1.domain.slot.SlotService;
import io.pp.arcade.v1.domain.slot.dto.SlotDto;
import io.pp.arcade.v1.domain.slotteamuser.SlotTeamUserService;
import io.pp.arcade.v1.domain.slotteamuser.dto.SlotTeamUserDto;
import io.pp.arcade.v1.domain.team.TeamService;
import io.pp.arcade.v1.domain.team.dto.TeamDto;
import io.pp.arcade.v1.domain.team.dto.TeamModifyGameResultDto;
import io.pp.arcade.v1.domain.team.dto.TeamPosDto;
import io.pp.arcade.v1.domain.user.UserService;
import io.pp.arcade.v1.domain.user.dto.UserModifyPppDto;
import io.pp.arcade.v1.global.type.GameType;
import io.pp.arcade.v1.global.util.EloRating;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class GameAdminControllerImpl implements GameAdminController {
    private final GameService gameService;
    private final SlotTeamUserService slotTeamUserService;
    private final TeamService teamService;
    private final PChangeService pChangeService;
    private final UserService userService;
    private final SlotService slotService;

    @Override
    @PostMapping(value = "/game")
    public void gameCreate(GameCreateRequestDto createRequestDto, HttpServletRequest request) {
        SlotDto slotDto = slotService.findSlotById(createRequestDto.getSlotId());
        GameCreateDto createDto = GameCreateDto.builder()
                .slotId(slotDto.getId())
                .type(slotDto.getType())
                .time(slotDto.getTime())
                .seasonId(createRequestDto.getSeasonId())
                .status(createRequestDto.getStatus())
                .build();
        gameService.createGameByAdmin(createDto);
    }

    @Override
    @PutMapping(value = "/game")
    public void gameUpdate(GameUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        /* ?????? ????????? ??? ???????????? ???????????? ?????? */
        Integer team1Score = updateRequestDto.getTeam1Score();
        Integer team2Score = updateRequestDto.getTeam2Score();
        GameDto game = gameService.findById(updateRequestDto.getGameId());
        List<SlotTeamUserDto> slotTeamUsers = slotTeamUserService.findAllBySlotId(game.getSlot().getId());

        TeamPosDto teamPostDto = teamService.findUsersByTeamPos(game.getSlot(), null);

        TeamDto team1 = null;
        TeamDto team2 = null;
        for (SlotTeamUserDto slotTeamUser : slotTeamUsers) {
            if (teamPostDto.getEnemyTeam().equals(slotTeamUser.getTeam())) {
                team1 = slotTeamUser.getTeam();
            } else{
                team2 = slotTeamUser.getTeam();
            }
        }

        TeamModifyGameResultDto modifyTeam1GameResultDto = TeamModifyGameResultDto.builder()
                .teamId(team1.getId())
                .score(team1Score)
                .win(team1Score > team2Score)
                .build();
        TeamModifyGameResultDto modifyTeam2GameResultDto = TeamModifyGameResultDto.builder()
                .teamId(team2.getId())
                .score(team2Score)
                .win(team2Score > team1Score)
                .build();

        teamService.modifyGameResultInTeam(modifyTeam1GameResultDto);
        teamService.modifyGameResultInTeam(modifyTeam2GameResultDto);
        /* ?????? ????????? ????????? ??? ???????????? ppp ?????? */
        modifyUsersInfo(game, team1, team2);
    }

    /* ?????? ??????
    -> ??? ????????? ?????? ?????? pChange ??????
    -> ?????? ppp(pppResult - pppChange)???, ?????? ????????? ????????? ????????? ????????? Elorating ?????????
    -> pChange?????? ??????
    -> rank ??????
     */
    private void modifyUsersInfo(GameDto gameDto, TeamDto updatedTeam1, TeamDto updatedTeam2) {
//        Boolean isOneSide = Math.abs(updatedTeam1.getScore() - updatedTeam2.getScore()) == 2;
        Integer differnece = Math.abs(updatedTeam1.getScore() - updatedTeam2.getScore());
        List<SlotTeamUserDto> slotTeamUsers = slotTeamUserService.findAllBySlotId(gameDto.getSlot().getId());
        for (SlotTeamUserDto slotTeamUser : slotTeamUsers) {
            modifyUserPPPAndPChangeAndRank(gameDto, slotTeamUser, updatedTeam1, updatedTeam2, differnece);
        }
    }

    private void modifyUserPPPAndPChangeAndRank(GameDto game, SlotTeamUserDto slotTeamUser, TeamDto beforeEnemyTeamDto, TeamDto enemyTeamDto, Integer difference) {
        PChangeDto pChangeDto = pChangeService.findPChangeByUserAndGame(PChangeFindDto.builder()
                .game(game)
                .user(slotTeamUser.getUser())
                .build());
        Integer userPreviousPpp = pChangeDto.getPppResult() - pChangeDto.getPppChange();

        Integer newPppChange = EloRating.pppChange(userPreviousPpp, enemyTeamDto.getTeamPpp(),!enemyTeamDto.getWin(), difference);
        Integer tmpPpp = userPreviousPpp + newPppChange;
        Integer userFinalPpp = tmpPpp > 0 ? tmpPpp : 0;
        UserModifyPppDto modifyPppDto = UserModifyPppDto.builder()
                .userId(slotTeamUser.getUser().getId())
                .ppp(userFinalPpp)
                .build();
        userService.modifyUserPpp(modifyPppDto);

        Integer pChangeId = pChangeService.findPChangeIdByUserAndGame(PChangeFindDto.builder()
                .game(game)
                .user(slotTeamUser.getUser())
                .build());
        pChangeService.updatePChangeByAdmin(pChangeId, PChangeUpdateDto.builder()
                .gameId(game.getId())
                .userId(slotTeamUser.getUser().getIntraId())
                .pppChange(userFinalPpp - userPreviousPpp)
                .pppResult(userFinalPpp)
                .build());
        GameType gameType = gameService.findById(game.getId()).getType();
        RankRedisModifyPppDto rankModifyDto =  RankRedisModifyPppDto.builder()
                .gameType(gameType)
                .ppp(userFinalPpp)
                .userDto(slotTeamUser.getUser())
                .modifyStatus(beforeEnemyTeamDto.getWin() == enemyTeamDto.getWin() ? -1 : booleanToInt(enemyTeamDto.getWin()))
                .build();
        //rankRedisService.modifyRankPpp(rankModifyDto);
    }

    @Override
    @DeleteMapping(value = "/game/{gameId}")
    public void gameDelete(Integer gameId, HttpServletRequest request) {
        GameDeleteDto deleteDto = GameDeleteDto.builder().gameId(gameId).build();
        gameService.deleteGameByAdmin(deleteDto);
    }

    @Override
    @GetMapping(value = "/game/all")
    public List<GameDto> gameAll(Pageable pageable, HttpServletRequest request) {
        List<GameDto> games = gameService.findGameByAdmin(pageable);
        return games;
    }

    private int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
}
