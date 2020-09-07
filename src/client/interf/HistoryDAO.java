package client.interf;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public interface HistoryDAO {
    // абсолютный путь до каталога с рабочей программой + каталог истории
    String historyDirPath = Paths.get("").toAbsolutePath().toString() + "\\history";

    List<String> getMessageList();

    void addMessage(String message);
}
