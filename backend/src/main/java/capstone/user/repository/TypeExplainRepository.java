package capstone.user.repository;

import capstone.user.entity.TypeExplain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeExplainRepository extends JpaRepository<TypeExplain, Integer> {

    // type 값으로 조회
    Optional<TypeExplain> findByType(String type);

    // type 중복 체크용
    boolean existsByType(String type);
}
