package com.historicconquest.historicconquest.controller.page.multiplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.util.Objects;

public class PlayerInfoController {
    private static final String SVG_PERSON = "M12.5 12.5C13.5816 12.5 14.6389 12.1793 15.5383 11.5784C16.4376 10.9774 17.1385 10.1233 17.5525 9.12405C17.9664 8.12477 18.0747 7.02519 17.8637 5.96435C17.6527 4.90352 17.1318 3.92908 16.367 3.16426C15.6022 2.39944 14.6277 1.8786 13.5669 1.66758C12.5061 1.45657 11.4065 1.56487 10.4072 1.97879C9.40792 2.3927 8.55381 3.09365 7.9529 3.99298C7.35199 4.89231 7.03125 5.94964 7.03125 7.03125C7.03125 8.48165 7.60742 9.87265 8.63301 10.8982C9.6586 11.9238 11.0496 12.5 12.5 12.5ZM12.5 14.0625C9.11035 14.0625 2.34375 16.1563 2.34375 20.3125V23.4375H22.6562V20.3125C22.6562 16.1563 15.8896 14.0625 12.5 14.0625Z";
    private static final String SVG_ROBOT = "M12.4999 3.03027C13.0022 3.03027 13.4839 3.22979 13.839 3.58494C14.1941 3.94009 14.3937 4.42177 14.3937 4.92402C14.3937 5.62507 14.0155 6.24069 13.4468 6.56257V7.76569H14.3937C16.1518 7.76569 17.838 8.46412 19.0812 9.70733C20.3244 10.9505 21.0228 12.6367 21.0228 14.3949H21.9697C22.2207 14.3949 22.4613 14.4945 22.6389 14.6718C22.8164 14.8492 22.9163 15.0897 22.9166 15.3407V18.1813C22.9166 18.4324 22.8168 18.6733 22.6393 18.8509C22.4617 19.0284 22.2208 19.1282 21.9697 19.1282H21.0228V20.0751C21.0228 20.3238 20.9739 20.57 20.8787 20.7998C20.7835 21.0295 20.644 21.2383 20.4682 21.4141C20.2923 21.59 20.0836 21.7295 19.8538 21.8247C19.624 21.9198 19.3778 21.9688 19.1291 21.9688H5.87075C5.36868 21.9688 4.88715 21.7694 4.53204 21.4145C4.17692 21.0596 3.97728 20.5782 3.977 20.0761V19.1292H3.03013C2.779 19.1292 2.53816 19.0295 2.36059 18.8519C2.18301 18.6743 2.08325 18.4335 2.08325 18.1824V15.3407C2.08325 15.0896 2.18301 14.8487 2.36059 14.6711C2.53816 14.4936 2.779 14.3938 3.03013 14.3938H3.977C3.97728 12.6358 4.67583 10.9499 5.91901 9.70696C7.16219 8.46397 8.84818 7.76569 10.6062 7.76569H11.553V6.56257C11.2642 6.39779 11.0244 6.15925 10.858 5.87135C10.6916 5.58346 10.6047 5.25653 10.6062 4.92402C10.6062 4.42177 10.8057 3.94009 11.1608 3.58494C11.516 3.22979 11.9977 3.03027 12.4999 3.03027ZM8.23846 13.4469C7.6105 13.4469 7.00827 13.6964 6.56424 14.1404C6.12021 14.5845 5.87075 15.1867 5.87075 15.8146C5.87075 16.4426 6.12021 17.0448 6.56424 17.4889C7.00827 17.9329 7.6105 18.1824 8.23846 18.1824C8.86642 18.1824 9.46865 17.9329 9.91268 17.4889C10.3567 17.0448 10.6062 16.4426 10.6062 15.8146C10.6062 15.1867 10.3567 14.5845 9.91268 14.1404C9.46865 13.6964 8.86642 13.4469 8.23846 13.4469ZM16.7614 13.4469C16.1334 13.4469 15.5312 13.6964 15.0872 14.1404C14.6431 14.5845 14.3937 15.1867 14.3937 15.8146C14.3937 16.4426 14.6431 17.0448 15.0872 17.4889C15.5312 17.9329 16.1334 18.1824 16.7614 18.1824C17.3893 18.1824 17.9916 17.9329 18.4356 17.4889C18.8796 17.0448 19.1291 16.4426 19.1291 15.8146C19.1291 15.1867 18.8796 14.5845 18.4356 14.1404C17.9916 13.6964 17.3893 13.4469 16.7614 13.4469Z";


    @FXML private Pane root;
    @FXML private Label PlayerName;
    @FXML private Label Annotation;
    @FXML private Label PlayerStatus;
    @FXML private Label PlayerPing;
    @FXML private SVGPath PlayerIcon;
    @FXML private HBox pingContainer;
    @FXML private ImageView RemoveBtn;


    public Pane getRoot() {
        return root;
    }

    public void setPlayerName(String name) {
        if (PlayerName != null && name != null) {
            PlayerName.setText(name);
        }
    }

    public void setAnnotation(String annotation) {
        if (Annotation != null) {
            Annotation.setText(Objects.requireNonNullElse(annotation, ""));
        }
    }

    public void setPlayerStatus(String status) {
        if (PlayerStatus != null && status != null) {
            PlayerStatus.setText(status);
        }
    }

    public void setPlayerPing(String ping) {
        if (PlayerPing != null && ping != null) {
            PlayerPing.setText(ping);
        }
    }

    public void setPlayerIcon(Icon icon, Color color) {
        if (PlayerIcon != null && icon != null && color != null) {
            PlayerIcon.setContent(icon == Icon.PERSON ? SVG_PERSON : SVG_ROBOT);
            PlayerIcon.setFill(color);
        }
    }

    public void showPing(boolean show) {
        pingContainer.setVisible(show);
        pingContainer.setManaged(show);
    }

    public void setOnRemove(Runnable onClose) {
        if (RemoveBtn != null) {
            RemoveBtn.setOnMouseClicked(event -> {
                if (onClose != null) {
                    onClose.run();
                }
            });
        }
    }

    public void removeOnRemove() {
        if (RemoveBtn != null) {
            ((Pane) RemoveBtn.getParent()).getChildren().remove(RemoveBtn);
        }
    }


    public enum Icon{
        PERSON,
        ROBOT
    }
}


