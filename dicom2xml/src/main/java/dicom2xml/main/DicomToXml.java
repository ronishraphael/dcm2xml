package dicom2xml.main;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.image.ICCProfile;

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
			final File inputDicomFile = new File("/home/ronish/DevTools/Temp/color-1.dcm");
			final String filePrefix = getOutputFileNamePrefix(inputDicomFile);
			createXMLFromDICOMFile(inputDicomFile, outputFolder, filePrefix);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createXMLFromDICOMFile(final File inputDCMFile, final File outputFolder,
			final String outputFilePrefix) throws IOException, TransformerConfigurationException {

		final File xmlOutputFile = new File(outputFolder, String.format("%s.xml", outputFilePrefix));
		final TransformerHandler transformerHandler = getTransformerHandler();

		transformerHandler.setResult(new StreamResult(xmlOutputFile));

		final DicomInputStream dicomInputStream = new DicomInputStream(inputDCMFile);
		final SAXWriter saxWriter = new SAXWriter(transformerHandler);
		saxWriter.setIncludeNamespaceDeclaration(true);
		dicomInputStream.setDicomInputHandler(saxWriter);
		dicomInputStream.readDataset();
		dicomInputStream.close();
	}
	
	public static void createJPEGFromDICOMFile(final File inputDCMFile, final File outputFolder,
			final String outputFilePrefix) throws IOException, TransformerConfigurationException {
		//final ICCProfi
		final File jpegOutputFile = new File(outputFolder, String.format("%s.jpeg", outputFilePrefix));
		
	}

	public static String getOutputFileNamePrefix(final File inputDCMFile)
			throws IOException, TransformerConfigurationException {
		final DicomInputStream dicomInputStream = new DicomInputStream(inputDCMFile);
		final Attributes attributes = dicomInputStream.readDataset();
		dicomInputStream.close();
		return String.format("%s_%s", attributes.getString(Tag.PatientName), attributes.getString(Tag.PatientID));
	}

	private static File createEmptyDirectory(final String folderPath) {
		final File folder = new File(folderPath);
		if (folder.exists()) {
			throw new IllegalArgumentException(String.format(
					"Folder %s already exists, use a different folder or delete the folder and try again.",
					folderPath));
		}
		folder.mkdirs();
		return folder;
	}

	private static TransformerHandler getTransformerHandler() throws TransformerConfigurationException, IOException {
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		return tf.newTransformerHandler();
	}

}
