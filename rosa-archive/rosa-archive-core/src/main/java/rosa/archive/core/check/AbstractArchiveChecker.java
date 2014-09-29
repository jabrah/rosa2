package rosa.archive.core.check;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import rosa.archive.core.ByteStreamGroup;
import rosa.archive.core.config.AppConfig;
import rosa.archive.core.serialize.Serializer;
import rosa.archive.model.ChecksumInfo;
import rosa.archive.model.HasId;
import rosa.archive.model.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class AbstractArchiveChecker {

    protected AppConfig config;
    protected Map<Class, Serializer> serializerMap;

    AbstractArchiveChecker(AppConfig config, Map<Class, Serializer> serializerMap) {
        this.config = config;
        this.serializerMap = serializerMap;
    }

    /**
     * Read an item from the archive, as identified by {@code item.getId()}. This ensures
     * that the object in the archive is readable. It does not check the bit integrity of
     * the object.
     *
     * @param item item to check
     * @param bsg byte stream group
     * @param <T> item type
     * @return list of errors found while performing the check
     */
    protected <T extends HasId> List<String> attemptToRead(T item, ByteStreamGroup bsg) {
        List<String> errors = new ArrayList<>();

        if (item == null || StringUtils.isBlank(item.getId())) {
            errors.add("Item missing from archive. [" + bsg.name() + "]");
            return errors;
        }

        try (InputStream in = bsg.getByteStream(item.getId())) {
            // This will read the item in the archive and report any errors
            serializerMap.get(item.getClass()).read(in, errors);
        } catch (IOException e) {
            errors.add("Failed to read [" + bsg.name() + ":" + item.getId() + "]");
        }

        return errors;
    }

    /**
     *
     * @param bsg byte stream group holding all streams to the archive
     * @param checkSubGroups TRUE - check streams in all sub-groups within {@code bsg}
     * @param required is the checksum validation required?
     * @return list of errors found while checking
     */
    protected List<String> checkBits(ByteStreamGroup bsg, boolean checkSubGroups, boolean required) {
        List<String> errors = new ArrayList<>();

        String checksumId = null;
        List<String> streams = new ArrayList<>();
        try {
            // List all stream names, in order to look for CHECKSUM stream
            streams.addAll(bsg.listByteStreamNames());
        } catch (IOException e) {
            errors.add("Could not get byte stream names from group. [" + bsg.name() + "]");
        }

        // Look for CHECKSUM stream
        for (String name : streams) {
            if (name.contains(config.getSHA1SUM())) {
                checksumId = name;
                break;
            }
        }

        if (checksumId != null) {
            errors.addAll(checkStreams(bsg, checksumId));
        } else if (required) {
            // checksumId will always be NULL here (no checksum stream exists)
            errors.add("Failed to get checksum data from group. [" + bsg.name() + "]");
        }

        // Check all byte stream groups within 'bsg' if checkSubGroups is TRUE
        if (checkSubGroups) {
            List<ByteStreamGroup> subGroups = new ArrayList<>();
            try {
                subGroups.addAll(bsg.listByteStreamGroups());
            } catch (IOException e) {
                errors.add("Could not get byte stream groups from top level group. [" + bsg.name() + "]");
            }

            for (ByteStreamGroup group : subGroups) {
                errors.addAll(checkBits(group, true, required));
            }
        }

        return errors;
    }

    /**
     * Check the input streams in the specified ByteStreamGroup and all groups that it contains.
     * If a ByteStreamGroup contains stored checksum values for the other streams, the checksum
     * of each stream is calculated and compared to the stored value. Otherwise, the stream is
     * read. In either case, each stream is checked to see if it can be opened and read successfully.
     *
     * @param bsg byte stream group
     * @param checksumName name of checksum item in this group
     * @return list of errors found while performing check
     */
    protected List<String> checkStreams(ByteStreamGroup bsg, String checksumName) {
        List<String> errors = new ArrayList<>();

        if (checksumName == null) {
            return errors;
        }

        // Load all stored checksum data
        ChecksumInfo checksumInfo = null;
        try (InputStream in = bsg.getByteStream(checksumName)) {
            checksumInfo = (ChecksumInfo) serializerMap.get(ChecksumInfo.class).read(in, errors);
        } catch (IOException e) {
            errors.add("Failed to read checksums. [" + bsg.name() + ":" + checksumName + "]");
        }

        if (checksumInfo == null) {
            return errors;
        }

        List<String> streamIds = new ArrayList<>();
        try {
            streamIds.addAll(bsg.listByteStreamNames());
        } catch (IOException e) {
            errors.add("Could not get stream IDs from group. [" + bsg.name() + "]");
        }

        // Calculate checksum for all InputStreams, compare to stored values
        for (String streamId : streamIds) {
            // Do not validate checksum for checksum file...
            if (streamId.equalsIgnoreCase(checksumName)) {
                continue;
            }

            String storedHash = checksumInfo.checksums().get(streamId);
            if (storedHash == null) {
                continue;
            }

            try (InputStream in = bsg.getByteStream(streamId)){
                String hash = calculateChecksum(in, HashAlgorithm.SHA1);

                if (!storedHash.equalsIgnoreCase(hash)) {
                    errors.add("Calculated hash value is different from stored value!\n"
                                    + "    Calc:   {" + streamId + ", " + hash + "}\n"
                                    + "    Stored: {" + streamId + ", " + storedHash + "}"
                    );
                }

            } catch (IOException | NoSuchAlgorithmException e) {
                errors.add("Could not read item. [" + streamId + "]");
            }
        }

        return errors;
    }

    /**
     * Compute the hash of an input stream using the specified algorithm.
     *
     * @param in input
     * @param algorithm hashing algorithm to use
     * @return hash value as hex string
     * @throws IOException
     * @throws java.security.NoSuchAlgorithmException
     */
    protected String calculateChecksum(InputStream in, HashAlgorithm algorithm)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest md = DigestUtils.getDigest(algorithm.toString());
        DigestUtils.updateDigest(md, in);
        return Hex.encodeHexString(md.digest());
    }

}
