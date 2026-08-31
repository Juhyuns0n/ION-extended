package capstone.voicereport.repository;

import capstone.voicereport.dto.VoiceReportListResponse;
import capstone.voicereport.entity.VoiceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface VoiceReportRepository extends JpaRepository<VoiceReport, Integer> {

    // 목록 (지금 있는 쿼리 그대로 OK)
    @Query(
            value = """
        select new capstone.voicereport.dto.VoiceReportListResponse(
            v.reportId, v.subTitle, v.day
        )
        from VoiceReport v
        where v.userId = :userId and v.processingStatus = capstone.voicereport.async.VoiceReportStatus.COMPLETED
        order by v.createdAt desc
        """,
            countQuery = """
        select count(v)
        from VoiceReport v
        where v.userId = :userId and v.processingStatus = capstone.voicereport.async.VoiceReportStatus.COMPLETED
        """
    )
    Page<VoiceReportListResponse> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") int userId, Pageable pageable
    );

    Optional<VoiceReport> findByReportIdAndUserId(int reportId, int userId);

    Optional<VoiceReport> findTop1ByUserIdAndProcessingStatusOrderByReportIdDesc(
            int userId, capstone.voicereport.async.VoiceReportStatus processingStatus
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VoiceReport v where v.reportId = :reportId")
    Optional<VoiceReport> findByReportIdForUpdate(@Param("reportId") int reportId);
}
