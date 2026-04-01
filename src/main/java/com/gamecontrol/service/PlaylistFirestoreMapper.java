package com.gamecontrol.service;

import com.gamecontrol.dto.CreatePlaylistRequest;
import com.gamecontrol.dto.UsuarioPlayListDTO;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PlaylistFirestoreMapper {

    private PlaylistFirestoreMapper() {}

    static Map<String, Object> toMapFromRequest(CreatePlaylistRequest req) {
        Map<String, Object> m = new HashMap<>();
        m.put("nome", req.getNome());
        m.put("descricao", req.getDescricao());
        m.put("usuarioId", req.getUsuarioId()); // Pega o que veio no Request
        m.put("jogosIds", req.getJogosIds() != null ? req.getJogosIds() : new ArrayList<>());
        m.put("syncedAt", Timestamp.now());
        return m;
    }

    static UsuarioPlayListDTO fromSnapshot(DocumentSnapshot snap) {
        if (!snap.exists()) return null;
        UsuarioPlayListDTO dto = new UsuarioPlayListDTO();
        dto.setId(snap.getId());
        dto.setNome(snap.getString("nome"));
        dto.setDescricao(snap.getString("descricao"));
        dto.setUsuarioId(snap.getString("usuarioId"));

        Object jogos = snap.get("jogosIds");
        if (jogos instanceof List<?> list) {
            dto.setJogosIds((List<String>) list);
        }

        Timestamp ts = snap.getTimestamp("syncedAt");
        if (ts != null) {
            dto.setSyncedAt(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()));
        }
        return dto;
    }

    static Map<String, Object> patchMap(UsuarioPlayListDTO patch) {
        Map<String, Object> m = new HashMap<>();
        if (patch.getNome() != null) m.put("nome", patch.getNome());
        if (patch.getDescricao() != null) m.put("descricao", patch.getDescricao());
        if (patch.getJogosIds() != null) m.put("jogosIds", patch.getJogosIds());
        m.put("syncedAt", Timestamp.now());
        return m;
    }
}