package org.memo;

import java.io.*;

public class MemoManager {

    // 현재 메모 저장 폴더
    private static File memoFolder;
    // 저장 위치 설정을 위한 설정 파일 (사용자 홈 디렉터리의 숨김 파일)
    private static final File configFile = new File(System.getProperty("user.home"), ".memo_config");

    // 클래스 로딩 시 저장 위치를 불러움
    static {
        loadMemoFolder();
    }

    // 저장 폴더를 설정 파일에서 불러오거나 기본값으로 설정
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

    // 현재 설정된 메모 저장 폴더 반환
    public static File getMemoFolder() {
        return memoFolder;
    }

    // 저장 위치를 변경하고 설정 파일에 경로 설정
    public static void setMemoFolder(File folder) {
        memoFolder = folder;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(folder.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메모 저장
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

    // 사용자가 집접 지정한 파일에 메모 저장
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

    // 메모 파일에서 제목과 내용을 읽어 배열로 반환
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

    // 동일 제목 파일이 이미 존재할 경우 제목 뒤에 숫자를 붙여 고유하게 만듦
    public static String getUniqueTitle(String baseTitle) {
        int count = 1;
        String newTitle = baseTitle;
        while (new File(memoFolder, toSafeFileName(newTitle) + ".txt").exists()) {
            newTitle = baseTitle + " (" + count++ + ")";
        }
        return newTitle;
    }

    // 파일명에 사용할 수 없는 문자들을 안전한 문자로 바꿈
    public static String toSafeFileName(String title) {
        return title.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
