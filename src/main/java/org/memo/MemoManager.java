package org.memo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MemoManager {

    private static final File memoFolder = new File("memos");

    public static void saveMemo(String title, String content) {
        if (!memoFolder.exists()) memoFolder.mkdirs();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File memoFile = new File(memoFolder, title +"_"+ timestamp + ".txt");

        saveMemoToFile(memoFile, title, content);
    }

    public static void saveMemoToFile(File file, String title, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("제목: " + title + "\n");
            writer.write("내용:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] readMemo(File file) {
        try (Scanner scanner = new Scanner(file)) {
            String titleLine = scanner.nextLine(); // "제목: xxx"
            scanner.nextLine();
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            String title = titleLine.replace("제목: ", "").trim();
            return new String[]{title, content.toString().trim()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
