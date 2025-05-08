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
                if (path != null && !path.isBlank()) {
                    File saveFolder = new File(path);
                    if (saveFolder.exists() && saveFolder.isDirectory()) {
                        memoFolder = saveFolder;
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 기본 경로로 설정 (기본: 바탕화면/memos)
        String userHome = System.getProperty("user.home");
        memoFolder = new File(userHome, "Desktop/memos");
        if (!memoFolder.exists()) {
            memoFolder.mkdirs();
        }
    }

    // 저장된 memoFolder 반환
    public static File getMemoFolder() {
        return memoFolder;
    }
    // 새로운 저장 위치 설정 후 설정 파일에 경로를 기록
    public static void setMemoFolder(File folder) {
        memoFolder = folder;
        if (!memoFolder.exists()) {
            memoFolder.mkdirs();
        }

        // 설정 파일에 경로 저장
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))){
            writer.write(folder.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메모 저장
    public static void saveMemo(String title, String content) {
        String safeTitle = toSafeFileName(title);
        File file = new File(memoFolder, safeTitle +".txt");
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

    // 다른 이름으로 메모 저장
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

    // 메모 파일 읽기
    public static String[] readMemo(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String titleLine = reader.readLine();

            if (titleLine == null || !titleLine.startsWith("제목: ")) {
                return null;
            }

            String title = titleLine.substring("제목: ".length()).trim();

            String separator = reader.readLine();
            if (separator == null || !separator.equals("---")) {
                return null;
            }


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

    // 중복된 제목을 피하기 위해 유니크한 제목 생성
    public static String getUniqueTitle(String baseTitle) {
        String newTitle = baseTitle;
        int count = 1;
        while (new File(memoFolder, newTitle + ".txt").exists()) {
            newTitle = baseTitle + " (" + count + ")";
            count ++;
        }
        return newTitle;
    }

    // 파일명으로 사용할 수 없는 문자를 대체
    public static String toSafeFileName(String title) {
        return title.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
