package org.memo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MemoApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TextField titleField = new TextField();
        titleField.setPromptText("제목을 입력하세요.");

        TextArea memoArea = new TextArea();
        memoArea.setPromptText("메모 내용을 입력하세요.");

        Button saveButton = new Button("저장");
        Button saveAsButton = new Button("다른 이름으로 저장");

        ListView<String> memoListView = new ListView<>();
        loadMemoList(memoListView);

        saveButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = memoArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("입력 오류", "제목과 내용을 모두 입력해주세요.");
            } else {
                MemoManager.saveMemo(title, content);
                titleField.clear();
                memoArea.clear();
                loadMemoList(memoListView);
                showAlert("저장 완료", "메모가 저장되었습니다.");
            }
        });

        saveAsButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = memoArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("입력 오류", "제목과 내용을 모두 입력해주세요.");
            } else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("다른 이름으로 저장");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("텍스트 파일", "*.txt"));

                File file = fileChooser.showSaveDialog(primaryStage);

                if (file != null) {
                    MemoManager.saveMemoToFile(file, title, content);
                    loadMemoList(memoListView);
                    showAlert("저장 완료", "파일이 저장되었습니다.");
                }
            }
        });

        memoListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                File file = new File("memos", newValue);
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
                new HBox(10, saveButton, saveAsButton)
        );

        leftPanel.setPrefWidth(200);
        rightPanel.setPrefWidth(480);

        HBox root = new HBox(20, leftPanel, rightPanel);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 700, 400);
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

    private void loadMemoList(ListView<String> listView) {
        File memoFolder = new File("memos");
        listView.getItems().clear();
        if (memoFolder.exists()) {
            File[] files = memoFolder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    listView.getItems().add(file.getName());
                }
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
