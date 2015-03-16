package at.porscheinformatik.cucumber.formatter;

import org.junit.Assert;
import org.junit.Test;

public class MimeTypeToExtensionsUtilTest
{
    @Test
    public void mapApplicationZipToZip()
    {
        String extension = MimeTypeToExtensionsUtil.getExtension("application/zip");
        Assert.assertEquals("zip", extension);
    }

    @Test
    public void mapImagePngToPng()
    {
        String extension = MimeTypeToExtensionsUtil.getExtension("image/png");
        Assert.assertEquals("png", extension);
    }

    @Test
    public void mapImageJpegToJpg()
    {
        String extension = MimeTypeToExtensionsUtil.getExtension("image/jpeg");
        Assert.assertEquals("jpg", extension);
    }

    @Test
    public void mapTextPlainToLog()
    {
        String extension = MimeTypeToExtensionsUtil.getExtension("text/plain");
        Assert.assertEquals("log", extension);
    }

}