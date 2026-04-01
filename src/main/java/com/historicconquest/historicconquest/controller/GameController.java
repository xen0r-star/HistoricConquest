package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.map.ZonePathfinder;
import com.historicconquest.historicconquest.view.GameHUD;
import com.historicconquest.historicconquest.view.ZoneInfoPanel;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.animation.PathTransition;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GameController {
    /*
    * =======================================================
    *     CODE TEMPORAIRE POUR TESTER LE VISUEL DE LA MAP
    * =======================================================
    */

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final WorldMap worldMap;
    private final ZoneInfoPanel zoneInfoPanel;
    private final GameHUD gameHUD;
    private final Group mapInterface;
    private final MapView mapView;

    private ZoneView selectedZoneView;
    private Zone startZoneForPath;  // Zone de départ pour le pathfinding

    public GameController(WorldMap worldMap, ZoneInfoPanel zoneInfoPanel, GameHUD gameHUD, Group mapInterface) {
        this.worldMap = worldMap;
        this.zoneInfoPanel = zoneInfoPanel;
        this.gameHUD = gameHUD;
        this.mapInterface = mapInterface;
        this.mapView = MapViewFactory.build(worldMap, true);
        this.selectedZoneView = null;
        this.startZoneForPath = null;

        initialize();
    }

    private void initialize() {
        mapInterface.getChildren().add(mapView.getRoot());
        gameHUD.initializeMap(mapInterface);

        mapView.getBlocs().forEach(blocView ->
            blocView.getZoneViews().forEach(zoneView ->
                zoneView.setOnMouseClicked(event -> handleZoneClick(zoneView))
            )
        );

        zoneInfoPanel.hide();
    }



    private void handleZoneClick(ZoneView zoneView) {
        Zone zone = zoneView.getZone();
        // TESTE D'AFFICHAGE DU PATHFINDING -----------------------------------------------------------
        if (startZoneForPath == null) {
            startZoneForPath = zone;
            logger.debug("[PATHFINDING] First click - start zone selected: {}", zone.getName());
            selectZone(zoneView);
            return;
        }

        if (startZoneForPath == zone) {
            deselectZone();
            startZoneForPath = null;
            logger.debug("[PATHFINDING] Click cancelled - same zone clicked twice: {}", zone.getName());
            return;
        }

        logger.debug("[PATHFINDING] Second click - target zone selected: {}", zone.getName());

        ZonePathfinder.PathResult pathResult = ZonePathfinder.findPath(startZoneForPath, zone);


        Node cible = mapInterface.lookup("#DrawPathGroup");
        if (cible != null) {
            mapInterface.getChildren().remove(cible);
        }


        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n╔════════════════════════════════╗\n");
            sb.append("║     RÉSULTAT DU PATHFINDING    ║\n");
            sb.append("╠════════════════════════════════╣\n");
            sb.append(String.format("║ De: %-27s║\n", startZoneForPath.getName()));
            sb.append(String.format("║ À:  %-27s║\n", zone.getName()));
            sb.append(String.format("║ Type: %-25s║\n", pathResult.type()));

            if (pathResult.zones() != null) {
                sb.append(String.format("║ Chemin (%d zones):              ║\n", pathResult.zones().size()));
                for (int i = 0; i < pathResult.zones().size(); i++) {
                    String zoneName = (i + 1) + ". " + pathResult.zones().get(i).getName();
                    sb.append(String.format("║   %-26s║\n", zoneName));
                }
                sb.append("╠════════════════════════════════╣\n");
                if (pathResult.type() == ZonePathfinder.PathType.DIRECT) {
                    sb.append("║ ✓ Trajet possible!                 ║\n");
                } else {
                    sb.append("║ ✗ Trajet trop long (> 4 zones)     ║\n");
                }
            } else {
                sb.append("║ Aucun chemin trouvé!              ║\n");
            }
            sb.append("╚════════════════════════════════╝\n");
            logger.debug(sb.toString());
        }

        Group groupDrawPath = drawPath(mapView.toViews(pathResult.zones()));
        if (groupDrawPath != null) {
            groupDrawPath.setId("DrawPathGroup");
            mapInterface.getChildren().add(groupDrawPath);
        }

        // --------------------------------------------------------------------------------------------

        deselectZone();
        startZoneForPath = null;
    }



    private void selectZone(ZoneView zoneView) {
        selectedZoneView = zoneView;
        zoneView.setFocusedZone(true);
        zoneView.setColor(Color.web("#D4AF37"));

        zoneInfoPanel.setData(zoneView.getZone().getName());
        zoneInfoPanel.show();
    }

    private void deselectZone() {
        if (selectedZoneView != null) {
            selectedZoneView.setFocusedZone(false);
            selectedZoneView.setColor(selectedZoneView.getZone().getColor());
            selectedZoneView = null;
        }

        zoneInfoPanel.hide();
    }



    private Group drawPath(List<ZoneView> path) {
        if (path == null || path.size() < 2) return null;

        Group root = new Group();

        // Créer une ligne droite simple entre chaque zone consécutive
        Path directPath = new Path();

        // Obtenir le premier point (centre de la première zone)
        ZoneView firstZone = path.getFirst();
        Bounds firstBounds = firstZone.getZoneSVGGroup().getBoundsInParent();
        double startX = firstBounds.getCenterX();
        double startY = firstBounds.getCenterY();

        directPath.getElements().add(new MoveTo(startX, startY));

        // Ajouter des segments droits entre chaque zone successive
        for (int i = 1; i < path.size(); i++) {
            ZoneView zone = path.get(i);
            Bounds bounds = zone.getZoneSVGGroup().getBoundsInParent();
            directPath.getElements().add(new javafx.scene.shape.LineTo(bounds.getCenterX(), bounds.getCenterY()));
        }

        directPath.setStroke(Color.web("#FFD700"));
        directPath.setStrokeWidth(2.0);
        directPath.setOpacity(1.0);
        directPath.getStrokeDashArray().addAll(15.0, 8.0);
        directPath.setFill(null);
        directPath.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        directPath.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        root.getChildren().add(directPath);

        // Ajouter l'animation de la charrette
        Node carriage = createCarriageAnimation(directPath, path.size());
        if (carriage != null) {
            root.getChildren().add(carriage);
        }

        return root;
    }




    private Node createCarriageAnimation(Path path, int numberOfZones) {
        try {
            // Charger l'image SVG de la charrette
            var inputStream = getClass().getResourceAsStream("/pawn/Horse-drawn1.png");
            if (inputStream == null) {
                System.err.println("Erreur: Le fichier Horse-drawn1.svg n'a pas pu être trouvé");
                return null;
            }

            Image carriageImage = new Image(inputStream);

            ImageView carriage = new ImageView(carriageImage);
            carriage.setScaleX(0.05);
            carriage.setScaleY(0.05);

            // Créer un groupe pour contenir la charrette
            Group carriageGroup = new Group();
            carriageGroup.getChildren().add(carriage);

            // Créer l'animation PathTransition
            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.seconds(numberOfZones * 1.5)); // Durée proportionnelle au nombre de zones
            pathTransition.setPath(path);
            pathTransition.setNode(carriageGroup);
            pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
            pathTransition.setCycleCount(1);
            pathTransition.setAutoReverse(false);

            // Lancer l'animation
            pathTransition.play();

            return carriageGroup;
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la charrette: " + e.getMessage());
            return null;
        }
    }
}
