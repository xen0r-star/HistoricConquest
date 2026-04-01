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

import java.util.List;

public class GameController {
    /*
    * =======================================================
    *     CODE TEMPORAIRE POUR TESTER LE VISUEL DE LA MAP
    * =======================================================
    */

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
            System.out.println("\n🎯 [CLIC 1] Zone de départ sélectionnée: " + zone.getName());
            selectZone(zoneView);
            return;
        }

        if (startZoneForPath == zone) {
            deselectZone();
            startZoneForPath = null;
            System.out.println("❌ Clic annulé - même zone");
            return;
        }

        System.out.println("🎯 [CLIC 2] Zone d'arrivée sélectionnée: " + zone.getName());

        ZonePathfinder.PathResult pathResult = ZonePathfinder.findPath(startZoneForPath, zone);


        Node cible = mapInterface.lookup("#DrawPathGroup");
        if (cible != null) {
            mapInterface.getChildren().remove(cible);
        }


        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║     RÉSULTAT DU PATHFINDING    ║");
        System.out.println("╠════════════════════════════════╣");
        System.out.println("║ De: " + String.format("%-27s", startZoneForPath.getName()) + "║");
        System.out.println("║ À:  " + String.format("%-27s", zone.getName()) + "║");
        System.out.println("║ Type: " + String.format("%-25s", pathResult.type()) + "║");

        if (pathResult.zones() != null) {
            System.out.println("║ Chemin (" + pathResult.zones().size() + " zones):              ║");
            for (int i = 0; i < pathResult.zones().size(); i++) {
                String zoneName = (i + 1) + ". " + pathResult.zones().get(i).getName();
                System.out.println("║   " + String.format("%-26s", zoneName) + "║");
            }
            System.out.println("╠════════════════════════════════╣");
            if (pathResult.type() == ZonePathfinder.PathType.DIRECT) {
                System.out.println("║ ✓ Trajet possible!                 ║");
            } else {
                System.out.println("║ ✗ Trajet trop long (> 4 zones)     ║");
            }
        } else {
            System.out.println("║ Aucun chemin trouvé!              ║");
        }
        System.out.println("╚════════════════════════════════╝\n");

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
