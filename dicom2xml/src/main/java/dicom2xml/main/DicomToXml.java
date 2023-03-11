package dicom2xml.main;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.SAXWriter;

/**
 * This class contains the main function of the tool.
 * 
 * @author ronish
 */
public class DicomToXml {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			final File outputFolder = createEmptyDirectory("/home/ronish/DevTools/Temp/out1");
			createXMLFromDICOMFile(new File("/home/ronish/DevTools/Temp/color-1.dcm"), outputFolder);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createXMLFromDICOMFile(final File inputDCMFile, final File outputFolder)
			throws IOException, TransformerConfigurationException {

		final String tempFileName = UUID.randomUUID().toString();
		final File tempOutPutFile = new File(outputFolder, tempFileName);
		final TransformerHandler transformerHandler = getTransformerHandler();

		transformerHandler.setResult(new StreamResult(tempOutPutFile));

		final DicomInputStream dicomInputStream = new DicomInputStream(inputDCMFile);
		final SAXWriter saxWriter = new SAXWriter(transformerHandler);
		saxWriter.setIncludeNamespaceDeclaration(true);
		dicomInputStream.setDicomInputHandler(saxWriter);
		final Attributes attributes = dicomInputStream.readDataset();
		dicomInputStream.close();
	}

	private static File createEmptyDirectory(final String folderPath) {
		final File folder = new File(folderPath);
		if (folder.exists()) {
			throw new IllegalArgumentException(String.format(
					"Folder %s already exists, use a different folder or delete the folder and try again.", folderPath));
		}
		folder.mkdirs();
		return folder;
	}

	private static TransformerHandler getTransformerHandler() throws TransformerConfigurationException, IOException {
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		return tf.newTransformerHandler();
	}

}
