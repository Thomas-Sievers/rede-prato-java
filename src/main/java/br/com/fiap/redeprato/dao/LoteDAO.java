package br.com.fiap.redeprato.dao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;

public interface LoteDAO extends JpaRepository<Lote, Long> {

    @Query("""
            select l from Lote l
              join fetch l.ofertante o join fetch o.endereco join fetch l.categoria c
            where (:status is null or l.status = :status)
              and (:idOfertante is null or o.idOfertante = :idOfertante)
              and (:idCategoria is null or c.idCategoria = :idCategoria)
              and (:modalidade is null or l.modalidade = :modalidade)
            order by l.dataCadastro desc, l.idLote desc
            """)
    List<Lote> filtrar(@Param("status") StatusLote status,
                       @Param("idOfertante") Long idOfertante,
                       @Param("idCategoria") Long idCategoria,
                       @Param("modalidade") ModalidadeLote modalidade);

    @Query("""
            select l from Lote l
              join fetch l.ofertante o join fetch o.endereco e join fetch l.categoria c
            where l.status = br.com.fiap.redeprato.entity.enums.StatusLote.DISPONIVEL
              and l.dataLimiteRetirada > :agora
              and o.ativo = :ofertanteAtivo
              and (:idCategoria is null or c.idCategoria = :idCategoria)
              and (:modalidade is null or l.modalidade = :modalidade)
              and (:bairro is null or lower(e.bairro) = lower(:bairro))
            order by l.dataLimiteRetirada asc, l.idLote asc
            """)
    List<Lote> disponiveis(@Param("agora") LocalDateTime agora,
                           @Param("ofertanteAtivo") Boolean ofertanteAtivo,
                           @Param("idCategoria") Long idCategoria,
                           @Param("modalidade") ModalidadeLote modalidade,
                           @Param("bairro") String bairro);

    /** Available lots (status DISPONIVEL, deadline after {@code agora}, active ofertante). */
    default List<Lote> disponiveis(LocalDateTime agora, Long idCategoria, ModalidadeLote modalidade, String bairro) {
        return disponiveis(agora, Boolean.TRUE, idCategoria, modalidade, bairro);
    }

    List<Lote> findByStatusInAndDataLimiteRetiradaBefore(Collection<StatusLote> status, LocalDateTime agora);

    List<Lote> findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(Long idOfertante, StatusLote status);

    boolean existsByCategoria_IdCategoria(Long idCategoria);
}
