package br.com.fiap.redeprato.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;

/** DAO layer: data access for {@link Beneficiario} (T_RP_BENEFICIARIO) via Spring Data JPA. */
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {

    @Query("""
            select b from Beneficiario b left join fetch b.endereco
            where (:tipo is null or b.tipo = :tipo)
              and (:ativo is null or b.ativo = :ativo)
            order by b.nome
            """)
    List<Beneficiario> filtrar(@Param("tipo") TipoBeneficiario tipo, @Param("ativo") Boolean ativo);

    boolean existsByEndereco_IdEndereco(Long idEndereco);
}
