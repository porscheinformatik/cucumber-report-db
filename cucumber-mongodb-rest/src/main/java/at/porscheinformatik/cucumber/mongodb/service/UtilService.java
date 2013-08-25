package at.porscheinformatik.cucumber.mongodb.service;

import org.springframework.stereotype.Service;

import com.mongodb.DBCursor;
import com.mongodb.util.JSON;

/**
 * @author Stefan Mayer (yms)
 */
@Service
public class UtilService
{
    public String formatJson(DBCursor cursor)
    {
        StringBuilder buf = new StringBuilder();

        buf.append("[");
        while (cursor.hasNext())
        {
            JSON.serialize(cursor.next(), buf);
            buf.append(",");
        }

        if (buf.length() > 1)
        {
            buf.setCharAt(buf.length() - 1, ']');
        }
        else
        {
            buf.append("]");
        }
        cursor.close();

        return buf.toString();
    }
}
