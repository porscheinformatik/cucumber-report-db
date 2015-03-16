package at.porscheinformatik.cucumber.formatter;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeToExtensionsUtil
{
    protected static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>()
    {{
            put("image/jpeg", "jpg");
            put("text/plain", "log");
    }};

    public static String getExtension(final String mimeType)
    {
        if (MIME_TYPES_EXTENSIONS.containsKey(mimeType))
        {
            return MIME_TYPES_EXTENSIONS.get(mimeType);
        }
        return mimeType.substring(mimeType.indexOf("/") + 1);
    }
}
