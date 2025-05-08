package org.memo;

import javafx.application.Application;
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

        saveButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = memoArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("입력 오류", "제목과 내용을 모두 입력해주세요.");
            } else {
                MemoManager.saveMemo(title, content);
                titleField.clear();
                memoArea.clear();
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
                    showAlert("저장 완료", "파일이 저장되었습니다.");
                }
            }
        });

        HBox buttonBox = new HBox(10, saveButton, saveAsButton);
        VBox layout = new VBox(10, new Label("제목: "), titleField, new Label("메모 내용: "), memoArea, buttonBox);

        layout.setPadding(new javafx.geometry.Insets(10));

        Scene scene = new Scene(layout, 400, 350);
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

    public static void main(String[] args) {
        launch(args);
    }
}
