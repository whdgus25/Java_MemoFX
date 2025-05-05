package org.memo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoManager {

    private static final File memoFolder = new File("memos");

    public static void saveMemo(String title, String content) {
        if (!memoFolder.exists()) memoFolder.mkdirs();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File memoFile = new File(memoFolder, timestamp + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(memoFile))) {
            writer.write("제목: " + title + "\n");
            writer.write("내용: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
