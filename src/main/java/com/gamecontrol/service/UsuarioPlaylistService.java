package com.gamecontrol.service;

import com.gamecontrol.dto.CreatePlaylistRequest;
import com.gamecontrol.dto.UsuarioPlayListDTO;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Service
public class UsuarioPlaylistService {

    private final Firestore firestore;
    private final String colecao;

    public UsuarioPlaylistService(Firestore firestore, @Value("${firebase.collection.playlists}") String colecao) {
        this.firestore = firestore;
        this.colecao = colecao;
    }

    public UsuarioPlayListDTO criarPlaylist(CreatePlaylistRequest request) {
        return executar(() -> {
            Map<String, Object> dados = PlaylistFirestoreMapper.toMapFromRequest(request);

            DocumentReference ref;
            if (request.getDocumentId() != null && !request.getDocumentId().isBlank()) {
                ref = firestore.collection(colecao).document(request.getDocumentId().trim());
            } else {
                ref = firestore.collection(colecao).document();
            }

            ref.set(dados).get();
            return PlaylistFirestoreMapper.fromSnapshot(ref.get().get());
        });
    }

    private static <T> T executar(Callable<T> operacao) {
        try {
            return operacao.call();
        } catch (Exception e) {
            throw new RuntimeException("Erro Firestore: " + e.getMessage(), e);
        }
    }
}