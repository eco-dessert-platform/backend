package com.bbangle.bbangle.board.repository.folder.query;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import java.util.List;

public interface QueryGenerator {

    List<BoardResponseDao> getBoards();

}
