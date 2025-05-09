package org.memo;

import java.io.*;

public class MemoManager {

    private static File memoFolder;
    private static final File configFile = new File(System.getProperty("user.home"), ".memo_config");

    static {
        loadMemoFolder();
    }

    private static void loadMemoFolder() {
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String path = reader.readLine();
                if (path != null) {
                    File saved = new File(path);
                    if (saved.exists()) {
                        memoFolder = saved;
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 기본: 바탕화면/memos
        File defaultFolder = new File(System.getProperty("user.home"), "Desktop/memos");
        if (!defaultFolder.exists()) defaultFolder.mkdirs();
        memoFolder = defaultFolder;
    }

    public static File getMemoFolder() {
        return memoFolder;
    }

    public static void setMemoFolder(File folder) {
        memoFolder = folder;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(folder.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMemo(String title, String content) {
        File file = new File(memoFolder, toSafeFileName(title) + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("제목: " + title);
            writer.newLine();
            writer.write("---");
            writer.newLine();
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMemoToFile(File file, String title, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("제목: " + title);
            writer.newLine();
            writer.write("---");
            writer.newLine();
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] readMemo(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String titleLine = reader.readLine();
            if (titleLine == null || !titleLine.startsWith("제목: ")) return null;
            String title = titleLine.substring("제목: ".length());
            reader.readLine(); // skip separator

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }

            return new String[]{title, content.toString().trim()};
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUniqueTitle(String baseTitle) {
        int count = 1;
        String newTitle = baseTitle;
        while (new File(memoFolder, toSafeFileName(newTitle) + ".txt").exists()) {
            newTitle = baseTitle + " (" + count++ + ")";
        }
        return newTitle;
    }

    public static String toSafeFileName(String title) {
        return title.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
