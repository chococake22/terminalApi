package project.terminalv2.respository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.terminalv2.domain.Board;
import project.terminalv2.domain.QBoard;
import project.terminalv2.domain.type.BoardType;
import project.terminalv2.domain.type.SearchType;
import project.terminalv2.vo.board.BoardListVo;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardSearchRepository {

    private final EntityManager em;

    public List<BoardListVo> findSearchBoard() {
        return em.createQuery("select new project.terminalv2.vo.board.BoardListVo(b.boardNo, b.title, b.boardType, b.writer)" +
                " from Board b", BoardListVo.class)
                .getResultList();
    }


    public List<Board> findBySearch(LocalDate startDate, LocalDate endDate, Integer page, Integer size, String search, SearchType searchType, BoardType boardType) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QBoard board = QBoard.board;

        return queryFactory
                .select(board)
                .from(board)
                .where(boardTypeEq(boardType), searchTypeEq(searchType, search), board.createdDate.between(startDate.atStartOfDay(), LocalDateTime.of(endDate, LocalTime.MAX)))
                .offset(page)
                .limit(size)
                .fetch();
    }

    // 게시글 타입이 같은지 체크
    private BooleanExpression boardTypeEq(BoardType boardType) {
        if (boardType == null) {
            return null;
        }
        return QBoard.board.boardType.eq(boardType);
    }

    // 해당 카테고리(작성자, 내용, 제목)에 해당 키워드가 있는지를 확인하는 메서드
    private BooleanExpression searchTypeEq(SearchType searchType, String search) {

        if (searchType == null) {
            return null;
        } else if (searchType == SearchType.TITLE) {
            return QBoard.board.title.contains(search);
        } else if (searchType == SearchType.WRITER) {
            return QBoard.board.writer.contains(search);
        } else if (searchType == SearchType.CONTENT) {
            return QBoard.board.content.contains(search);
        }

        return null;
    }



}
