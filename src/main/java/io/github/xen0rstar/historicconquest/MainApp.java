package io.github.xen0rstar.historicconquest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
    // private Circle pion;
    private final Group plateauGlobal = new Group();
    private final List<Group> toutesLesZones = new ArrayList<>();

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private static final double ZOOM_REDUCTION_FACTOR = 1.1;

    @Override
    public void start(Stage stage) {
        chargerMapDepuisJson("/io/github/xen0rstar/historicconquest/zones/map_config.json");

//        pion = new Circle(15, Color.RED);
//        pion.setStroke(Color.WHITE);
//        pion.setStrokeWidth(3);
//        pion.setMouseTransparent(true);

//        StackPane root = new StackPane(plateauGlobal, pion);
        StackPane root = new StackPane(plateauGlobal);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #2b2b2b;");

        Scene scene = new Scene(root, 1300, 850);

        // Navigation rapide (Touche ESPACE pour cycler entre les zones)
//        scene.setOnKeyPressed(e -> {
//            if (!toutesLesZones.isEmpty()) {
//                int index = 0;
//                sauterVersZone(toutesLesZones.get(index++ % toutesLesZones.size()));
//            }
//        });

        // --- ZOOM AVEC LA MOLETTE ---
        root.setOnScroll(event -> {
            double zoomFactor = (event.getDeltaY() > 0) ? ZOOM_REDUCTION_FACTOR : 1 / ZOOM_REDUCTION_FACTOR;

            // Appliquer le zoom sur le plateau
            plateauGlobal.setScaleX(plateauGlobal.getScaleX() * zoomFactor);
            plateauGlobal.setScaleY(plateauGlobal.getScaleY() * zoomFactor);

            event.consume();
        });

        // --- DÉPLACEMENT (PAN) AVEC LE CLIC DROIT OU MILIEU ---
        root.setOnMousePressed(event -> {
            // On enregistre la position de départ du clic
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = plateauGlobal.getTranslateX();
            translateAnchorY = plateauGlobal.getTranslateY();
        });

        root.setOnMouseDragged(event -> {
            // On déplace le plateau selon le mouvement de la souris
            plateauGlobal.setTranslateX(translateAnchorX + (event.getSceneX() - mouseAnchorX));
            plateauGlobal.setTranslateY(translateAnchorY + (event.getSceneY() - mouseAnchorY));
        });

        stage.setTitle("Système de Map JSON");
        stage.setScene(scene);
        stage.show();
    }

    private void chargerMapDepuisJson(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new Exception("Fichier JSON introuvable");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootArray = mapper.readTree(is);

            for (JsonNode bloc : rootArray) {
                String nomBloc = bloc.get("name").asText();

                Group groupeBloc = new Group();
                groupeBloc.setLayoutX(0.0);
                groupeBloc.setLayoutY(0.0);

                JsonNode children = bloc.get("children");
                for (JsonNode zone : children) {
                    String nomZone = zone.get("name").asText();
                    double zoneX = zone.get("x").asDouble();
                    double zoneY = zone.get("y").asDouble();

                    Group svgGroup = new Group();
                    chargerSVG("/io/github/xen0rstar/historicconquest/zones/" + nomBloc + "/" + nomZone + ".svg", svgGroup);

                    svgGroup.setLayoutX(zoneX);
                    svgGroup.setLayoutY(zoneY);

                    groupeBloc.getChildren().add(svgGroup);
                    toutesLesZones.add(svgGroup);
                }
                plateauGlobal.getChildren().add(groupeBloc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerSVG(String resourcePath, Group targetGroup) {
        try (InputStream svgStream = getClass().getResourceAsStream(resourcePath)) {
            if (svgStream == null) {
                System.err.println("Fichier non trouvé : " + resourcePath);
                return;
            }

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgStream);
            NodeList paths = doc.getElementsByTagName("path");
            for (int i = 0; i < paths.getLength(); i++) {
                Element el = (Element) paths.item(i);
                SVGPath p = new SVGPath();
                p.setContent(el.getAttribute("d"));

                p.setFill(Color.LIGHTGRAY);
                p.setStroke(Color.WHITE);
                p.setStrokeWidth(0.5);
                p.setStrokeLineJoin(StrokeLineJoin.ROUND);
                p.setStrokeLineCap(StrokeLineCap.ROUND);

                targetGroup.getChildren().add(p);
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

//    private void sauterVersZone(Group zoneCible) {
//        // boundsInParent prend en compte les setLayoutX/Y du parent et de l'enfant
//        var limites = zoneCible.localToScene(zoneCible.getBoundsInLocal());
//        pion.setTranslateX(limites.getMinX() + (limites.getWidth() / 2));
//        pion.setTranslateY(limites.getMinY() + (limites.getHeight() / 2));
//    }

    public static void main(String[] args) { launch(); }
}
