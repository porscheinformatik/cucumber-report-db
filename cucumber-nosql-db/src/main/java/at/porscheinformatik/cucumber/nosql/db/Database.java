package at.porscheinformatik.cucumber.nosql.db;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author Stefan Mayer (yms)
 * @param <Result>
 * @param <Argument>
 */
public interface Database
{
    void connect() throws Exception;

    void insert(String data);

    void insertMedia(String name, InputStream data);

    Collection<InputStream> fetchMedia(String name);

    <T> Collection<T> fetch(Class<T> type, String query);

    <T> T fetchLast(Class<T> type, String query);

    void close();
}
