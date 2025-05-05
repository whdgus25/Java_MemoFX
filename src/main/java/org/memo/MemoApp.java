package org.memo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MemoApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        TextArea memoArea = new TextArea();
        Button saveButton = new Button("저장");

        saveButton.setOnAction(e -> {
           String content = memoArea.getText();
           MemoManager.saveMemo(content);
           memoArea.clear();
           showAlert("저장 완료", "메모가 저장되었습니다.");
        });

        VBox layout = new VBox(10, new Label("메모 입력: "), memoArea, saveButton);
        Scene scene = new Scene(layout, 400, 300);

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
