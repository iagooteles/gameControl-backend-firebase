package com.gamecontrol.service;

import com.gamecontrol.dto.CreateUserRequest;
import com.gamecontrol.dto.UserDTO;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private final Firestore firestore;
    private final String nomeColecaoUsuarios;

    public UserService(Firestore firestore, @Value("${firebase.collection.users}") String nomeColecaoUsuarios) {
        this.firestore = firestore;
        this.nomeColecaoUsuarios = nomeColecaoUsuarios;
    }

    public List<UserDTO> listarUsuarios() {
        try {
            QuerySnapshot resultado = firestore.collection(nomeColecaoUsuarios).get().get();
            List<UserDTO> usuarios = new ArrayList<>();
            for (QueryDocumentSnapshot documento : resultado.getDocuments()) {
                usuarios.add(UserFirestoreMapper.paraDto(documento));
            }
            return usuarios;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operação no Firestore interrompida.", e);
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            if (causa instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(
                    causa != null ? causa.getMessage() : "Falha ao acessar o Firestore.",
                    e
            );
        }
    }

    /**
     * Cria usuário no Firestore. A senha é gravada como enviada — em produção use hash (ex.: BCrypt).
     */
    public UserDTO cadastrarUsuario(CreateUserRequest requisicao) {
        try {
            String email = requisicao.getEmail().trim();
            QuerySnapshot existente = firestore.collection(nomeColecaoUsuarios)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .get();
            if (!existente.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
            }
            Map<String, Object> dados = UserFirestoreMapper.paraDocumento(requisicao);
            DocumentReference referencia = firestore.collection(nomeColecaoUsuarios).document();
            referencia.set(dados).get();
            DocumentSnapshot salvo = referencia.get().get();
            return UserFirestoreMapper.paraDto(salvo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operação no Firestore interrompida.", e);
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            if (causa instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(
                    causa != null ? causa.getMessage() : "Falha ao acessar o Firestore.",
                    e
            );
        }
    }
}
