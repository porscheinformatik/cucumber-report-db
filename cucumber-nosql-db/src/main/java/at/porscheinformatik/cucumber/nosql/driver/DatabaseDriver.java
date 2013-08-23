package at.porscheinformatik.cucumber.nosql.driver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

/**
 * @author Stefan Mayer (yms)
 * @param <T> class where the fetched data should be mapped
 */
public interface DatabaseDriver
{
    void insertData(String data);

    void insertFile(File file) throws IOException;

    void insertMedia(String name, InputStream data);

    Collection<InputStream> fetchMedia(String name);

    <T> Collection<T> fetchByFromToDate(Class<T> type, String fieldName, Date from, Date to);

    <T> Collection<T> fetchByLastDay(Class<T> type, String fieldName);

    <T> Collection<T> fetchByDate(Class<T> type, String fieldName, Date date);

    <T> Collection<T> fetchByValue(Class<T> type, String fieldName, String value);

    <T> T fetchLastByValue(Class<T> type, String fieldName, String value);

    void connect() throws Exception;

    void close();
}
