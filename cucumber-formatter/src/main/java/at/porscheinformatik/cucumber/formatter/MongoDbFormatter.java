package at.porscheinformatik.cucumber.formatter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import gherkin.formatter.NiceAppendable;

import org.apache.commons.lang3.StringUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Use SystemProperty {@link at.porscheinformatik.cucumber.formatter.MongoDbFormatter#BASEURL_SYS_PROP} to specify a
 * url where the cucumber-report-web is provided
 */
public class MongoDbFormatter extends AbstractJsonFormatter
{

    public static final String DEFAULT_COLLECTION = "collection_version";

    public static final String BASEURL_SYS_PROP = "cucumber.report.server.baseUrl";
    public static final String DEFAULT_BASE_URL = "http://localhost:8081";

    private NiceAppendable jsonOutput;

    private WebResource restResource;

    public MongoDbFormatter() throws UnsupportedEncodingException
    {
        Client client = Client.create(new DefaultClientConfig());
        restResource = client.resource(getBaseUrl());
        jsonOutput = new NiceAppendable(new OutputStreamWriter(new DbOutputStream(), "UTF-8"));
    }

    private String getBaseUrl()
    {
        String baseUrlFromProperty = System.getProperty(BASEURL_SYS_PROP);
        if (StringUtils.isEmpty(baseUrlFromProperty))
        {
            return DEFAULT_BASE_URL;
        }
        return baseUrlFromProperty;
    }

    protected void dbInsertJson(String data)
    {
        restResource.path("rest").path("cucumberplugin").path(getCollection()).path("report").entity(data).post();
    }

    protected void dbInsertMedia(String fileName, InputStream inputStream)
    {
        restResource.path("rest").path("cucumberplugin").path(getCollection()).path("media").path(fileName)
                .entity(inputStream).post();
    }

    protected String getCollection()
    {
        return DEFAULT_COLLECTION;
    }

    @Override
    protected String doEmbedding(String extension, byte[] data)
    {
        String fileName = new StringBuilder("embedded")
                .append(embeddedIndex++)
                .append("_")
                .append(getFormattedDate())
                .append(".")
                .append(extension).toString();

        dbInsertMedia(fileName, new ByteArrayInputStream(data));
        return fileName;
    }

    @Override
    protected NiceAppendable jsOut()
    {
        return jsonOutput;
    }

    public class DbOutputStream extends OutputStream
    {
        private static final String DATE_REGEX = "\"date\":\"(.*)\"";
        private static final String DATE_REPLACEMENT = "\"date\":\\{\"\\$date\":\"$1\"\\}";

        private StringBuilder output = new StringBuilder();

        @Override
        public void write(int b) throws IOException
        {
            output.append((char) b);
        }

        @Override
        public void close() throws IOException
        {
            final String preparedJsonForDb = output.toString().replaceAll(DATE_REGEX, DATE_REPLACEMENT);
            dbInsertJson(preparedJsonForDb);
        }
    }

}
