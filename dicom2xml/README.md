# DICOM2XML/PDF/JPEG

This tool extracts out information (in the form of XML/PDF/JPEG) from the given DICOM file.


## Which IDE to use for exploring the code?

You can use any IDE of your choice, the project is pre-configured with Eclipse IDE.

## How to build?

1. Get the project source downloaded into your computer, use GIT Clone or simply download using [src link](https://github.com/ronishraphael/dcm2xml/archive/refs/heads/master.zip)

2. Under the source tree, nagivate to folder dicom2xml, and then run following command

Windows

```bash
mvnw clean install
```

Linux

```bash
./mvnw clean install
```

## How to run the tool

Either build the jar as mentioned above or just download the jar from [release](https://github.com/ronishraphael/dcm2xml/blob/master/dicom2xml/release/dicom2xml-1.0.jar)

Navigate to the folder which contains the jar and run the command given below

```bash
java -jar dicom2xml-1.0.jar <path-to-dicom-file> <path-to-the-output-folder-which-will-be-created>
```

Example:

```bash
java -jar dicom2xml-1.0.jar /home/smarty/Temp/pdf-1.dcm /home/smarty/Temp/pdf-1-output
```

## Sample files 

Sample input and output files are can be found [here](https://github.com/ronishraphael/dcm2xml/tree/master/dicom2xml/release)