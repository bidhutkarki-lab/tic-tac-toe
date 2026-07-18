package com.bidhutkarki.tictactoe.game.service;

import com.bidhutkarki.tictactoe.game.dto.CreateGameRequest;
import com.bidhutkarki.tictactoe.game.dto.GameResponse;
import com.bidhutkarki.tictactoe.game.dto.JoinGameRequest;
import com.bidhutkarki.tictactoe.game.dto.MakeMoveRequest;
import com.bidhutkarki.tictactoe.game.dto.UpdateGameRequest;
import com.bidhutkarki.tictactoe.game.entity.Board;
import com.bidhutkarki.tictactoe.game.entity.Game;
import com.bidhutkarki.tictactoe.game.entity.GameStatus;
import com.bidhutkarki.tictactoe.game.event.GameEvent;
import com.bidhutkarki.tictactoe.game.event.GameEventPublisher;
import com.bidhutkarki.tictactoe.game.exception.GameNotFoundException;
import com.bidhutkarki.tictactoe.game.exception.InvalidGameStateException;
import com.bidhutkarki.tictactoe.game.exception.InvalidMoveException;
import com.bidhutkarki.tictactoe.game.repository.GameRepository;
import com.bidhutkarki.tictactoe.player.entity.Player;
import com.bidhutkarki.tictactoe.player.exception.PlayerNotFoundException;
import com.bidhutkarki.tictactoe.player.repository.PlayerRepository;
import com.bidhutkarki.tictactoe.result.GameResultService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameEventPublisher gameEventPublisher;
    private final GameResultService gameResultService;

    @Transactional
    public GameResponse create(CreateGameRequest request) {
        Game saved = gameRepository.save(new Game(request.playerXId()));
        return GameResponse.from(saved);
    }

    @Transactional
    public GameResponse join(Long id, JoinGameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        if (request.playerId().equals(game.getPlayerXId())) {
            throw new InvalidGameStateException(
                    "player '" + request.playerId() + "' cannot join their own game");
        }
        game.join(request.playerId());
        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse start(Long id, String authId) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        Player player = playerRepository.findByAuthId(authId)
                .orElseThrow(() -> new PlayerNotFoundException("no player found for user '" + authId + "'"));
        String playerId = player.getId();
        if (!playerId.equals(game.getPlayerXId())
                && !playerId.equals(game.getPlayerOId())) {
            throw new InvalidGameStateException(
                    "player '" + playerId + "' is not part of game '" + id + "'");
        }
        game.start();
        return GameResponse.from(game);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findAll() {
        return gameRepository.findAll().stream()
                .map(GameResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameResponse findById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse update(Long id, UpdateGameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        game.update(request.board());
        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse makeMove(Long id, String authId, MakeMoveRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("game '" + id + "' is not in progress");
        }
        Player player = playerRepository.findByAuthId(authId)
                .orElseThrow(() -> new PlayerNotFoundException("no player found for user '" + authId + "'"));
        String playerId = player.getId();
        char mark = markFor(game, playerId);
        Board board = new Board(game.getBoard());
        if (board.nextMark() != mark) {
            throw new InvalidMoveException("it is not player " + mark + " turn");
        }
        if (!board.isEmptyAt(request.cell())) {
            throw new InvalidMoveException("cell " + request.cell() + " is already taken");
        }
        game.update(board.withMark(request.cell(), mark));
        if (game.getStatus().isTerminal()) {
            gameResultService.recordResult(game);
        }
        GameResponse response = GameResponse.from(game);
        gameEventPublisher.publish(new GameEvent(id, response));
        return response;
    }

    private char markFor(Game game, String playerId) {
        if (playerId.equals(game.getPlayerXId())) {
            return Board.MARK_X;
        }
        if (playerId.equals(game.getPlayerOId())) {
            return Board.MARK_O;
        }
        throw new InvalidMoveException(
                "player '" + playerId + "' is not part of game '" + game.getId() + "'");
    }

    @Transactional
    public void delete(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new GameNotFoundException(id);
        }
        gameRepository.deleteById(id);
    }
}
