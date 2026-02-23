package interfaceService;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;

public class MapNavigationService {

    private static final double ZOOM_FACTOR = 1.1;
    private static final double ZOOM_MIN = 0.2;
    private static final double ZOOM_MAX = 5.0;
    private double mouseAnchorX, mouseAnchorY;
    private double translateAnchorX, translateAnchorY;


    public void attachNavigation(StackPane root, Group plateau) {

        // --- ZOOM ---
        root.setOnScroll(event -> {
            double zoomFactor = (event.getDeltaY() > 0) ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;
            double oldScale = plateau.getScaleX();
            double newScale = oldScale * zoomFactor;

            if (newScale >= ZOOM_MIN && newScale <= ZOOM_MAX) {
                double mouseSceneX = event.getSceneX();
                double mouseSceneY = event.getSceneY();

                javafx.geometry.Point2D mouseInMap = plateau.sceneToLocal(mouseSceneX, mouseSceneY);

                plateau.setScaleX(newScale);
                plateau.setScaleY(newScale);

                javafx.geometry.Point2D mouseInSceneAfterZoom = plateau.localToScene(mouseInMap);

                double errorX = mouseInSceneAfterZoom.getX() - mouseSceneX;
                double errorY = mouseInSceneAfterZoom.getY() - mouseSceneY;

                plateau.setTranslateX(plateau.getTranslateX() - errorX);
                plateau.setTranslateY(plateau.getTranslateY() - errorY);
            }
            event.consume();
        });

        // --- DÉPLACEMENT (PAN) ---
        root.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = plateau.getTranslateX();
            translateAnchorY = plateau.getTranslateY();
        });

        root.setOnMouseDragged(event -> {
            plateau.setTranslateX(translateAnchorX + (event.getSceneX() - mouseAnchorX));
            plateau.setTranslateY(translateAnchorY + (event.getSceneY() - mouseAnchorY));
        });
    }
}
