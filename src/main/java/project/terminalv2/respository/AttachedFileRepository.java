package project.terminalv2.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.terminalv2.domain.AttachedFile;

@Repository
public interface AttachedFileRepository extends JpaRepository<AttachedFile, Long> {
}
