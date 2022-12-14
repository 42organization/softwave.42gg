package io.pp.arcade.domain.user.controller;

import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.v1.domain.currentmatch.CurrentMatch;
import io.pp.arcade.v1.domain.currentmatch.CurrentMatchRepository;
import io.pp.arcade.v1.domain.rank.dto.RankFindDto;
import io.pp.arcade.v1.domain.rank.dto.RankUserDto;
import io.pp.arcade.v1.domain.rank.service.RankService;
import io.pp.arcade.v1.domain.slot.Slot;
import io.pp.arcade.v1.domain.user.User;
import io.pp.arcade.v1.domain.user.dto.UserDto;
import io.pp.arcade.v1.global.type.GameType;
import io.pp.arcade.v1.global.type.Mode;
import io.pp.arcade.v1.global.util.ExpLevelCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class UserControllerNormalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestInitiator initiator;

    @Autowired
    private RankService rankService;

    @Autowired
    private CurrentMatchRepository currentMatchRepository;

    @BeforeEach
    void init() {
        initiator.letsgo();
    }

    @Test
    @Transactional
    @DisplayName("?????? ?????? ?????? - ?????????, ??????(??????) (/users/{targetIntraId}/detail)")
    void findDetailUserNormal() throws Exception {
        User user = initiator.users[0];

        mockMvc.perform(get("/pingpong/users/{targetIntraId}/detail", initiator.users[0].getIntraId()).contentType(MediaType.APPLICATION_JSON) // intra id ????????????
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())) // "Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userImageUri").value(user.getImageUri())) // ????????? ??????
                .andExpect(jsonPath("$.racketType").value(user.getRacketType().getCode())) // ????????????
                .andExpect(jsonPath("$.statusMessage").value(user.getStatusMessage())) // ???????????????
                .andExpect(jsonPath("$.level").value(ExpLevelCalculator.getLevel(user.getTotalExp()))) // ??????
                .andExpect(jsonPath("$.currentExp").value(ExpLevelCalculator.getCurrentLevelMyExp(user.getTotalExp()))) // ?????? ?????????
                .andExpect(jsonPath("$.maxExp").value(ExpLevelCalculator.getLevelMaxExp(ExpLevelCalculator.getLevel(user.getTotalExp())))) // ?????? ?????? ?????? ?????????
                .andDo(document("user-profile-detail"));
    }

    @Test
    @Transactional
    @DisplayName("?????? ?????? ?????? - ?????????, ?????? (/pingpong/users/{targetIntraId}/rank)")
    void findDetailUserRank() throws Exception {
        User user = initiator.users[0];
        UserDto userDto = UserDto.from(user);
        RankUserDto rank = rankService.findRank(RankFindDto.builder().user(userDto).gameType(GameType.SINGLE).build());

        mockMvc.perform(get("/pingpong/users/{targetIntraId}/rank", user.getIntraId()).contentType(MediaType.APPLICATION_JSON) // intra id, season ??? ????????????
                .param("season", "0")
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank").value(rank.getRank())) // ??????
                .andExpect(jsonPath("$.ppp").value(rank.getPpp())) // ?????????
                .andExpect(jsonPath("$.wins").value(rank.getWins())) // ??????
                .andExpect(jsonPath("$.losses").value(rank.getLosses())) // ??????
                .andExpect(jsonPath("$.winRate").value(rank.getWinRate())) // ??????
                .andDo(document("user-profile-rank-with-season-0"));
    }

    @Test
    @Transactional
    @DisplayName("?????? ????????? ?????? - mode ?????? (/users/live)")
    void userLiveInfo() throws Exception {

        //?????? ??????
        mockMvc.perform(get("/pingpong/users/live").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())) // "Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").isEmpty())
                .andExpect(jsonPath("$.currentMatchMode").isEmpty()) // enum ??????
                .andDo(document("user-live"));

        //?????? ??????
        createCurrentMatch();
        mockMvc.perform(get("/pingpong/users/live").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())) // "Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").value("match"))
                .andExpect(jsonPath("$.currentMatchMode").value(Mode.NORMAL.getCode())) // enum ??????
                .andDo(document("user-live-with-match"));
    }

    @Transactional
    CurrentMatch createCurrentMatch() {
        User user = initiator.users[0];
        Slot slot = initiator.slots[0];
        slot.setMode(Mode.NORMAL);
        CurrentMatch currentMatch = CurrentMatch.builder()
                .user(user)
                .slot(slot)
                .game(null)
                .matchImminent(false)
                .isMatched(true)
                .isDel(false)
                .build();
        currentMatchRepository.save(currentMatch);
        return currentMatch;
    }
}