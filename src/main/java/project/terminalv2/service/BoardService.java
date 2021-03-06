package project.terminalv2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.terminalv2.domain.Board;
import project.terminalv2.domain.type.BoardType;
import project.terminalv2.domain.type.SearchType;
import project.terminalv2.dto.board.BoardSaveRequest;
import project.terminalv2.dto.board.BoardUpdRequest;
import project.terminalv2.exception.ApiException;
import project.terminalv2.exception.ApiResponse;
import project.terminalv2.exception.ErrorCode;
import project.terminalv2.respository.BoardRepository;
import project.terminalv2.respository.BoardSearchRepository;
import project.terminalv2.vo.board.BoardDetailVo;
import project.terminalv2.vo.board.BoardListVo;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final ApiResponse apiResponse;
    private final BoardSearchRepository boardSearchRepository;

    @Transactional
    public ApiResponse saveBoard(BoardSaveRequest request, HttpServletRequest tokenInfo, BoardType boardType) {

        String token = tokenInfo.getHeader("jwt");
        String userId = jwtService.getSubject(token);

        Board board = Board.builder()
                .title(request.getTitle())
                .boardType(boardType)
                .writer(userId)
                .content(request.getContent())
                .build();

        log.info("board: {}", board);

        boardRepository.save(board);

        BoardDetailVo boardDetailVo = BoardDetailVo.builder()
                .boardNo(board.getBoardNo())
                .title(board.getTitle())
                .boardType(board.getBoardType())
                .writer(board.getWriter())
                .content(board.getContent())
                .writeDate(board.getCreatedDate())
                .build();

        return apiResponse.makeResponse(HttpStatus.OK, "2000", "????????? ?????? ??????", boardDetailVo);
    }


    @Transactional
    public ApiResponse getBoardInfoOne(Long boardNo) {

        // ????????? ??????
        // ?????? ???????????? ????????? ????????????
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_BOARD));

        log.info("Board = {}", board);

        BoardDetailVo boardDetailVo = BoardDetailVo.builder()
                .boardNo(board.getBoardNo())
                .title(board.getTitle())
                .boardType(board.getBoardType())
                .writeDate(board.getCreatedDate())
                .updateDate(board.getModifiedDate())
                .writer(board.getWriter())
                .content(board.getContent())
                .build();

        return apiResponse.makeResponse(HttpStatus.OK, "2000", "?????? ????????? ?????? ??????", boardDetailVo);
    }

    @Transactional
    public ApiResponse getBoardList(Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "boardNo"));
        Page<Board> boards = boardRepository.findAll(pageable);
        Page<BoardListVo> boardInfoVos = boards.map(board -> BoardListVo.builder()
                .boardNo(board.getBoardNo())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .writer(board.getWriter())
                .build());

        return apiResponse.makeResponse(HttpStatus.OK, "2000", "????????? ?????? ?????? ??????", boardInfoVos);
    }



    @Transactional
    public ApiResponse updateBoard(Long boardNo, BoardUpdRequest request, HttpServletRequest tokenInfo) {

        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_BOARD));

        if (userService.hasAccessAuth(board.getWriter(), tokenInfo)) {
            board.update(request);

            BoardDetailVo boardDetailVo = BoardDetailVo.builder()
                    .boardNo(board.getBoardNo())
                    .title(board.getTitle())
                    .boardType(board.getBoardType())
                    .writer(board.getWriter())
                    .content(board.getContent())
                    .writeDate(board.getCreatedDate())
                    .updateDate(board.getModifiedDate())
                    .build();

            return apiResponse.makeResponse(HttpStatus.OK, "2000", "????????? ?????? ??????", boardDetailVo);
        } else {
            throw new ApiException(ErrorCode.BOARD_UNAUTHORIZED);
        }
    }

    public ApiResponse deleteBoard(Long boardNo, HttpServletRequest tokenInfo) {

        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_BOARD));

        if (userService.hasAccessAuth(board.getWriter(), tokenInfo)) {
            boardRepository.deleteById(boardNo);
            return apiResponse.makeResponse(HttpStatus.OK, "2000", "????????? ?????? ??????", null);
        } else {
            throw new ApiException(ErrorCode.BOARD_UNAUTHORIZED);
        }
    }


    @Transactional
    public ApiResponse searchBoard(LocalDate startDate, LocalDate endDate, Integer page, Integer size, String keyword, SearchType searchType, BoardType boardType) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "boardNo"));

        Page<Board> boards;

        // ??????????????? ??????
//        if (SearchType.ofCode(type).equals(SearchType.TITLE)) {
//            boards = boardRepository.findAllByTitleContaining(search, pageable);
//        } else if (SearchType.ofCode(type).equals(SearchType.WRITER)) {
//            boards = boardRepository.findAllByWriterContaining(search, pageable);
//        } else if (SearchType.ofCode(type).equals(SearchType.CONTENT)) {
//            boards = boardRepository.findAllByContentContaining(search, pageable);
//        } else if(SearchType.ofCode(type).equals(SearchType.All) && search == null) {
//            boards = boardRepository.findAll(pageable);
//        } else {
//            boards = boardRepository.findAllBySearch(search, pageable);
//        }

        List<Board> boardList = boardSearchRepository.findBySearch(startDate, endDate, page, size, keyword, searchType, boardType);



//        boards = boardRepository.findAllByWord(search, boardType, pageable);
//
//        Page<BoardListVo> boardListVos = boards.map(board -> BoardListVo.builder()
//                .boardNo(board.getBoardNo())
//                .boardType(board.getBoardType())
//                .title(board.getTitle())
//                .writer(board.getWriter())
//                .build());

        return apiResponse.makeResponse(HttpStatus.OK, "2000", "????????? ????????? ?????? ??????", boardList);
    }
}
