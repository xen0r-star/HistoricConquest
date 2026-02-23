package interfaceService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.historicconquest.historicconquest.Bloc;
import com.historicconquest.historicconquest.Zone;
import javafx.scene.Group;

import java.io.InputStream;
import java.util.List;

public class MapLoaderService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void chargerMap(String path, Group plateauGlobal, List<Zone> toutesLesZones) {
        try (InputStream is = MapLoaderService.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Fichier JSON introuvable  : " + path);
                return;
            }

            JsonNode rootArray = MAPPER.readTree(is);

            for (JsonNode blocNode : rootArray) {
                Bloc bloc = new Bloc(blocNode);

                // On ajoute le bloc visuellement au plateau
                plateauGlobal.getChildren().add(bloc);

                toutesLesZones.addAll(bloc.getZones());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
