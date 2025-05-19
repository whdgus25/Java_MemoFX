package org.memo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;

public class MemoApp extends Application {

    private ListView<String> memoListView;      // 메모 목록 표시
    private TextField titleField, searchField;  // 제목 입력, 검색 필드
    private TextArea memoArea;                  // 메모 내용 입력 영역
    private Label statusLabel;                  // 상태 표시 레이블
    private boolean isDarkMode = false;         // 테마 상태 플레그
    private Scene scene;
    private String currentMemoFileName = null;
    private static final File themeConfigFile = new File(System.getProperty("user.home"), ".memo_theme_config");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("메모장");

        loadThemeState(); // 테마 상태 불러오기

        // 검색창 및 메모 리스트
        searchField = new TextField();
        searchField.setPromptText("제목 검색...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMemoList(newVal.trim()));

        memoListView = new ListView<>();
        memoListView.setStyle("-fx-font-size: 13px;");
        loadMemoList(); // 메모 리스트 초기 로드

        // 메모 선택 시 제목과 내용 로드
        memoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                File file = new File(MemoManager.getMemoFolder(), newVal);
                String[] result = MemoManager.readMemo(file);
                if (result != null) {
                    titleField.setText(result[0]);
                    memoArea.setText(result[1]);
                    statusLabel.setText("열린 메모: " + newVal);
                    currentMemoFileName = newVal;
                }
            }
        });

        VBox leftPanel = new VBox(10, new Label("메모 목록:"), searchField, memoListView);
        leftPanel.setPrefWidth(250);

        // 우측: 제목, 내용, 버튼
        titleField = new TextField();
        titleField.setPromptText("제목을 입력하세요.");

        memoArea = new TextArea();
        memoArea.setPromptText("메모 내용을 입력하세요.");
        memoArea.setStyle("-fx-font-size: 14px;");

        Button newBtn = new Button("🆕 새 메모");
        Button saveBtn = new Button("💾 저장");
        Button saveAsBtn = new Button("📁 다른 이름으로");
        Button settingBtn = new Button("🛠 저장 위치");
        Button modifyBtn = new Button("📝 수정");
        Button deleteBtn = new Button("🗑 삭제");
        Button themeBtn = new Button("🌙 테마 전환");

        newBtn.setOnAction(e -> handleNew());
        saveBtn.setOnAction(e -> handleSave());
        saveAsBtn.setOnAction(e -> handleSaveAs(primaryStage));
        settingBtn.setOnAction(e -> handleSetFolder(primaryStage));
        modifyBtn.setOnAction(e -> handleModify());
        deleteBtn.setOnAction(e -> handleDelete());
        themeBtn.setOnAction(e -> toggleTheme());


        statusLabel = new Label("열린 메모 없음");
        statusLabel.setStyle("-fx-text-fill: gray;");

        HBox buttonRow = new HBox(10, newBtn, saveBtn, saveAsBtn, settingBtn, modifyBtn, deleteBtn, themeBtn);
        VBox rightPanel = new VBox(10,
                new Label("제목:"), titleField,
                new Label("내용:"), memoArea,
                buttonRow,
                statusLabel
        );
        rightPanel.setPrefWidth(650);

        HBox root = new HBox(25, leftPanel, rightPanel);
        root.setPadding(new Insets(10));

        scene = new Scene(root, 900, 500);
        applyTheme(); // 최초 테마 적용

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 저장 버튼
    private void handleSave() {
        String title = titleField.getText().trim();
        String content = memoArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("입력 오류", "제목과 내용을 모두 입력해주세요.");
            return;
        }

        File file = new File(MemoManager.getMemoFolder(), MemoManager.toSafeFileName(title) + ".txt");

        // 파일이 이미 존재하는 경우 덮어쓰기 확인
        if (file.exists()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("덮어쓰기 확인");
            confirm.setHeaderText("이미 존재하는 메모입니다.");
            confirm.setContentText("덮어쓰시겠습니까?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                title = MemoManager.getUniqueTitle(title);
                file = new File(MemoManager.getMemoFolder(), MemoManager.toSafeFileName(title) + ".txt");
            }
        }

        MemoManager.saveMemo(title, content);
        titleField.clear();
        memoArea.clear();
        memoListView.getSelectionModel().clearSelection();

        clearEditor();
        loadMemoList();
        showAlert("저장 완료", "메모가 저장되었습니다.");
    }

    // 다른 이름 저장 버튼
    private void handleSaveAs(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("다른 이름으로 저장");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("텍스트 파일", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            MemoManager.saveMemoToFile(file, titleField.getText(), memoArea.getText());
            loadMemoList();
            showAlert("저장 완료", "다른 이름으로 저장되었습니다.");
        }
    }

    // 저장 폴더 위치 설정
    private void handleSetFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("저장 위치 선택");
        File folder = chooser.showDialog(stage);
        if (folder != null) {
            File subFolder = new File(folder, "memos");
            if (!subFolder.exists()) subFolder.mkdirs();
            MemoManager.setMemoFolder(subFolder);
            loadMemoList();
            Platform.runLater(() -> showAlert("설정 완료", "저장 위치가 변경되었습니다."));
        }
    }

    // 메모 삭제
    private void handleDelete() {
        String selected = memoListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("선택 오류", "삭제할 메모를 선택해주세요.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("삭제 확인");
        confirm.setHeaderText("정말 삭제하시겠습니까?");
        confirm.setContentText("되돌릴 수 없습니다.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            File file = new File(MemoManager.getMemoFolder(), selected);
            if (file.exists() && file.delete()) {
                titleField.clear();
                memoArea.clear();
                loadMemoList();
                showAlert("삭제 완료", "메모가 삭제되었습니다.");
            } else {
                showAlert("오류", "삭제에 실패했습니다.");
            }
        }
        statusLabel.setText("삭제가 완료되었습니다.");
    }

    private void handleNew() {
        clearEditor();
        statusLabel.setText("새 메모...");
    }

    private void handleModify() {
        if (currentMemoFileName == null) {
            showAlert("수정 오류", "수정할 메모를 먼저 선택해주세요.");
            return;
        }
        String title = titleField.getText().trim();
        String content = memoArea.getText().trim();

        if (!validateInput(title, content)) return;

        String safeFileName = MemoManager.toSafeFileName(title);
        File newFile = new File(MemoManager.getMemoFolder(), safeFileName + ".txt");

        if (isSameTitle(safeFileName)) {
            MemoManager.saveMemo(title, content);
        } else {
            if (newFile.exists() && !confirmOverwrite()) {
                title = MemoManager.getUniqueTitle(title);
                safeFileName = MemoManager.toSafeFileName(title);
                newFile = new File(MemoManager.getMemoFolder(), safeFileName + ".txt");
            }
            MemoManager.saveMemo(title, content);
            deleteOldFile();
        }

        clearEditor();
        loadMemoList();
        currentMemoFileName = newFile.getName();
        showAlert("수정완료", "메모가 수정되었습니다.");
    }

    private boolean validateInput(String title, String content) {
        if (title.isEmpty() || content.isEmpty()) {
            showAlert("입력오류", "제목과 내용을 모두 입력해주세요.");
            return false;
        }
        return true;
    }

    private boolean isSameTitle(String newSafeFileName) {
        return currentMemoFileName != null && currentMemoFileName.equals(newSafeFileName + ".txt");
    }

    private boolean confirmOverwrite() {
        Alert overwriteAlert = new Alert(Alert.AlertType.CONFIRMATION);
        overwriteAlert.setTitle("파일 덮어쓰기 경고");
        overwriteAlert.setHeaderText("같은 제목의 메모가 이미 존재합니다.");
        overwriteAlert.setContentText("덮어쓰시겠습니까?");
        Optional<ButtonType> result = overwriteAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void deleteOldFile() {
        if (currentMemoFileName != null) {
            File oldFile = new File(MemoManager.getMemoFolder(), currentMemoFileName);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
    }

    private void clearEditor() {
        titleField.clear();
        memoArea.clear();
        memoListView.getSelectionModel().clearSelection();
        currentMemoFileName = null;
        statusLabel.setText("편집창이 초기화되었습니다.");
        Platform.runLater(() -> titleField.requestFocus());
    }


    // 다크/라이트 테마 전환
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        saveThemeState();
    }

    // 테마 적용
    private void applyTheme() {
        scene.getStylesheets().clear();
        if (isDarkMode) {
            scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("/light.css").toExternalForm());
        }
    }

    // 테마 상태 저장
    private void saveThemeState() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(themeConfigFile))) {
            writer.write(isDarkMode ? "dark" : "light");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 테마 상태 불러오기
    private void loadThemeState() {
        if (themeConfigFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(themeConfigFile))) {
                String line = reader.readLine();
                if ("dark".equalsIgnoreCase(line)) {
                    isDarkMode = true;
                } else {
                    isDarkMode = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            isDarkMode = false;
        }
    }

    // 키워드에 맞는 메모 필터링
    private void filterMemoList(String keyword) {
        memoListView.getItems().clear();
        File folder = MemoManager.getMemoFolder();
        File[] files = folder.listFiles((dir, name) ->
                name.endsWith(".txt") && (keyword.isEmpty() || name.contains(keyword))
        );
        if (files != null) {
            for (File file : files) {
                memoListView.getItems().add(file.getName());
            }
        }
    }

    // 전체 메모 목록 로드
    private void loadMemoList() {
        filterMemoList("");
    }

    // 경고창 또는 알림창 표시
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
