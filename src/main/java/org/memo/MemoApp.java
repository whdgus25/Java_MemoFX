package org.memo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

public class MemoApp extends Application {

    private ListView<String> memoListView;

    @Override
    public void start(Stage primaryStage) {
        TextField titleField = new TextField();
        titleField.setPromptText("제목을 입력하세요.");

        TextArea memoArea = new TextArea();
        memoArea.setPromptText("메모 내용을 입력하세요.");

        Button saveButton = new Button("저장");
        Button saveAsButton = new Button("다른 이름으로 저장");
        Button settingButton = new Button("저장 위치 설정");
        Button deleteButton = new Button("삭제");

        memoListView = new ListView<>();
        loadMemoList();

        saveButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = memoArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("입력 오류", "제목과 내용을 모두 입력해주세요.");
                return;
            }

            File file = new File(MemoManager.getMemoFolder(), title + ".txt");

            if (file.exists()) {
                Alert overwriteAlert = new Alert(Alert.AlertType.CONFIRMATION);
                overwriteAlert.setTitle("파일 덮어쓰기 경고");
                overwriteAlert.setHeaderText("같은 제목의 메모가 이미 존재합니다.");
                overwriteAlert.setContentText("덮어쓰시겠습니까?");

                Optional<ButtonType> result = overwriteAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    title = MemoManager.getUniqueTitle(title);
                }

            }
            MemoManager.saveMemo(title, content);
            titleField.clear();
            memoArea.clear();
            loadMemoList();
            showAlert("저장 완료", "메모가 저장되었습니다.");
        });

        saveAsButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = memoArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("입력오류", "제목과 내용을 모두 입력해주세요.");
            } else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("다른 이름으로 저장");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("텍스트 파일", "*.txt"));
                File file = fileChooser.showSaveDialog(primaryStage);
                if (file != null) {
                    MemoManager.saveMemoToFile(file, title, content);
                    loadMemoList();
                    showAlert("저장완료", "파일이 저장되었습니다.");
                }
            }
        });

        settingButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("저장할 폴더 선택");
            File selectedDirectory = directoryChooser.showDialog(primaryStage);

            if (selectedDirectory != null) {
                File memoSubFolder = new File(selectedDirectory, "memos");
                if (!memoSubFolder.exists()) {
                    memoSubFolder.mkdirs();
                }
                MemoManager.setMemoFolder(memoSubFolder);
                loadMemoList();
                Platform.runLater(() -> showAlert("설정 완료", "저장 위치가 변경되었습니다."));
            }
        });

        deleteButton.setOnAction(e-> {
            String selectedMemo = memoListView.getSelectionModel().getSelectedItem();
            if (selectedMemo == null) {
                showAlert("선택 오류", "삭제할 메모를 선택해주세요.");
                return;
            }

            Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
            deleteAlert.setTitle("메모 삭제");
            deleteAlert.setHeaderText("선택한 메모를 삭제하시겠습니까?");
            deleteAlert.setContentText("삭제 후 되돌릴 수 없습니다.");

            Optional<ButtonType> result = deleteAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            File file = new File(MemoManager.getMemoFolder(), selectedMemo);
            if (file.exists()) {
                file.delete();
                loadMemoList();
                showAlert("삭제 완료", "메모가 삭제되었습니다.");
            } else {
                showAlert("삭제 오류", "파일을 찾을 수 없습니다.");
            }
        });

        memoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                File file = new File(MemoManager.getMemoFolder(), newVal);
                String[] result = MemoManager.readMemo(file);
                if (result != null) {
                    titleField.setText(result[0]);
                    memoArea.setText(result[1]);
                }
            }
        });


        VBox leftPanel = new VBox(10, new Label("저장된 메모 목록:"), memoListView);
        VBox rightPanel = new VBox(10,
                new Label("제목:"), titleField,
                new Label("메모 내용:"), memoArea,
                new HBox(10, saveButton, saveAsButton, settingButton, deleteButton)
        );

        leftPanel.setPrefWidth(200);
        rightPanel.setPrefWidth(480);

        HBox root = new HBox(20, leftPanel, rightPanel);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 750, 400);
        primaryStage.setTitle("메모장");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadMemoList() {
        File memoFolder = MemoManager.getMemoFolder();
        memoListView.getItems().clear();
        if (memoFolder.exists()) {
            File[] files = memoFolder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    memoListView.getItems().add(file.getName());
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
