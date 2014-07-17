package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import at.porscheinformatik.cucumber.nosql.driver.MongoDbDriver;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * @author Stefan Mayer (yms)
 */
@Controller
@RequestMapping("/rest/file")
public class FileController
{
    @Autowired
    private MongoDbDriver mongoDbDriver;

    @RequestMapping(value = "/{dbName}/{collection}/{fileName}/", method = RequestMethod.GET)
    public void findFileByName(
        @PathVariable(value = "dbName") String dbName,
        @PathVariable(value = "collection") String collection,
        @PathVariable(value = "fileName") String fileName,
        HttpServletResponse response) throws IOException
    {
        mongoDbDriver.connect(dbName, collection);
        GridFSDBFile file = mongoDbDriver.fetchMediaFile(fileName);
        if (file != null)
        {
            response.setContentLength((int) file.getLength());
            String contentType = file.getContentType();
            if (contentType != null)
            {
                response.setContentType(contentType);
            }
            ServletOutputStream out = response.getOutputStream();

            while ((file.writeTo(out)) > 0);
            out.flush();
            out.close();
        }
        else
        {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    public GridFSDBFile getByFilename(DB db, String collection, String filename) throws UnknownHostException
    {
        GridFS gridFS = new GridFS(db, collection);
        return gridFS.findOne(filename);
    }
}
