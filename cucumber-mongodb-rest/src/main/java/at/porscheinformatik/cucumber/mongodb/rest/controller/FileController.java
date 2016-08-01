package at.porscheinformatik.cucumber.mongodb.rest.controller;

import static org.springframework.data.mongodb.core.query.Query.*;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
    private MongoDbFactory dbFactory;

    @Autowired
    private MongoConverter converter;

    @RequestMapping(value = "/{collection}/{fileName}/", method = RequestMethod.GET)
    public void findFileByName(
        @PathVariable(value = "collection") String collection,
        @PathVariable(value = "fileName") String fileName,
        HttpServletResponse response) throws IOException
    {
        GridFsOperations gridfs = new GridFsTemplate(dbFactory, converter, collection);

        // TODO check maybe Spring can handle DBCursor automaticallys
        GridFSDBFile file = gridfs.findOne(query(GridFsCriteria.whereFilename().is(fileName)));

        if (file != null)
        {
            response.setContentLength((int) file.getLength());
            String contentType = file.getContentType();
            if (contentType != null)
            {
                response.setContentType(contentType);
            }
            ServletOutputStream out = response.getOutputStream();

            file.writeTo(out);
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
