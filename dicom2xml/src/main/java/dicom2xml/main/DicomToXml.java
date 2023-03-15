package dicom2xml.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.data.Attributes;
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
	 * Main function of the tool. This API extracts info from a given DICOM file
	 * to easily readable formats like XML, PDF and JPEG.
	 * 
	 * @param args
	 *            string arguments array. args[0] is the input DICOM file, and
	 *            args[1] is the path of the output folder to be created.
	 * 
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 */
	public static void main(String[] args)
			throws TransformerConfigurationException, IOException {

		//args = new String[2];
		//args[0] = "/home/ronish/DevTools/Temp/color-1.dcm";
		//args[1] = "/home/ronish/DevTools/Temp/out1";
		
		validateInputs(args);

		final File inputDCMFile = new File(args[0]);
		final File outputFolder = createEmptyDirectory(args[1]);

		final Map<Integer, String> metaDataMap = getMetaData(inputDCMFile);

		final String filePrefix = getOutputFileNamePrefix(inputDCMFile, metaDataMap);
		
		extractXMLFromDICOMFile(inputDCMFile, outputFolder, filePrefix);
		extractJPEGFromDICOMFile(inputDCMFile, outputFolder, filePrefix, metaDataMap);
		extractPDFFromDICOMFile(inputDCMFile, outputFolder, filePrefix);

	}

	/**
	 * Get meta data map of the file.
	 * 
	 * @param inputDCMFile
	 *            the input DICOM file.
	 * 
	 * @return the metadata map <TagID,String>.
	 * 
	 * @throws IOException
	 */
	private static Map<Integer, String> getMetaData(final File inputDCMFile)
			throws IOException {
		final Map<Integer, String> tempMap = new HashMap<Integer, String>();
		final DicomInputStream dis = new DicomInputStream(inputDCMFile);
		try {
			final Attributes metaAttributes = dis.getFileMetaInformation();
			final Attributes attributes = dis.readDataset();
			tempMap.put(Tag.TransferSyntaxUID,
					metaAttributes.getString(Tag.TransferSyntaxUID));
			tempMap.put(Tag.PatientName, attributes.getString(Tag.PatientName));
			tempMap.put(Tag.PatientID, attributes.getString(Tag.PatientID));
		} finally {
			dis.close();
		}
		return Collections.unmodifiableMap(tempMap);
	}

	/**
	 * Logs the given text.
	 * 
	 * @param logText
	 *            the log to be printed.
	 */
	private static void log(final String logText) {
		// TODO Use a logger if required.
		System.out.println(logText);
	}

	/**
	 * Validates given inputs.
	 * 
	 * @param args
	 *            string arguments array to be validated. args[0] is the input
	 *            DICOM file, and args[1] is the path of the output folder.
	 */
	private static void validateInputs(final String[] args) {
		if (args.length != 2) {
			final StringBuilder sb = new StringBuilder(
					"Incorrect usgae, please try again.");
			sb.append(
					"Correct usage is <todo> path-to-input-file path-to-a-new-output-folder-to-be-created");
			sb.append(
					"Example : <todo> /user/x/1.dcm /user/x/new-out-put-folder-to-be-created");
			throw new IllegalArgumentException(sb.toString());
		}

		if (isNullOrEmptyOrBlank(args[0]) || isNullOrEmptyOrBlank(args[1])) {
			final StringBuilder sb = new StringBuilder(
					"Incorrect usgae, please try again.");
			sb.append("One or more arguments are empty or blank.");
			sb.append(
					"Correct usage is <todo> path-to-input-file path-to-a-new-output-folder-to-be-created");
			sb.append(
					"Example : <todo> /user/x/1.dcm /user/x/new-out-put-folder-to-be-created");
			throw new IllegalArgumentException(sb.toString());
		}

		final File inputFile = new File(args[0]);
		if (!inputFile.exists()) {
			throw new IllegalArgumentException(String.format(
					"Input file %s doesn't exist, please use correct file name and try again.",
					args[0]));
		}

		final File outputFolder = new File(args[1]);
		if (outputFolder.exists()) {
			throw new IllegalArgumentException(String.format(
					"Folder %s already exists, use a different folder or delete the folder and try again.",
					args[1]));
		}
	}

	/**
	 * Checks whetehr the given string is null, empty or blank.
	 * 
	 * @param string
	 *            the string to be checked.
	 * 
	 * @return true if null, empty or blank, false otherwise.
	 */
	private static boolean isNullOrEmptyOrBlank(final String string) {
		if (string == null || string.isEmpty() || string.isBlank()) {
			return true;
		}
		return false;
	}

	/**
	 * Exracts information from given DICOM file and write it into a XML file.
	 * 
	 * @param inputDCMFile
	 *            the input DICOM file.
	 * @param outputFolder
	 *            the output folder where the xml has to be created.
	 * @param outputFilePrefix
	 *            the file name prefix.
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 */
	public static void extractXMLFromDICOMFile(final File inputDCMFile,
			final File outputFolder, final String outputFilePrefix)
			throws IOException, TransformerConfigurationException {

		final File xmlOutputFile = new File(outputFolder,
				String.format("%s.xml", outputFilePrefix));
		final TransformerHandler transformerHandler = ((SAXTransformerFactory) TransformerFactory
				.newInstance()).newTransformerHandler();
		transformerHandler.setResult(new StreamResult(xmlOutputFile));

		final DicomInputStream dicomInputStream = new DicomInputStream(
				inputDCMFile);
		try {
			final SAXWriter saxWriter = new SAXWriter(transformerHandler);
			saxWriter.setIncludeNamespaceDeclaration(true);
			dicomInputStream.setDicomInputHandler(saxWriter);
			dicomInputStream.readDataset();
		} finally {
			dicomInputStream.close();
		}
		log("Extracting XML is finished.");
	}

	/**
	 * Checks whether the metadata of the DICOM file indicates whether the file
	 * contains a JPEG image or not.
	 * 
	 * @param metadataMap
	 *            the metadata map.
	 * @return true if the given DICOM file contains a JPEG image, false
	 *         otherwise.
	 * @throws IOException
	 */
	private static boolean containsJPEG(final Map<Integer, String> metadataMap) {
		final String transferSyntaxUID = metadataMap.get(Tag.TransferSyntaxUID);
		return transferSyntaxUID != null
				&& transferSyntaxUID.contains("1.2.840.10008.1.2.4");
	}

	/**
	 * Extracts JPEG (if any) from given DICOM file and write it into a JPEG
	 * file.
	 * 
	 * @param inputDCMFile
	 *            the input DICOM file.
	 * @param outputFolder
	 *            the output folder where the JPEG has to be created.
	 * @param outputFilePrefix
	 *            the file name prefix.
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 */
	public static void extractJPEGFromDICOMFile(final File inputDCMFile,
			                                    final File outputFolder,
			                                    final String outputFilePrefix,
			                                    final Map<Integer, String> metadataMap)
			throws IOException, TransformerConfigurationException {
		if (!containsJPEG(metadataMap)) {
			log("DICOM input file doesn't contain any jpeg document.");
			return;
		}
		final String imageType = "JPEG";
		final ImageReader imageReader = ImageIO
				.getImageReadersByFormatName("DICOM").next();
		final ImageInputStream imageInputStream = ImageIO
				.createImageInputStream(inputDCMFile);
		try {
			imageReader.setInput(imageInputStream);
			final int numberOfImages = imageReader.getNumImages(true);
			for (int i = 0; i < numberOfImages; ++i) {
				final BufferedImage bufferedImage = imageReader.read(i);
				final ImageWriter imageWriter = ImageIO
						.getImageWritersByFormatName(imageType).next();
				final File jpegOutputFile = new File(outputFolder,
						String.format("%s_%s.%s", outputFilePrefix, i + 1,
								imageType));
				final RandomAccessFile raf = new RandomAccessFile(
						jpegOutputFile, "rw");
				raf.setLength(0);
				final FileImageOutputStream fileImageOutputStream = new FileImageOutputStream(
						raf);
				imageWriter.setOutput(fileImageOutputStream);
				imageWriter.write(bufferedImage);
				fileImageOutputStream.close();
				log(String.format("Extracted %s.",
						jpegOutputFile.getAbsolutePath()));
			}
		} finally {
			imageInputStream.close();
		}
		log("Extracting JPEG is finished.");
	}

	/**
	 * Exracts PDF (if any) from given DICOM file and write it into a PDF file.
	 * 
	 * @param inputDCMFile
	 *            the input DICOM file.
	 * @param outputFolder
	 *            the output folder where the PDF has to be created.
	 * @param outputFilePrefix
	 *            the file name prefix.
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 */
	public static void extractPDFFromDICOMFile(final File inputDCMFile,
			final File outputFolder, final String outputFilePrefix)
			throws IOException {

		final DicomInputStream dicomInputStream = new DicomInputStream(
				inputDCMFile);
		try {
			final Attributes attributes = dicomInputStream.readDataset();
			final byte[] encapsulatedDocumentData = (byte[]) attributes
					.getValue(Tag.EncapsulatedDocument);
			if (encapsulatedDocumentData == null) {
				log("DICOM input file doesn't contain any encapsulated document.");
				dicomInputStream.close();
				return;
			}
			final File pdfOutputFile = new File(outputFolder,
					String.format("%s.%s", outputFilePrefix, "pdf"));
			final FileOutputStream fileOutputStream = new FileOutputStream(
					pdfOutputFile);
			try {
				fileOutputStream.write(encapsulatedDocumentData, 0,
						encapsulatedDocumentData.length);
			} finally {
				fileOutputStream.close();
			}
			log(String.format("Extracted %s.", pdfOutputFile.getAbsolutePath()));
		} finally {
			dicomInputStream.close();
		}
		log("Extracting PDF is finished.");
	}

	/**
	 * Gets the output file name prefix for the given DICOM file.
	 * 
	 * @param inputDCMFile
	 *            the input DICOM file.
	 * @return the output file name prefix.
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 */
	public static String getOutputFileNamePrefix(final File inputDCMFile,
			                                     final Map<Integer, String> metadataMap) {
		return String.format("%s_%s", metadataMap.get(Tag.PatientName),
				metadataMap.get(Tag.PatientID));
	}

	/**
	 * Creates a folder as per the given path.
	 * 
	 * @param folderPath
	 *            the folder path.
	 * @return the folder created.
	 */
	private static File createEmptyDirectory(final String folderPath) {
		final File folder = new File(folderPath);
		folder.mkdirs();
		return folder;
	}

}
