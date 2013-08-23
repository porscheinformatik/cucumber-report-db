package at.porscheinformatik.cucumber.formatter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import cucumber.runtime.CucumberException;
import gherkin.formatter.model.Scenario;

/**
 * @author Stefan Mayer (yms)
 */
public abstract class AbstractDbFormatter extends AbstractJsonFormatter
{
    protected abstract void dbInsertJson(String data);

    protected abstract void dbInsertMedia(String fileName, InputStream inputStream);

    public AbstractDbFormatter(File htmlReportDir)
    {
        super(htmlReportDir);
    }

    @Override
    public void scenario(Scenario scenario)
    {
        super.scenario(scenario);
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
    protected Appendable jsOut(File htmlReportDir)
    {
        try
        {
            return new OutputStreamWriter(new DbOutputStream(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new CucumberException(e);
        }
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
