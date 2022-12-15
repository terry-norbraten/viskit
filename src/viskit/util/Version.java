package viskit.util;

import edu.nps.util.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import viskit.VGlobals;

/**
 * Reads a file assumed to be in the following form (2 lines):
 * <pre>
 * &lt;major version&gt;.&lt;minor version&gt;.&lt;subminor version&gt;
 * $Date: 2007-12-16 11:44:04 -0800 (Sun, 16 Dec 2007) $
 * </pre>
 *
 * @version $Id$
 * @author ahbuss
 */
public class Version {

    static Logger LOG = LogUtils.getLogger(Version.class);

    protected String versionString;

    protected int majorVersion;

    protected int minorVersion;

    protected int patchVersion;
    
    protected LocalDate lastModified;

    protected int gitShaNumber;

    public Version(String fileName) {
        URL versionURL = VGlobals.class.getResource(fileName);
        try {
            InputStream versionStream = versionURL.openStream();
            Properties versionProperties = new Properties();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(versionStream));
            versionProperties.load(versionStream);
            Path versionPath = Paths.get(versionURL.toURI());
            BasicFileAttributes attributes
                    = Files.readAttributes(versionPath, BasicFileAttributes.class);
            Date date = new Date(attributes.lastModifiedTime().toMillis());
            Instant instant = date.toInstant();
            ZonedDateTime zoneDateTime = instant.atZone(ZoneId.systemDefault());
            lastModified = zoneDateTime.toLocalDate();
            majorVersion = Integer.parseInt(versionProperties.getProperty("major"));
            minorVersion = Integer.parseInt(versionProperties.getProperty("minor"));
            patchVersion = Integer.parseInt(versionProperties.getProperty("patch"));
            versionString = String.format("%s.%s.%s", majorVersion, minorVersion, patchVersion);
//            versionString = reader.readLine();
//            int[] version = parseVersionString(versionString);
//            majorVersion = version[0];
//            minorVersion = version[1];
//            patchVersion = version[2];
//            String dateString = reader.readLine();
//            lastModified = parseDateString(dateString);
//            String revisionString = reader.readLine();
//            gitShaNumber = parseRevisionString(revisionString);
//            versionString += "." + gitShaNumber;
        } catch (IOException | URISyntaxException e) {
            LOG.error("Problem reading " + fileName + ": " + e);
        }
    }

    protected static int[] parseVersionString(String versionString) {
        String[] versions = versionString.split("\\.");
        int[] versionNumber = new int[versions.length];
        for (int i = 0; i < versionNumber.length; ++i) {
            versionNumber[i] = Integer.parseInt(versions[i]);
        }
        return versionNumber;
    }

    protected static Date parseDateString(String dateString) {
        Date date = null;
        try {
            Pattern pattern
                    = Pattern.compile("\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d \\d\\d:\\d\\d:\\d\\d");
        Matcher matcher = pattern.matcher(dateString);
        if (matcher.find()) {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(matcher.group());
        }
        } catch (ParseException t) {
            LOG.error("Problem parsing date string " + dateString + ": " + t);
        }
        return date;
    }

    protected static int parseRevisionString(String revisionString) {
        String[] data = revisionString.split("\\D+");
        return Integer.parseInt(data[1]);
    }

    public String getVersionString() {
        return versionString;
    }

    public LocalDate getLastModified() {
        return lastModified;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    public int getShaNumber() {
        return gitShaNumber;
    }

    public boolean isSameVersionAs(String otherVersionString) {
        return getVersionString().equals(otherVersionString);
    }

    public boolean isHigherVersionThan(String otherVersionString) {
        int[] otherVersion = parseVersionString(otherVersionString);
        return getMajorVersion() > otherVersion[0]
                || getMinorVersion() > otherVersion[1]
                || getPatchVersion() > otherVersion[2];
    }

    @Override
    public String toString() {
        return "Version " + +getMajorVersion() + "."
                + getMinorVersion() + "." + getPatchVersion()
                + System.getProperty("line.separator")
                + "Last Modified: " + getLastModified();
    }
}
