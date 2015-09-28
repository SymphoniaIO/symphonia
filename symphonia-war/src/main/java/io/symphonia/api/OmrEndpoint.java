package io.symphonia.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import omr.WellKnowns;
import omr.util.FileUtil;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * A simple JAX-RS 2.0 REST service which is responsible for processing requests and forwarding them to {@link OmrService}.
 * 
 * @author sbunciak
 * 
 */
// TODO: http://examples.javacodegeeks.com/enterprise-java/rest/jax-rs-download-file/
// TODO: http://stackoverflow.com/questions/8147956/return-file-from-resteasy-server
@Path("/omr")
@Api(value = "/omr")
public class OmrEndpoint {

    @Inject
    OmrService omrService;

    private static final Logger LOG = Logger.getLogger(OmrEndpoint.class);

    // Content-Disposition header name
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    // (html) form input field name
    private static final String FILE_INPUT_FIELD = "attachment";

    /*
     * MusicXML output
     */
    @POST
    @Path("/musicxml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @ApiOperation(produces = MediaType.APPLICATION_XML, consumes = MediaType.MULTIPART_FORM_DATA, value = "Converts given image to MusicXML")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attachment", value = "File to upload", required = true, dataType = "file", paramType = "form")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "OMR successful."),
            @ApiResponse(code = 204, message = "No supported image provided."),
            @ApiResponse(code = 500, message = "Error processing input image.")
    })
    public Response getMusicXML(@ApiParam(value = "File input", hidden = true) MultipartFormDataInput input) {
        File imageFile = extractFileFromFormInput(input);

        if (imageFile == null) {
            return Response.noContent().build();
        }

        File xmlFile = null;
        try {
            xmlFile = omrService.getMusicXmlFromImage(imageFile);
            String xml = IOUtils.toString(new FileInputStream(xmlFile));
            return Response.ok(xml).build();
        } catch (IOException e) {
            LOG.error("Error processing input image.", e);
        } finally {
            // finally delete all files
            imageFile.delete();
            if (xmlFile != null) {
                xmlFile.delete();
            }
        }

        return Response.serverError().build();
    }

    /*
     * PDF output
     */
    @POST
    @Path("/pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Converts given image into PDF")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attachment", value = "File to upload", required = true, dataType = "file", paramType = "form")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "OMR successful."),
            @ApiResponse(code = 204, message = "No supported image provided."),
            @ApiResponse(code = 500, message = "Error processing input image.")
    })
    public Response getPdf(@ApiParam(value = "Form data input", hidden = true) MultipartFormDataInput input) {
        File imageFile = extractFileFromFormInput(input);

        if (imageFile == null) {
            return Response.noContent().build();
        }

        File pdfFile = null;
        try {
            pdfFile = omrService.getPdfFromImage(imageFile);
            byte[] content = IOUtils.toByteArray(new FileInputStream(pdfFile));
            return Response.ok(content).header(CONTENT_DISPOSITION, "attachment; filename=output.pdf").header("Content-Length", pdfFile.length()).build();
        } catch (IOException e) {
            LOG.error("Error processing input image.", e);
        } finally {
            // finally delete all files
            imageFile.delete();
            if (pdfFile != null) {
                pdfFile.delete();
            }
        }

        return Response.serverError().build();
    }

    /*
     * Auxiliary methods
     */

    private File extractFileFromFormInput(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        // Get file data to save
        List<InputPart> inputParts = uploadForm.get(FILE_INPUT_FIELD);

        for (InputPart inputPart : inputParts) {
            try {

                // get file name
                String fileName = getFileName(inputPart.getHeaders());
                String ext = FileUtil.getExtension(fileName);

                // proceed only if extension is supported 
                if (SupportedImage.valueOf(ext.toUpperCase().substring(1)) == null) {
                    return null;
                }

                // convert the uploaded file to an Input Stream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);

                // constructs uploaded file path
                File image = File.createTempFile(fileName, ext, WellKnowns.DATA_FOLDER);

                // convert image to byte array and write to file
                byte[] bytes = IOUtils.toByteArray(inputStream);
                return writeFile(bytes, image.getAbsolutePath());
            } catch (IOException e) {
                LOG.error("Error saving input file for local procesing.", e);
            }
        }

        return null;
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst(CONTENT_DISPOSITION).split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

    private File writeFile(byte[] content, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            LOG.error("File doesn not exist: " + file.getAbsolutePath());
            file.createNewFile();
        }
        FileOutputStream fop = new FileOutputStream(file);
        fop.write(content);
        fop.flush();
        fop.close();

        return file;
    }

    /*
     * We support BMP, GIF, JPEG, PNG, TIFF.
     */
    enum SupportedImage {
        BMP, GIF, JPEG, JPG, PNG, TIFF;
    }
}
