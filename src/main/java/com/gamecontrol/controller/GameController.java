package com.gamecontrol.controller;

import com.gamecontrol.dto.CreateGameRequest;
import com.gamecontrol.dto.GameDTO;
import com.gamecontrol.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<List<GameDTO>> listarJogos() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(gameService.listarJogos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> buscarJogoPorId(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        return gameService.buscarJogoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GameDTO> buscarJogoPorSlug(@PathVariable String slug)
            throws ExecutionException, InterruptedException {
        return gameService.buscarJogoPorSlug(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GameDTO> cadastrarJogo(@Valid @RequestBody CreateGameRequest body)
            throws ExecutionException, InterruptedException {
        GameDTO criado = gameService.cadastrarJogo(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameDTO> atualizarJogo(@PathVariable String id, @RequestBody GameDTO patch)
            throws ExecutionException, InterruptedException {
        try {
            return ResponseEntity.ok(gameService.atualizarJogo(id, patch));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarJogo(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        if (gameService.deletarJogo(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
