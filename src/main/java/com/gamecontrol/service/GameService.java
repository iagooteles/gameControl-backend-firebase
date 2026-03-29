package com.gamecontrol.service;

import com.gamecontrol.dto.CreateGameRequest;
import com.gamecontrol.dto.GameDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class GameService {

    private final Firestore firestore;
    private final String gamesCollection;

    public GameService(Firestore firestore, @Value("${firebase.collection.games}") String gamesCollection) {
        this.firestore = firestore;
        this.gamesCollection = gamesCollection;
    }

    public List<GameDTO> listarJogos() throws ExecutionException, InterruptedException {
        CollectionReference col = firestore.collection(gamesCollection);
        ApiFuture<QuerySnapshot> future = col.get();
        List<GameDTO> out = new ArrayList<>();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            GameDTO dto = GameFirestoreMapper.fromSnapshot(doc);
            if (dto != null) {
                out.add(dto);
            }
        }
        return out;
    }

    public Optional<GameDTO> buscarJogoPorId(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(gamesCollection).document(id).get().get();
        if (!snap.exists()) {
            return Optional.empty();
        }
        return Optional.ofNullable(GameFirestoreMapper.fromSnapshot(snap));
    }

    public Optional<GameDTO> buscarJogoPorSlug(String slug) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(gamesCollection)
                .whereEqualTo("slug", slug)
                .limit(1)
                .get();
        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        if (docs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(GameFirestoreMapper.fromSnapshot(docs.getFirst()));
    }

    public GameDTO cadastrarJogo(CreateGameRequest req) throws ExecutionException, InterruptedException {
        Map<String, Object> data = GameFirestoreMapper.toMap(req);
        if (!data.containsKey("syncedAt")) {
            data.put("syncedAt", FieldValue.serverTimestamp());
        }
        DocumentReference ref;
        if (req.getDocumentId() != null && !req.getDocumentId().isBlank()) {
            ref = firestore.collection(gamesCollection).document(req.getDocumentId().trim());
        } else {
            ref = firestore.collection(gamesCollection).document();
        }
        ApiFuture<WriteResult> write = ref.set(data);
        write.get();
        DocumentSnapshot snap = ref.get().get();
        return GameFirestoreMapper.fromSnapshot(snap);
    }

    public GameDTO atualizarJogo(String id, GameDTO patch) throws ExecutionException, InterruptedException {
        DocumentReference ref = firestore.collection(gamesCollection).document(id);
        DocumentSnapshot existing = ref.get().get();
        if (!existing.exists()) {
            throw new IllegalArgumentException("Jogo não encontrado.");
        }
        Map<String, Object> updates = GameFirestoreMapper.patchMap(patch);
        if (updates.isEmpty()) {
            return GameFirestoreMapper.fromSnapshot(existing);
        }
        ref.set(updates, SetOptions.merge()).get();
        return GameFirestoreMapper.fromSnapshot(ref.get().get());
    }

    public boolean deletarJogo(String id) throws ExecutionException, InterruptedException {
        DocumentReference ref = firestore.collection(gamesCollection).document(id);
        DocumentSnapshot snap = ref.get().get();
        if (!snap.exists()) {
            return false;
        }
        ref.delete().get();
        return true;
    }
}
