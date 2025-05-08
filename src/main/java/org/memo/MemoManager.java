package org.memo;

import java.io.*;

public class MemoManager {

    private static File memoFolder;

    // 기본 저장 경로는 사용자 바탕화면의 "memos" 폴더
    static {
        String userHome = System.getProperty("user.home");
        memoFolder = new File(userHome, "Desktop/memos");
        if (!memoFolder.exists()) {
            memoFolder.mkdirs();
        }
    }

    public static File getMemoFolder() {
        return memoFolder;
    }

    public static void setMemoFolder(File folder) {
        memoFolder = folder;
        if (!memoFolder.exists()) {
            memoFolder.mkdirs();
        }
    }

    public static void saveMemo(String title, String content) {
        File file = new File(memoFolder, title +".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("제목: " + title);
            writer.newLine();
            writer.write("내용: \n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMemoToFile(File file, String title, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("제목: " + title);
            writer.newLine();
            writer.write("내용: \n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] readMemo(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String title = reader.readLine();
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            return new String[]{title, content.toString().trim()};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
