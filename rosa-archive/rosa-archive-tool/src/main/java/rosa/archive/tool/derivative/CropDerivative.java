package rosa.archive.tool.derivative;

import rosa.archive.core.store.Store;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CropDerivative extends BookDerivative {

    public CropDerivative(String collection, String book, PrintStream report, Store store) {
        super(collection, book, report, store);
    }

    private void generateCroppedImageList(boolean force) throws IOException {
        List<String> errors = new ArrayList<>();
//        store.generateAndWriteImageList(collection, book, force, errors);

        if (!errors.isEmpty()) {
            reportError("Errors:", errors);
        }
    }

    public boolean cropImages(boolean force) throws IOException {
        List<String> errors = new ArrayList<>();
        store.cropImages(collection, book, force, errors);

        return errors.isEmpty();
    }

}