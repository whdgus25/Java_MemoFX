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

import java.io.File;
import java.util.Optional;

public class MemoApp extends Application {

    private ListView<String> memoListView;
    private TextField titleField, searchField;
    private TextArea memoArea;
    private Label statusLabel;
    private boolean isDarkMode = false;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("메모장");

        // 좌측: 메모 목록 + 검색
        searchField = new TextField();
        searchField.setPromptText("제목 검색...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMemoList(newVal.trim()));

        memoListView = new ListView<>();
        memoListView.setStyle("-fx-font-size: 13px;");
        loadMemoList();

        memoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                File file = new File(MemoManager.getMemoFolder(), newVal);
                String[] result = MemoManager.readMemo(file);
                if (result != null) {
                    titleField.setText(result[0]);
                    memoArea.setText(result[1]);
                    statusLabel.setText("열린 메모: " + newVal);
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

        Button saveBtn = new Button("💾 저장");
        Button saveAsBtn = new Button("📁 다른 이름으로");
        Button settingBtn = new Button("🛠 저장 위치");
        Button deleteBtn = new Button("🗑 삭제");
        Button themeBtn = new Button("🌙 테마 전환");

        saveBtn.setOnAction(e -> handleSave());
        saveAsBtn.setOnAction(e -> handleSaveAs(primaryStage));
        settingBtn.setOnAction(e -> handleSetFolder(primaryStage));
        deleteBtn.setOnAction(e -> handleDelete());
        themeBtn.setOnAction(e -> toggleTheme());

        statusLabel = new Label("열린 메모 없음");
        statusLabel.setStyle("-fx-text-fill: gray;");

        HBox buttonRow = new HBox(10, saveBtn, saveAsBtn, settingBtn, deleteBtn, themeBtn);
        VBox rightPanel = new VBox(10,
                new Label("제목:"), titleField,
                new Label("내용:"), memoArea,
                buttonRow,
                statusLabel
        );
        rightPanel.setPrefWidth(500);

        HBox root = new HBox(20, leftPanel, rightPanel);
        root.setPadding(new Insets(10));

        scene = new Scene(root, 800, 500);
        applyTheme(); // 최초 테마 적용

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSave() {
        String title = titleField.getText().trim();
        String content = memoArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("입력 오류", "제목과 내용을 모두 입력해주세요.");
            return;
        }

        File file = new File(MemoManager.getMemoFolder(), MemoManager.toSafeFileName(title) + ".txt");

        if (file.exists()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("덮어쓰기 확인");
            confirm.setHeaderText("이미 존재하는 메모입니다.");
            confirm.setContentText("덮어쓰시겠습니까?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                title = MemoManager.getUniqueTitle(title);
            }
        }

        MemoManager.saveMemo(title, content);
        titleField.clear();
        memoArea.clear();
        loadMemoList();
        showAlert("저장 완료", "메모가 저장되었습니다.");
    }

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
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        scene.getStylesheets().clear();
        if (isDarkMode) {
            scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("/light.css").toExternalForm());
        }
    }

    private void filterMemoList(String keyword) {
        memoListView.getItems().clear();
        File folder = MemoManager.getMemoFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") && name.contains(keyword));
        if (files != null) {
            for (File file : files) {
                memoListView.getItems().add(file.getName());
            }
        }
    }

    private void loadMemoList() {
        filterMemoList("");
    }

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
