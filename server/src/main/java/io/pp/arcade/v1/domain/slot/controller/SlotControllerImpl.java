package io.pp.arcade.v1.domain.slot.controller;

import io.pp.arcade.v1.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchAddDto;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchDto;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchModifyDto;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchRemoveDto;
import io.pp.arcade.v1.domain.noti.dto.NotiCanceledTypeDto;
import io.pp.arcade.v1.domain.opponent.OpponentService;
import io.pp.arcade.v1.domain.season.SeasonService;
import io.pp.arcade.v1.domain.season.dto.SeasonDto;
import io.pp.arcade.v1.domain.security.jwt.TokenService;
import io.pp.arcade.v1.domain.slot.SlotService;

import io.pp.arcade.v1.domain.slotteamuser.SlotTeamUserService;
import io.pp.arcade.v1.domain.slotteamuser.dto.SlotTeamUserDto;
import io.pp.arcade.v1.domain.team.TeamService;
import io.pp.arcade.v1.domain.team.dto.TeamAddUserDto;
import io.pp.arcade.v1.domain.team.dto.TeamDto;
import io.pp.arcade.v1.domain.team.dto.TeamRemoveUserDto;
import io.pp.arcade.v1.domain.slot.dto.SlotStatusResponseDto;
import io.pp.arcade.v1.domain.user.UserService;
import io.pp.arcade.v1.domain.user.dto.UserDto;
import io.pp.arcade.v1.domain.user.dto.UserFindDto;
import io.pp.arcade.v1.global.exception.BusinessException;
import io.pp.arcade.v1.global.redis.Key;
import io.pp.arcade.v1.global.scheduler.SlotGenerator;
import io.pp.arcade.v1.global.type.*;
import io.pp.arcade.v1.global.util.HeaderUtil;
import io.pp.arcade.v1.global.util.NotiGenerater;
import io.pp.arcade.v1.domain.slot.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/pingpong")
public class SlotControllerImpl implements SlotController {
    private final SlotService slotService;
    private final TeamService teamService;
    private final CurrentMatchService currentMatchService;
    private final NotiGenerater notiGenerater;
    private final SeasonService seasonService;
    private final TokenService tokenService;
    private final RedisTemplate redisTemplate;
    private final SlotGenerator slotGenerator;
    private final OpponentService opponentService;
    private final SlotTeamUserService slotTeamUserService;

    private final UserService userService;

    @Override
    @GetMapping(value = "/match/tables/{tableId}/{mode}/{type}")
    public SlotStatusResponseDto slotStatusList(Integer tableId, Mode mode, GameType type, HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        List<SlotStatusDto> slots;
        List<List<SlotStatusDto>> matchBoards;
        SlotFindStatusDto findDto = SlotFindStatusDto.builder()
                .userId(user.getId())
                .type(type)
                .currentTime(LocalDateTime.now())
                .userMode(mode)
                .build();
        slots = slotService.findSlotsStatus(findDto);
        matchBoards = groupingSlots(slots);
        SlotStatusResponseDto responseDto = SlotStatusResponseDto.builder().matchBoards(matchBoards).intervalMinute(slotGenerator.getInterval()).build();
        return responseDto;
    }

    @Override
    @PostMapping(value = "/match/tables/{tableId}/{type}")
    public synchronized void slotAddUser(Integer tableId, GameType type, SlotAddUserRequestDto addReqDto, HttpServletRequest request) throws MessagingException {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        doubleNotSupportedYet(type);
        SlotDto slot = slotService.findSlotById(addReqDto.getSlotId());

        checkHeadCountMatchesInChallenge(addReqDto, slot);
        if (addReqDto.getMode() == Mode.CHALLENGE && addReqDto.getOpponent() != null) { // ????????? ?????? ????????? ?????? ??? null ?????? opponent ?????? ???????????? ?????? ??????.
            String opponentId = opponentService.findByIntraId(addReqDto.getOpponent()).getIntraId();// ????????? ???????????? ????????? ????????? ???????????? ????????? ????????????
            user = userService.findByIntraId(UserFindDto.builder().intraId(opponentId).build());
        }
        // else { // challenge ?????? ?????? ( opponent == null ) ?????? ?????? ????????? ?????? // +????????? ?????? ???????????? ????????? ????????? ??? ?????? ????????? ?????? ?????? ?????? ??????????????? ???.
        //}
        // checkIfUserHavePenalty(user);
        // checkIfSlotAvailable(slot, type, user, addReqDto);

        checkIfModeMatches(addReqDto, slot);
        checkIfUserHaveCurrentMatch(user);
        checkIfSlotAvailable(slot, type, user, addReqDto);

        TeamAddUserDto teamAddUserDto = getTeamAddUserDto(slot, user);
        //????????? ????????? ???????????? currentMatch??? ????????????.
        CurrentMatchAddDto matchAddDto = CurrentMatchAddDto.builder()
                .slot(slot)
                .user(user)
                .build();
        currentMatchService.addCurrentMatch(matchAddDto);

        SlotAddUserDto addDto = SlotAddUserDto.builder()
                .slotId(addReqDto.getSlotId())
                .type(type)
                .joinUserPpp(user.getPpp())
                .mode(addReqDto.getMode())
                .build();
        slotService.addUserInSlot(addDto);
        teamService.addUserInTeam(teamAddUserDto);

        //????????? ????????? ??? ?????? currentMatch??? ?????? ?????????.
        slot = slotService.findSlotById(slot.getId());
        modifyUsersCurrentMatchStatus(user, slot);
        notiGenerater.addMatchNotisBySlot(slot); // ???????????? ???????????? ????????? ????????? ??? ????????? ?????? ?????? ????????? ??????????


    }

    private void checkHeadCountMatchesInChallenge(SlotAddUserRequestDto addReqDto, SlotDto slot) {
        if (addReqDto.getMode() == Mode.CHALLENGE && addReqDto.getOpponent() != null && slot.getHeadCount() == 0) {
            throw new BusinessException("E0001");
        }
        if (addReqDto.getMode() == Mode.CHALLENGE && addReqDto.getOpponent() == null && slot.getHeadCount() != 0) {
            throw new BusinessException("E0001");
        }
        if (slot.getHeadCount() >= 2) {
            throw new BusinessException("E0001");
        }
    }

    private void checkIfModeMatches(SlotAddUserRequestDto addReqDto, SlotDto slot) {
        if (addReqDto.getMode() == null) {
            throw new BusinessException("SC001");
        }
        if (slot.getMode() != Mode.BOTH && slot.getMode() != addReqDto.getMode()) {
            throw new BusinessException("SC001");
        }
    }

    private void doubleNotSupportedYet(GameType type) {
        if (GameType.DOUBLE.equals(type)) {
            throw new BusinessException("SC004");
        }
    }

    @Override
    @DeleteMapping(value = "/match/slots/{slotId}")
    public synchronized void slotRemoveUser(Integer slotId, HttpServletRequest request) throws MessagingException {
        // slotId , tableId ????????? ??????
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        // ?????? ??????, ?????? ??????, ??? ??????( ????????? ?????? ????????? -, ????????? ?????? ?????? )
        CurrentMatchDto currentMatch = currentMatchService.findCurrentMatchByUser(user);

        checkIfCurrentMatchExists(currentMatch);
        SlotDto slot = slotService.findSlotById(slotId);
        checkIfUserRemovable(currentMatch, slot);
        // ????????? Challenge??? Slot headcount??? 2??? ??????
        if (slot.getMode() == Mode.CHALLENGE && slot.getHeadCount() == 2) {
            slotRemoveAdminUser(user, currentMatch, slot);
        }
        CurrentMatchRemoveDto currentMatchRemoveDto = CurrentMatchRemoveDto.builder()
                .user(user).build();
        currentMatchService.removeCurrentMatch(currentMatchRemoveDto);
        teamService.removeUserInTeam(TeamRemoveUserDto.builder()
                .slotId(slot.getId()).userId(user.getId()).build());
        slotService.removeUserInSlot(getSlotRemoveUserDto(slot, user));
//        slot = slotService.findSlotById(slot.getId());
        checkIsSlotMatched(user, currentMatch, slot);
    }

    private void slotRemoveAdminUser(UserDto user, CurrentMatchDto currentMatch, SlotDto slot) throws MessagingException {
        List<SlotTeamUserDto> users = slotTeamUserService.findAllBySlotId(slot.getId());
        UserDto adminUser = null;
        for (SlotTeamUserDto slotTeamUser : users) {
            if (slotTeamUser.getUser().getRoleType() == RoleType.ADMIN) {
                adminUser = slotTeamUser.getUser();
            }
        }
        if (adminUser == null) {
            return ;
        }
        teamService.removeUserInTeam(TeamRemoveUserDto.builder()
                .slotId(slot.getId()).userId(adminUser.getId()).build());
        slotService.removeUserInSlot(getSlotRemoveUserDto(slot, adminUser));
        checkIsSlotMatched(adminUser, currentMatch, slot);
    }

//    @Override
//    @GetMapping(value = "/match/opponent")
//    public SlotFindOpponentResDto FindOpponentAvailable() {
//        // ??????????????? ?????? ????????? ????????????.
//        // ????????? ????????? ( ??????????????? ? ) ??????
//        // 3?????? ??????. 3??? ????????? ? ??????.
//        List<UserOpponentResDto> opponents = userService.findAllOpponentByIsReady();
//        SlotFindOpponentResDto res = SlotFindOpponentResDto.builder().opponents(opponents).build();
//        return res;
//    }

    private void checkIsSlotMatched(UserDto user, CurrentMatchDto currentMatch, SlotDto slot) throws MessagingException {
        if (currentMatch.getIsMatched() == true) {
            falsifyIsMatchedForRemainders(slot);
//            redisTemplate.opsForValue().set(Key.PENALTY_USER + user.getIntraId(), "true", 60, TimeUnit.SECONDS);
            notiGenerater.deleteMatchedNotisBySlot(slot);
            notiGenerater.addCancelNotisBySlot(NotiCanceledTypeDto.builder().slotDto(slot).notiType(NotiType.CANCELEDBYMAN).build());
        }
    }


    private void checkIfUserRemovable(CurrentMatchDto currentMatch, SlotDto slot) {
        if (currentMatch.getSlot().getId() != slot.getId()) {
            throw new BusinessException("E0001");
        }
//        if (slot.getMode() != Mode.CHALLENGE && currentMatch.getMatchImminent() && slot.getHeadCount() == (slot.getType().equals(GameType.SINGLE) ? 2 : 4)) {
//            throw new BusinessException("SD002");
//        }
    }

    private void checkIfCurrentMatchExists(CurrentMatchDto currentMatch) {
        if (currentMatch == null || currentMatch.getGame() != null) {
            throw new BusinessException("SD001");
        }
    }

    private void falsifyIsMatchedForRemainders(SlotDto slot) {
        List<UserDto> users = new ArrayList<>();

        currentMatchService.modifyCurrentMatch(CurrentMatchModifyDto.builder()
                .slot(slot)
                .isMatched(false)
                .matchImminent(false)
                .build());
    }

    private List<List<SlotStatusDto>> groupingSlots(List<SlotStatusDto> slotStatusDtos) {
        /*
        List<List<SlotStatusDto>> slotGroups = new ArrayList<>();
        if (!slotStatusDtos.isEmpty()) {
            List<SlotStatusDto> oneGroup = new ArrayList<>();
            int groupTime = slotStatusDtos.get(0).getTime().getHour();

            for(SlotStatusDto slot: slotStatusDtos) {
                if (slot.getTime().getHour() == groupTime) {
                    oneGroup.add(slot);
                } else {
                    slotGroups.add(oneGroup);
                    oneGroup = new ArrayList<>(); //?????? ???????????? ?????? ?????? ??????
                    groupTime = slot.getTime().getHour(); //?????? ??????
                    oneGroup.add(slot);
                }
            }
            slotGroups.add(oneGroup);
        }
        */
        List<List<SlotStatusDto>> slotGroups = new ArrayList<>();
        if (!slotStatusDtos.isEmpty()) {
            List<SlotStatusDto> oneGroup = new ArrayList<>(slotStatusDtos);
            slotGroups.add(oneGroup);
        }
        return slotGroups;
    }

    private void modifyUsersCurrentMatchStatus(UserDto user, SlotDto slot) {
        Integer maxSlotHeadCount = GameType.SINGLE.equals(slot.getType()) ? 2 : 4;
        Boolean isMatched = (slot.getHeadCount()).equals(maxSlotHeadCount);
        Boolean isImminent = slot.getTime().isBefore(LocalDateTime.now().plusMinutes(5));
        CurrentMatchModifyDto matchModifyDto = CurrentMatchModifyDto.builder()
                .slot(slot)
                .isMatched(isMatched)
                .matchImminent(isImminent)
                .isDel(false)
                .build();
        currentMatchService.modifyCurrentMatch(matchModifyDto);
    }

    private TeamAddUserDto getTeamAddUserDto(SlotDto slot, UserDto user) {
        Integer teamId = null;
        List<TeamDto> teams = teamService.findAllBySlotId(slot.getId());
        GameType slotType = slot.getType();
        Integer maxTeamHeadCount = GameType.SINGLE.equals(slotType) ? 1 : 2;

        for (TeamDto team : teams) {
            if (team.getHeadCount() < maxTeamHeadCount) {
                teamId = team.getId();
                break;
            }
        }
        TeamAddUserDto teamAddUserDto = TeamAddUserDto.builder()
                .teamId(teamId)
                .userId(user.getId())
                .build();
        return teamAddUserDto;
    }

    private void checkIfSlotAvailable(SlotDto slot, GameType gameType, UserDto user, SlotAddUserRequestDto requestDto) {
        Integer pppGap = getPppGapFromSeason();

        SlotFilterDto slotFilterDto = SlotFilterDto.builder()
                .slot(slot)
                .gameType(gameType)
                .userPpp(user.getPpp())
                .userMode(requestDto.getMode())
                .pppGap(pppGap)
                .build();
        if (user.getRoleType() == RoleType.ADMIN) {
            return;
        }
        if (SlotStatusType.CLOSE.equals(slotService.getStatus(slotFilterDto))) {
            throw new BusinessException("SC001");
        }
    }

    private Integer getPppGapFromSeason() {
        Integer pppGap;
        SeasonDto season = seasonService.findLatestRankSeason();
        if (season == null) {
            pppGap = 100;
        } else {
            pppGap = season.getPppGap();
        }
        return pppGap;
    }

    private SlotRemoveUserDto getSlotRemoveUserDto(SlotDto slot, UserDto user) {
        SlotRemoveUserDto slotRemoveUserDto = SlotRemoveUserDto.builder()
                .slotId(slot.getId())
                .intraId(user.getIntraId())
                .exitUserPpp(user.getPpp())
                .build();
        return slotRemoveUserDto;
    }

    private void checkIfUserHavePenalty(UserDto user) {
        if (redisTemplate.opsForValue().get(Key.PENALTY_USER + user.getIntraId()) != null) {
            throw new BusinessException("SC003");
        }
    }

    private void checkIfUserHaveCurrentMatch(UserDto user) {
        CurrentMatchDto matchDto = currentMatchService.findCurrentMatchByUser(user);
        if (user.getRoleType() != RoleType.ADMIN && matchDto != null) { // ?????? ???????????? ?????? ?????? ????????? ????????????, ????????? ???????????? ?????????.
            throw new BusinessException("SC002");
        }
    }
    // ???????????? ????????? ( 42 ?????? ????????? ???????????? )



}