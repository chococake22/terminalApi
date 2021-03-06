package project.terminalv2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import project.terminalv2.domain.AttachedFile;
import project.terminalv2.domain.Board;
import project.terminalv2.exception.ApiException;
import project.terminalv2.exception.ApiResponse;
import project.terminalv2.exception.ErrorCode;
import project.terminalv2.respository.AttachedFileRepository;
import project.terminalv2.respository.BoardRepository;
import project.terminalv2.vo.file.FileResponseVo;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachedFileService {

    private final AttachedFileRepository attachedFileRepository;
    private final BoardRepository boardRepository;
    private final UserService userService;

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    @Transactional
    public ApiResponse saveFiles(List<MultipartFile> files, Long boardNo, HttpServletRequest tokenInfo) throws IOException {

        if (files.isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND_FILE);
        }

        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_BOARD));

        if (userService.hasAccessAuth(board.getWriter(), tokenInfo)) {

            List<FileResponseVo> fileResponseVos = new ArrayList<>();

            String downloadURI = "";

            for (MultipartFile file : files) {

                // ?????? ?????????
                String orginalFilename = StringUtils.cleanPath(file.getOriginalFilename());

                // ?????? ?????????
                String storeFileName = createStoreFileName(orginalFilename);

                // ????????? ?????? ??????
                file.transferTo(new File(getFullPath(orginalFilename)));

                // ?????? ?????? ?????? ??????
                AttachedFile attachedFile = AttachedFile.builder()
                        .filename(orginalFilename)
                        .saveName(storeFileName)
                        .board(board)
                        .build();

                // URI ??????
                downloadURI = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/v1/board/download/")
                        .path(attachedFile.getFilename())
                        .toUriString();

                FileResponseVo fileResponseVo = FileResponseVo.builder()
                        .filename(orginalFilename.toString())
                        .fileUri(downloadURI.toString())
                        .build();

                fileResponseVos.add(fileResponseVo);

                // ?????? ?????? ?????? ??????
                attachedFileRepository.save(attachedFile);
            }

            ApiResponse apiResponse = new ApiResponse();

            return apiResponse.makeResponse(HttpStatus.OK, "6000", "?????? ????????? ??????", fileResponseVos);

            // ????????? ?????? contentType??? ?????? ???????????? ?????? ????????? ApiResponse??? ???????????? ????????? ????????? ResponseEntity??? ???????????? ?????? ????????? ??????.
        } else {
            throw new ApiException(ErrorCode.USER_UNAUTHORIZED);
        }
    }

    // ????????? ?????????
    private String createStoreFileName(String orginalFilename) {

        String ext = extractExt(orginalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    // ???????????? ?????? ????????????
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    @Transactional
    public ResponseEntity<Resource> downloadFile(String fileName) throws MalformedURLException, UnsupportedEncodingException {

        UrlResource resource = new UrlResource("file:" + getFullPath(fileName));

        log.info("URL: {}", resource.toString());

        String encodedUploadFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

        log.info("encodedUploadFileName: {}", encodedUploadFileName);

        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.MULTIPART_FORM_DATA) // ????????? Multipart??? ??????
                .body(resource);
        // ????????? ?????? contentType??? ?????? ???????????? ?????? ????????? ApiResponse??? ???????????? ????????? ????????? ResponseEntity??? ???????????? ?????? ????????? ??????.
    }
}
