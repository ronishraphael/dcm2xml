package dicom2xml.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
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
			final File inputDicomFile = new File("/home/ronish/DevTools/Temp/pdf-1.dcm");
			final String filePrefix = getOutputFileNamePrefix(inputDicomFile);
			createXMLFromDICOMFile(inputDicomFile, outputFolder, filePrefix);
			createJPEGFromDICOMFile(inputDicomFile, outputFolder, filePrefix);
			createPDFFromDICOMFile(inputDicomFile, outputFolder, filePrefix);
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
		System.out.println("Extracting XML is finished.");
	}
	
	private static boolean containsJPEG(final File inputDCMFile) throws IOException {
		final DicomInputStream dis = new DicomInputStream(inputDCMFile);
		final String bla = dis.getFileMetaInformation().getString(Tag.TransferSyntaxUID);
		dis.close();
		return bla != null && bla.contains("1.2.840.10008.1.2.4");
	}

	public static void createJPEGFromDICOMFile(final File inputDCMFile, final File outputFolder,
			final String outputFilePrefix) throws IOException, TransformerConfigurationException {
		if (!containsJPEG(inputDCMFile)) {
			System.out.println("DICOM input file doesn't contain any jpeg document.");
			return;
		}
		final String imageType = "JPEG";
		final ImageReader imageReader = ImageIO.getImageReadersByFormatName("DICOM").next();
		final ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputDCMFile);
		imageReader.setInput(imageInputStream);
		final int numberOfImages = imageReader.getNumImages(true);
		for (int i = 0; i < numberOfImages; ++i) {
			final BufferedImage bufferedImage = imageReader.read(i);
			final ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(imageType).next();
			final File jpegOutputFile = new File(outputFolder,
					String.format("%s_%s.%s", outputFilePrefix, i + 1, imageType));
			final RandomAccessFile raf = new RandomAccessFile(jpegOutputFile, "rw");
			raf.setLength(0);
			final FileImageOutputStream fileImageOutputStream = new FileImageOutputStream(raf);
			imageWriter.setOutput(fileImageOutputStream);
			imageWriter.write(bufferedImage);
			fileImageOutputStream.close();
			System.out.println(String.format("Extracted %s.", jpegOutputFile.getAbsolutePath()));
		}
		imageInputStream.close();
		System.out.println("Extracting JPEG is finished.");
	}

	public static void createPDFFromDICOMFile(final File inputDCMFile, final File outputFolder,
			final String outputFilePrefix) throws IOException {

		DicomInputStream dis = new DicomInputStream(inputDCMFile);
		Attributes attributes = dis.readDataset();
		final byte[] value = (byte[]) attributes.getValue(Tag.EncapsulatedDocument);
		if (value == null) {
			System.out.println("DICOM input file doesn't contain any encapsulated document.");
			dis.close();
			return;
		}
		final File pdfOutputFile = new File(outputFolder, String.format("%s.%s", outputFilePrefix, "pdf"));
		final FileOutputStream fos = new FileOutputStream(pdfOutputFile);
		fos.write(value, 0, value.length);
		fos.close();
		dis.close();
		System.out.println(String.format("Extracted %s.", pdfOutputFile.getAbsolutePath()));
		System.out.println("Extracting PDF is finished.");
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
