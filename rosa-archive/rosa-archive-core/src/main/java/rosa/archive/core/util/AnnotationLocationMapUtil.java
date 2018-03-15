package rosa.archive.core.util;

import org.apache.commons.io.IOUtils;
import rosa.archive.core.ByteStreamGroup;
import rosa.archive.model.aor.AorLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rosa.archive.core.ArchiveConstants.ID_LOCATION_MAP;

// TODO actually a serializer
public class AnnotationLocationMapUtil {

    public static Map<String, AorLocation> annotationIdMap(ByteStreamGroup collection, PrintStream report) {
        Map<String, AorLocation> result = new HashMap<>();

        if (!collection.hasByteStream(ID_LOCATION_MAP)) {
            return result;
        }

        try (InputStream map = collection.getByteStream(ID_LOCATION_MAP)) {
            /*
                Each line:
                annotation_id,collection,book,page
             */
            IOUtils.readLines(map, "UTF-8").forEach(line -> {
                String[] parts = line.split(",");

                if (parts.length != 4) {
                    return;
                }
                String id = parts[0];
                AorLocation loc = new AorLocation(parts[1], parts[2], parts[3], id);

                if (!result.containsKey(id)) {
                    print(report, " >> Duplicate ID found (" + id + ")");
                } else {
                    result.put(id, loc);
                }
            });
        } catch (IOException e) {
            print(report, "Failed to parse '" + ID_LOCATION_MAP + "' for (" + collection.name() + ")");
        }

        return result;
    }

    /**
     * Get the collection's map of IDs to locations within that collection, ignoring errors.
     *
     * @param collection collection
     * @return map of IDs to locations in corpus
     */
    public static Map<String, AorLocation> annotationIdMap(ByteStreamGroup collection) {
        return annotationIdMap(collection, null);
    }

    private static void print(PrintStream report, String message) {
        if (report == null || message == null) {
            return;
        }
        report.println(message);
    }

    public static void write(ByteStreamGroup collection, Map<String, AorLocation> map, List<String> errors) {
        if (map == null) {
            errors.add("No annotation ID map found to write.");
            return;
        } else if (collection == null) {
            errors.add("No collection to write to.");
            return;
        }

        try (OutputStream out = collection.getOutputStream(ID_LOCATION_MAP)) {
            for (Map.Entry<String, AorLocation> entry : map.entrySet()) {
                IOUtils.write(entry.getKey() + "," + entry.getValue().getCollection() + "," + entry.getValue().getBook(), out);
            }
        } catch (IOException e) {
            errors.add("Failed to write '" + ID_LOCATION_MAP + "' for collection (" + collection.name() + ")");
        }

    }

}
