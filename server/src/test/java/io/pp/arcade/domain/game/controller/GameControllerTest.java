package io.pp.arcade.domain.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.v1.domain.currentmatch.CurrentMatch;
import io.pp.arcade.v1.domain.currentmatch.CurrentMatchRepository;
import io.pp.arcade.v1.domain.game.Game;
import io.pp.arcade.v1.domain.game.GameRepository;
import io.pp.arcade.v1.domain.pchange.PChange;
import io.pp.arcade.v1.domain.pchange.PChangeRepository;
import io.pp.arcade.v1.domain.slot.Slot;
import io.pp.arcade.v1.domain.slot.SlotRepository;
import io.pp.arcade.v1.domain.slotteamuser.SlotTeamUser;
import io.pp.arcade.v1.domain.slotteamuser.SlotTeamUserRepository;
import io.pp.arcade.v1.domain.team.Team;
import io.pp.arcade.v1.domain.team.TeamRepository;
import io.pp.arcade.v1.domain.user.User;
import io.pp.arcade.v1.global.type.GameType;
import io.pp.arcade.v1.global.type.Mode;
import io.pp.arcade.v1.global.type.StatusType;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfiguration.class)
class GameControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private CurrentMatchRepository currentMatchRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private SlotTeamUserRepository slotTeamUserRepository;

    @Autowired
    TestInitiator initiator;

    @Autowired
    private TeamRepository teamRepository;

    private final int GAMESIZE = 200;
    @Autowired
    private PChangeRepository pChangeRepository;
    Game liveGame;
    Game waitGame;
    Game doubleGame;
    User[] users;
    Team[] teams;
    Game[] endGames;
    Slot[] slots;

    @BeforeEach
    void init() {
        initiator.letsgo();
        users = initiator.users;
        teams = initiator.teams;
        slots = initiator.slots;
        endGames = new Game[GAMESIZE - 3];

        Slot[] slotList = new Slot[GAMESIZE];

        for (int i = 0; i < GAMESIZE - 2; i++) {
            slotList[i] = slotRepository.save(Slot.builder()
                    .time(LocalDateTime.now().plusMinutes(i))
                    .headCount(2)
                    .tableId(1)
                    .gamePpp(1000)
                    .type(GameType.SINGLE)
                    .mode(Mode.RANK)
                    .build());
            for (int j = 0; j < 2; j++) {
                Team team = teamRepository.save(Team.builder()
                        .slot(slotList[i])
                        .score(j + 1)
                        .headCount(1)
                        .win(j != 0)
                        .teamPpp(1000)
                        .build());
                slotTeamUserRepository.save(SlotTeamUser.builder()
                        .slot(slotList[i])
                        .team(team)
                        .user(users[j])
                        .build());
            }
        }
        /*
         * endGames : user0, user1
         * doubleGames : user2, 3, 4, 5
         * liveGame : user0, 1
         * waitGame : user6, 7
         * */
        slotList[GAMESIZE - 2] = slotRepository.save(Slot.builder()
                .time(LocalDateTime.now().plusMinutes(GAMESIZE - 2))
                .headCount(4)
                .tableId(1)
                .gamePpp(1000)
                .type(GameType.DOUBLE)
                .mode(Mode.RANK)
                .build());
        for (int j = 0; j < 2; j++) {
            Team team = teamRepository.save(Team.builder()
                    .slot(slotList[GAMESIZE - 2])
                    .score(j + 1)
                    .headCount(2)
                    .win(j != 0)
                    .teamPpp(1000)
                    .build());
            slotTeamUserRepository.save(SlotTeamUser.builder()
                    .slot(slotList[GAMESIZE - 2])
                    .team(team)
                    .user(users[2 + j * 2])
                    .build());
            slotTeamUserRepository.save(SlotTeamUser.builder()
                    .slot(slotList[GAMESIZE - 2])
                    .team(team)
                    .user(users[2 + j * 2 + 1])
                    .build());
        }

        slotList[GAMESIZE - 1] = slotRepository.save(Slot.builder()
                .time(LocalDateTime.now().plusMinutes(GAMESIZE - 1))
                .headCount(2)
                .tableId(1)
                .gamePpp(1000)
                .type(GameType.SINGLE)
                .mode(Mode.RANK)
                .build());
        for (int i = 0; i < 2; i++) {
            Team team = teamRepository.save(Team.builder()
                    .slot(slotList[GAMESIZE - 1])
                    .score(i + 1)
                    .headCount(1)
                    .win(i != 0)
                    .teamPpp(1000)
                    .build());
            slotTeamUserRepository.save(SlotTeamUser.builder()
                    .slot(slotList[GAMESIZE - 1])
                    .team(team)
                    .user(users[i + 6])
                    .build());
        }

        for (int i = 0; i < GAMESIZE - 3; i++){
            endGames[i] = gameRepository.save(Game.builder().slot(slotList[i]).mode(Mode.RANK).season(1).status(StatusType.END).build());
        }

        // wait, double, live ?????????
        waitGame = gameRepository.save(Game.builder().slot(slotList[GAMESIZE - 3]).season(1).status(StatusType.WAIT).mode(Mode.RANK).build());
        doubleGame = gameRepository.save(Game.builder().slot(slotList[GAMESIZE - 2]).season(1).status(StatusType.WAIT).mode(Mode.RANK).build());
        liveGame = gameRepository.save(Game.builder().slot(slotList[GAMESIZE - 1]).season(1).status(StatusType.LIVE).mode(Mode.RANK).build());

        /* pChange ?????? */
        for (int i = 0; i < GAMESIZE - 3; i++){
            pChangeRepository.save(PChange.builder().game(endGames[i]).user(users[0]).pppChange(20).pppResult(1000).build());
            pChangeRepository.save(PChange.builder().game(endGames[i]).user(users[1]).pppChange(20).pppResult(1000).build());
        }
        pChangeRepository.save(PChange.builder().game(doubleGame).user(users[2]).pppChange(20).pppResult(1000).build());
        pChangeRepository.save(PChange.builder().game(doubleGame).user(users[3]).pppChange(20).pppResult(1000).build());
        pChangeRepository.save(PChange.builder().game(doubleGame).user(users[4]).pppChange(20).pppResult(1000).build());
        pChangeRepository.save(PChange.builder().game(doubleGame).user(users[5]).pppChange(20).pppResult(1000).build());
        /*
         * ?????? ?????? ?????? ??????
         * -> ??????,?????? ?????? ??????
         * -> matchTable??? ?????? ?????? ?????? ??????
         * */
        List<SlotTeamUser> liveUsers = slotTeamUserRepository.findAllBySlotId(slotList[GAMESIZE - 1].getId());
        for (SlotTeamUser liveUser : liveUsers) {
            currentMatchRepository.save(CurrentMatch.builder().isDel(false).user(liveUser.getUser()).slot(liveUser.getSlot()).game(liveGame).build());
        }
        List<SlotTeamUser> doubleGameUsers = slotTeamUserRepository.findAllBySlotId(slotList[GAMESIZE - 2].getId());
        for (SlotTeamUser doubleGameUser : doubleGameUsers) {
            currentMatchRepository.save(CurrentMatch.builder().isDel(false).user(doubleGameUser.getUser()).slot(doubleGameUser.getSlot()).game(doubleGame).build());
        }
        List<SlotTeamUser> waitUsers = slotTeamUserRepository.findAllBySlotId(slotList[GAMESIZE - 3].getId());
        for (SlotTeamUser waituser : waitUsers) {
            currentMatchRepository.save(CurrentMatch.builder().isDel(false).user(waituser.getUser()).slot(waituser.getSlot()).game(waitGame).build());
        }
    }


    @Transactional
    void addCurrentMatch(Game game, User user) {
        currentMatchRepository.save(CurrentMatch.builder()
                .matchImminent(true)
                .isMatched(true)
                .game(game)
                .slot(game.getSlot())
                .user(user)
                .build());
    }

    @Transactional
    Game saveGame(Slot slot, User user1, User user2) {
        List<Team> teamList = teamRepository.findAllBySlotId(slot.getId());
        slotTeamUserRepository.save(SlotTeamUser.builder().slot(slot).team(teamList.get(0)).user(user1).build());
        slotTeamUserRepository.save(SlotTeamUser.builder().slot(slot).team(teamList.get(1)).user(user2).build());
        slot.setType(GameType.SINGLE);
        slot.setMode(Mode.RANK);
        slot.setGamePpp(1000);
        slot = slotRepository.findById(slot.getId()).orElse(null);
        Game game = Game.builder()
                .slot(slot)
                .season(1)
                .status(StatusType.LIVE)
                .mode(slot.getMode())
                .build();
        game = gameRepository.save(game);
        currentMatchRepository.save(CurrentMatch.builder().matchImminent(true).isMatched(true)
                .game(game).slot(slot).user(user1).isDel(false).build());
        currentMatchRepository.save(CurrentMatch.builder().matchImminent(true).isMatched(true)
                .game(game).slot(slot).user(user2).isDel(false).build());
        return game;
    }


    @Test
    @Transactional
    @DisplayName("?????? ?????? ?????? - /games/result")
    void testGameUserInfo() throws  Exception {
        /*
         * ????????? ??????
         * - ?????? ?????? ????????? ????????? ?????? ??????
         * - ?????? ????????? ?????? ?????? ??????
         * - ?????? ????????? ?????? ?????? ??????
         * - gameId ??? integer ??? ?????? ?????? ?????? ?????? (400)
         * - gameId ??? ????????? ????????? ??????
         * - gameId ??? null??? ??????
         * - gameId ??? String??? ??????
         * - count ??? ????????? ??????
         * - count ??? null??? ??????
         * - count ??? String??? ??????
         * - count ??? 100??? ????????? ??????
         * - gameId ??? 100 ??? ?????? - 200,game ????????? ??????
         * - status ??? ?????? ?????? ??????
         * - status ??? null??? ??????
         * - score ??? null??? ??????
         * - ??? ??? ???????????? 3 ????????? ??????
         * - ?????? score ??? 1:1??? ????????? ??????
         * - score??? ????????? ??????
         * - score??? ????????? ??????
         * - ?????? score ??? ?????? 4 ????????? ?????? >>>> 1:5??? ????????? 2:2??? ????????????
         * - intraId ??? ??? ?????? ??????
         * - ????????? ??????????????? ????????? ?????? ??????
         * - ???????????? ?????? ???????????? ?????? ????????? ????????? ????????? ??????
         */


        /*
         * ????????? - ???????????? ?????? ????????? ?????? ??????
         * -> 400
         * */
        mockMvc.perform(get("/pingpong/games/players").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + initiator.tokens[10].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-find-result-4XXError-cause-no-exists"));
        /*
         * ????????? - ?????? ?????? ?????? ??????
         * myTeam [{hakim, imageUri}]
         * enemyTeam [{nheo, imageUri}]
         * -> 200
         * */
        mockMvc.perform(get("/pingpong/games/players").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.matchTeamsInfo.myTeam.teams[0].intraId").value(users[0].getIntraId()))
                .andExpect(jsonPath("$.matchTeamsInfo.myTeam.teams[0].userImageUri").value(users[0].getImageUri()))
                .andExpect(jsonPath("$.matchTeamsInfo.enemyTeam.teams[0].intraId").value(users[1].getIntraId()))
                .andExpect(jsonPath("$.matchTeamsInfo.enemyTeam.teams[0].userImageUri").value(users[1].getImageUri()))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-single"));
        /*
         * ????????? - ?????? ?????? ?????? ??????
         * jekim wochae jabae jihyukim
         * myTeam [{jekim, imageUri}, {wochae, }]
         * enemyTeam [{jabae, imageUri}, {jihyukim, }]
         * -> 200
         * */
        mockMvc.perform(get("/pingpong/games/players").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.matchTeamsInfo.myTeam.teams[0].intraId").value(users[2].getIntraId()))
                .andExpect(jsonPath("$.matchTeamsInfo.myTeam.teams[0].userImageUri").value(users[2].getImageUri()))
                .andExpect(jsonPath("$.matchTeamsInfo.myTeam.teams[1].intraId").value(users[3].getIntraId()))
                .andExpect(jsonPath("$.matchTeamsInfo.myTeam.teams[1].userImageUri").value(users[3].getImageUri()))
                .andExpect(jsonPath("$.matchTeamsInfo.enemyTeam.teams[0].intraId").value(users[4].getIntraId()))
                .andExpect(jsonPath("$.matchTeamsInfo.enemyTeam.teams[0].userImageUri").value(users[4].getImageUri()))
                .andExpect(jsonPath("$.matchTeamsInfo.enemyTeam.teams[1].intraId").value(users[5].getIntraId()))
                .andExpect(jsonPath("$.matchTeamsInfo.enemyTeam.teams[1].userImageUri").value(users[5].getImageUri()))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-double"));
    }

    @Test
    @Transactional
    @DisplayName("?????? ?????? ?????? - /games")
    void gameUserInfo() throws Exception {
        /*
         * gameId != Integer (????????? ?????? ??????)
         * -> RequestDto Binding Error
         * -> 400
         * */
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("gameId", "string");
        params.add("count", "20");
        params.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-find-results-4XXError-cause-gameId-is-not-Integer"));

        /*
         * gameId -> -1 (????????? ??????)
         * -> ?????? ???????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params2 = new LinkedMultiValueMap<>();
        params2.add("gameId", "-1");
        params2.add("count", "20");
        params2.add("season", "1");
        params2.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params2)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-gameId-is-negative"));

        /*
         * gameId -> null
         * -> ?????? ???????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params3 = new LinkedMultiValueMap<>();
        params3.add("count", "20");
        params3.add("season", "1");
        params3.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params3)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                //.andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
//                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-find-result-gameId-is-null"));

        /*
         * count -> string (????????? ?????? ??????)
         * -> RequestDto Binding Error
         * -> 400
         * */
        MultiValueMap<String,String> params11 = new LinkedMultiValueMap<>();
        params11.add("count", "string");
        params11.add("status", StatusType.END.getCode());
        params11.add("season", "1");

        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                        .params(params11)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-find-results-4XXError-cause-count-is-string"));

        /*
         * count -> -1 (????????? ??????)
         * -> ?????? 20?????? ???????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params12 = new LinkedMultiValueMap<>();
        params12.add("count", "-1");
        params12.add("status", StatusType.END.getCode());
        params12.add("season", "1");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                        .params(params12)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(10))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-count-is-negative"));

        /*
         * count -> null
         * -> ?????? 20?????? ???????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params4 = new LinkedMultiValueMap<>();
        params4.add("gameId", "12345678");
        params4.add("status", StatusType.END.getCode());
        params4.add("season", "1");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params4)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(10))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-count-is-null"));
        /*
         * count -> 1234 (100????????? ??????)
         * -> 100?????? ???????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params5 = new LinkedMultiValueMap<>();
        params5.add("count", "12345678");
        params5.add("status", StatusType.END.getCode());
        params5.add("season", "1");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params5)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
//                .andExpect(jsonPath("$.games.length()").value(100))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-count-is-bigger-than-100"));

        /*
         * status -> NOTHING (?????? ?????? ??????)
         * -> live, wait, end ?????? ???????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params6 = new LinkedMultiValueMap<>();
        params6.add("count", "20");
        params6.add("status", "NOTHING");
        params6.add("season", "1");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params6)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games[0].gameId").value(liveGame.getId()))
                .andExpect(jsonPath("$.games[1].gameId").value(doubleGame.getId()))
                .andExpect(jsonPath("$.games[2].gameId").value(waitGame.getId()))
                .andExpect(jsonPath("$.games[3].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-user-info-status-wrong"));

        /*
         * status -> null
         * -> end??? ???????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params7 = new LinkedMultiValueMap<>();
        params7.add("count", "20");
        params7.add("season", "1");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params7)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games[1].gameId").value(endGames[endGames.length - 2].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-user-info-status-is-null"));

        /*
         * GameId -> 1000
         * -> GameId 99????????? ????????? ??????
         * -> 200
         * */
        MultiValueMap<String,String> params8 = new LinkedMultiValueMap<>();
        params8.add("gameId", "12345678");
        params8.add("count", "20");
        params8.add("status", StatusType.END.getCode());
        params8.add("season", "1");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params8)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[99 - 1].getId()))
                .andDo(document("game-find-results-find-id-1000"));
    }

    @Test
    @Transactional
    void gameResultByUserIdAndIndexAndCount() throws Exception {
        /*
         * IntraId ?????? ??? ?????? ??????
         * -> 400
         * */
        LinkedMultiValueMap<String, String> params2 = new LinkedMultiValueMap<>();
        params2.add("gameId", "1234");
        params2.add("count", "20");
        mockMvc.perform(get("/pingpong/games/users/{intraId}","NOTFOUND").contentType(MediaType.APPLICATION_JSON)
                        .params(params2)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UF001"))
                .andDo(document("game-find-results-4xxError-cause-couldn't-find-intraId"));

        /*
         * ????????? - ??????????????? ?????? ??????
         * -> 200
         * */
        LinkedMultiValueMap<String, String> params3 = new LinkedMultiValueMap<>();
        params3.add("gameId", "1234");
        params3.add("count", "20");
        mockMvc.perform(get("/pingpong/games/users/{intraId}", users[10].getIntraId()).contentType(MediaType.APPLICATION_JSON)
                        .params(params3)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games").isEmpty())
                .andExpect(jsonPath("$.lastGameId").value(0))
                .andExpect(status().isOk())
                .andDo(document("find-game-results-theres-no-game-record"));
    }

    @Test
    @Transactional
    @DisplayName("?????? ?????? ?????? - /games/result")
    void gameResultSave() throws Exception {

        Game game = saveGame(slots[1], users[8], users[9]);
        Map<String, String> body1 = new HashMap<>();

//        - Score?????? null??? ??????
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-null"));

//        - Score??? ?????? 3????????? ??????
        Map<String, String> body2 = new HashMap<>();
        body2.put("myTeamScore", "3");
        body2.put("enemyTeamScore", "1");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body2))
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-bigger-than-3"));

//        - Score?????? 1:1 ??????
        Map<String, String> body3 = new HashMap<>();
        body3.put("myTeamScore", "1");
        body3.put("enemyTeamScore", "1");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body3))
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-1:1"));

//        - Score?????? 4????????? ??????
        Map<String, String> body4 = new HashMap<>();
        body4.put("myTeamScore", "2");
        body4.put("enemyTeamScore", "2");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body4))
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-sum-is-bigger-than-4"));

//        - Score?????? ????????? ??????
        Map<String, String> body5 = new HashMap<>();
        body5.put("myTeamScore", "2");
        body5.put("enemyTeamScore", "-3");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body5))
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-negative"));

//        - Score?????? ????????? ??????
        Map<String, String> body6 = new HashMap<>();
        body6.put("myTeamScore", "0.75");
        body6.put("enemyTeamScore", "2");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body6))
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-decimal"));

//        - Score??? ????????? ?????? ??????
        Map<String, String> body7 = new HashMap<>();
        body7.put("myTeamScore", "win");
        body7.put("enemyTeamScore", "lose");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body7))
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-not-integer"));

//        ??? ??? ??? ???
        Map<String, String> body8 = new HashMap<>();
        body8.put("myTeamScore", "2");
        body8.put("enemyTeamScore", "1");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body8))
                        .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isCreated())
                .andDo(document("game-result-save-isCreated"));

//        - ???????????? ????????? ?????? ??????
//          ??? 202 (Accepted)
        Map<String, String> body9 = new HashMap<>();
        body9.put("myTeamScore", "2");
        body9.put("enemyTeamScore", "1");
        mockMvc.perform(post("/pingpong/games/result/rank").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body9))
                        .header("Authorization", "Bearer " + initiator.tokens[9].getAccessToken()))
                .andExpect(status().isAccepted())
                .andDo(document("game-result-save-isAccepted"));

//        ?????????????????????
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("count", "20");
        mockMvc.perform(get("/pingpong/games/users/{intraId}", users[8].getIntraId()).contentType(MediaType.APPLICATION_JSON)
                .params(params)
                .header("Authorization", "Bearer " + initiator.tokens[8].getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.games[0].status").value(StatusType.END.getCode()))
                .andExpect(jsonPath("$.games[0].team1.score").value(body8.get("myTeamScore")))
                .andExpect(jsonPath("$.games[0].team1.players[0].intraId").value(users[8].getIntraId()))
                .andExpect(jsonPath("$.games[0].team1.isWin").value(true))
                .andExpect(jsonPath("$.games[0].team2.score").value(body8.get("enemyTeamScore")))
                .andExpect(jsonPath("$.games[0].team2.players[0].intraId").value(users[9].getIntraId()))
                .andExpect(jsonPath("$.games[0].team2.isWin").value(false))
                .andDo(document("game-find-results-only-user-request"));
    }
}
