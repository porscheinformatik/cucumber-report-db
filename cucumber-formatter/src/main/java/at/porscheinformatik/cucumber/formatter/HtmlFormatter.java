package at.porscheinformatik.cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.JSONFormatter;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Formats Cucumber results in a HTML page with search capability.
 * <p>
 * This formatter delegates to {@link JSONFormatter} for generating the JSON report file. After that it includes a
 * simple <a href="http://angularjs.org/">AngularJS</a> application that reads the JSON and displays it in the browser.
 * The app includes a simple search mechanism.
 */
public class HtmlFormatter extends AbstractJsonFormatter
{
    protected static final String JSON_REPORT_FILENAME = "report.json";
    protected static final String FORMATTER_DIR = "/";
    protected static final String[] TEXT_ASSETS =
    {
        "index.html",
        
        "pages/feature.html",
        "pages/features.html",

        "css/bootstrap-spacelab.css",
        "css/colorbox.css",
        "css/style.css",
        
        "js/angular.min.js",
        "js/angular-route.min.js",
        "js/angular.localStorageModule.js",
        "js/app.js",
        "js/charts.js",
        "js/bootstrap.min.js",
        "js/config.js",
        "js/dateAndTime.js",
        "js/jquery.colorbox-min.js",
        "js/jquery.min.js",
        "js/json3.min.js",
        "js/lightbox.js",
        "js/ui-bootstrap-tpls-0.5.0.min.js",
        
        "img/loading.gif",
        "img/load.gif",
        "img/controls.png",
        "img/border1.png",
        "img/border2.png",
        
        "fonts/glyphicons-halflings-regular.eot",
        "fonts/glyphicons-halflings-regular.svg",
        "fonts/glyphicons-halflings-regular.ttf",
        "fonts/glyphicons-halflings-regular.woff"
    };

    protected final File htmlReportDir;
    protected final File htmlReportJsDir;
    protected final File htmlReportCssDir;
    protected final File htmlReportImgDir;
    protected final File htmlReportFontsDir;
    protected final File htmlReportPagesDir;

    protected Map<String, Object> currentFeature;
    protected Map<String, Object> currentScenario;
    protected List<Map<String, Object>> currentSteps;
    protected int embeddedIndex = 1;
    protected int currentStepResultIndex;

    protected Date date;

    public HtmlFormatter(File htmlReportDir)
    {
        super(htmlReportDir);
        this.htmlReportDir = htmlReportDir;
        this.htmlReportJsDir = new File(htmlReportDir + "/js");
        this.htmlReportCssDir = new File(htmlReportDir + "/css");
        this.htmlReportImgDir = new File(htmlReportDir + "/img");
        this.htmlReportFontsDir = new File(htmlReportDir + "/fonts");
        this.htmlReportPagesDir = new File(htmlReportDir + "/pages");
    }

    @Override
    public void done()
    {
        super.done();
        copyReportFiles(htmlReportDir);
    }

    private void copyReportFiles(File htmlReportDir)
    {
        htmlReportJsDir.mkdirs();
        htmlReportCssDir.mkdirs();
        htmlReportImgDir.mkdirs();
        htmlReportFontsDir.mkdirs();
        htmlReportPagesDir.mkdirs();
        
        for (String textAsset : TEXT_ASSETS)
        {
            InputStream textAssetStream = getClass().getResourceAsStream(FORMATTER_DIR + textAsset);
            writeStreamAndClose(textAssetStream, reportFileOutputStream(htmlReportDir, textAsset));
        }
    }

    @Override
    protected String doEmbedding(String extension, byte[] data)
    {
        String fileName = new StringBuilder("embedded")
            .append(embeddedIndex++)
            .append(".")
            .append(extension).toString();
        writeBytesAndClose(data, reportFileOutputStream(htmlReportDir, fileName));

        return fileName;
    }

    @Override
    protected Appendable jsOut(File htmlReportDir)
    {
        htmlReportDir.mkdirs();
        try
        {
            return new OutputStreamWriter(reportFileOutputStream(htmlReportDir, JSON_REPORT_FILENAME), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new CucumberException(e);
        }
    }

    protected OutputStream reportFileOutputStream(File htmlReportDir, String fileName)
    {
        
        File file = new File(htmlReportDir, fileName);
        try
        {
            return new FileOutputStream(file);
        }
        catch (FileNotFoundException e)
        {
            throw new CucumberException("Error creating file: " + file.getAbsolutePath(), e);
        }
    }

    protected void writeBytesAndClose(byte[] buf, OutputStream out)
    {
        try
        {
            out.write(buf);
        }
        catch (IOException e)
        {
            throw new CucumberException("Unable to write to report file item: ", e);
        }
        finally
        {
            closeQuietly(out);
        }
    }

    protected void writeStreamAndClose(InputStream in, OutputStream out)
    {
        byte[] buffer = new byte[16 * 1024];
        try
        {
            int len = in.read(buffer);
            while (len != -1)
            {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
        }
        catch (IOException e)
        {
            throw new CucumberException("Unable to write to report file item: ", e);
        }
        finally
        {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    private void closeQuietly(Closeable closeable)
    {
        try
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
        catch (IOException ioe)
        {
            // ignore
        }
    }
}
