package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.map.Zone;
import com.historicconquest.historicconquest.map.ZonePathfinder;
import com.historicconquest.historicconquest.ui.GameHUD;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.CubicCurveTo;

import java.util.List;

public class GameController {
    private final WorldMap worldMap;
    private final ZoneInfoPanel zoneInfoPanel;
    private final GameHUD gameHUD;
    private final Group mapInterface;

    private Zone selectedZone;
    private Zone startZoneForPath;  // Zone de départ pour le pathfinding

    public GameController(WorldMap worldMap, ZoneInfoPanel zoneInfoPanel, GameHUD gameHUD, Group mapInterface) {
        this.worldMap = worldMap;
        this.zoneInfoPanel = zoneInfoPanel;
        this.gameHUD = gameHUD;
        this.mapInterface = mapInterface;
        this.selectedZone = null;
        this.startZoneForPath = null;

        initialize();
    }

    private void initialize() {
        mapInterface.getChildren().addAll(worldMap.getBlocs());
        gameHUD.initializeMap(mapInterface);

        worldMap.getBlocs().forEach(bloc ->
            bloc.getZones().forEach(zone ->
                zone.setOnMouseClicked(event -> handleZoneClick(zone))
            )
        );

        zoneInfoPanel.hide();
    }



    private void handleZoneClick(Zone zone) {
        // TESTE D'AFFICHAGE DU PATHFINDING -----------------------------------------------------------
        if (startZoneForPath == null) {
            startZoneForPath = zone;
            System.out.println("\n🎯 [CLIC 1] Zone de départ sélectionnée: " + zone.getName());
            selectZone(zone);
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

        Group groupDrawPath = drawPath(pathResult.zones());
        if (groupDrawPath != null) {
            groupDrawPath.setId("DrawPathGroup");
            mapInterface.getChildren().add(groupDrawPath);
        }

        // --------------------------------------------------------------------------------------------

        deselectZone();
        startZoneForPath = null;
    }



    private void selectZone(Zone zone) {
        selectedZone = zone;
        zone.setFocusedZone(true);
        zone.updateColor(Color.web("#D4AF37"));

        zoneInfoPanel.setData(zone.getName());
        zoneInfoPanel.show();
    }

    private void deselectZone() {
        if (selectedZone != null) {
            selectedZone.setFocusedZone(false);
            selectedZone.updateColor(selectedZone.getColor());
            selectedZone = null;
        }

        zoneInfoPanel.hide();
    }



    private Group drawPath(List<Zone> path) {
        if (path == null || path.size() < 2) return null;

        Group root = new Group();

        // Récupérer tous les points du chemin
        double[] xPoints = new double[path.size()];
        double[] yPoints = new double[path.size()];

        for (int i = 0; i < path.size(); i++) {
            Zone zone = path.get(i);
            Bounds bounds = zone.getZoneSVGGroup().getBoundsInParent();
            if (bounds != null) {
                xPoints[i] = bounds.getCenterX();
                yPoints[i] = bounds.getCenterY();
            }
        }

        // Créer une courbe lisse (Catmull-Rom spline) qui passe par tous les points
        Path smoothPath = createSmoothPath(xPoints, yPoints);
        smoothPath.setStroke(Color.web("#FFD700"));
        smoothPath.setStrokeWidth(5.0);
        smoothPath.setOpacity(1.0);
        smoothPath.getStrokeDashArray().addAll(15.0, 8.0);
        smoothPath.setFill(null);
        smoothPath.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        smoothPath.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        root.getChildren().add(smoothPath);

        // Ajouter des cercles visibles sur chaque zone du chemin
//        for (int i = 0; i < path.size(); i++) {
//            Zone zone = path.get(i);
//            Bounds bounds = zone.getZoneSVGGroup().getBoundsInParent();
//
//            if (bounds != null) {
//                double x = bounds.getCenterX();
//                double y = bounds.getCenterY();
//
//                Circle circle = new Circle(x, y, 12.0);
//                circle.setFill(Color.web("#FFD700"));
//                circle.setOpacity(1.0);
//
//                // Marquer le départ et l'arrivée différemment
//                if (i == 0) {
//                    circle.setRadius(15.0);
//                    circle.setFill(Color.web("#00FF00"));  // Vert pour le départ
//                    circle.setStroke(Color.BLACK);
//                    circle.setStrokeWidth(2.0);
//                } else if (i == path.size() - 1) {
//                    circle.setRadius(15.0);
//                    circle.setFill(Color.web("#FF0000"));  // Rouge pour l'arrivée
//                    circle.setStroke(Color.BLACK);
//                    circle.setStrokeWidth(2.0);
//                } else {
//                    circle.setStroke(Color.web("#FFD700"));
//                    circle.setStrokeWidth(1.5);
//                }
//
//                root.getChildren().add(circle);
//            }
//        }

        return root;
    }

    /**
     * Crée une courbe Catmull-Rom lisse qui passe par tous les points
     */
    private Path createSmoothPath(double[] xPoints, double[] yPoints) {
        Path path = new Path();

        if (xPoints.length < 2) return path;

        // Commencer au premier point
        path.getElements().add(new MoveTo(xPoints[0], yPoints[0]));

        // Si seulement 2 points, une ligne droite suffit
        if (xPoints.length == 2) {
            path.getElements().add(new CubicCurveTo(
                xPoints[0], yPoints[0],
                xPoints[1], yPoints[1],
                xPoints[1], yPoints[1]
            ));
            return path;
        }

        // Pour chaque segment, créer une courbe de Bézier
        for (int i = 0; i < xPoints.length - 1; i++) {
            // Points de contrôle pour la courbe de Bézier
            // Utiliser les points voisins pour créer une courbe lisse

            double x0 = (i > 0) ? xPoints[i - 1] : xPoints[i];
            double y0 = (i > 0) ? yPoints[i - 1] : yPoints[i];

            double x1 = xPoints[i];
            double y1 = yPoints[i];

            double x2 = xPoints[i + 1];
            double y2 = yPoints[i + 1];

            double x3 = (i + 2 < xPoints.length) ? xPoints[i + 2] : xPoints[i + 1];
            double y3 = (i + 2 < yPoints.length) ? yPoints[i + 2] : yPoints[i + 1];

            // Calculer les points de contrôle (Catmull-Rom)
            double cp1x = x1 + (x2 - x0) / 6.0;
            double cp1y = y1 + (y2 - y0) / 6.0;

            double cp2x = x2 - (x3 - x1) / 6.0;
            double cp2y = y2 - (y3 - y1) / 6.0;

            // Ajouter la courbe cubique
            path.getElements().add(new CubicCurveTo(cp1x, cp1y, cp2x, cp2y, x2, y2));
        }

        return path;
    }
}
