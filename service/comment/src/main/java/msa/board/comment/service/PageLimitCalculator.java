package msa.board.comment.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageLimitCalculator {

    /**
     * @param page 현재 페이지 번호(1부터 시작)
     * @param pageSize 한 페이지당 데이터 개수
     * @param movablePageCount 화면에서 한 번에 보여줄 페이지 버튼 개수
     * @return 현재 그룹(1~10페이지 // 11 ~ 20페이지)의 마지막 데이터 번호 + 1
     */
    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        return ((page - 1) / movablePageCount + 1) * pageSize * movablePageCount + 1;
    }
}
