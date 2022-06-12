package project.terminalv2.dto.comment;


import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor
public class CommentSaveRequest {

    @NotEmpty
    private String content;

}