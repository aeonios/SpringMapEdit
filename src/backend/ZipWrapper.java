package backend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.*;

public class ZipWrapper {
	public File smt;
	public File smf;
	
	private ZipFile zip;
	
	public ZipWrapper (File NewFile) throws IOException
	{
		try {
			zip = new ZipFile (NewFile);
		} catch (IOException e) {
			throw e;
		}
		smf = File.createTempFile("map", "smf");
		smf.deleteOnExit();
		smt = File.createTempFile("map", "smt");
		smt.deleteOnExit();
	}
	
	private ZipEntry findSmf()
	{
		return zip.getEntry("map.smf");
	}
	
	private ZipEntry findSmt()
	{
		return zip.getEntry("map.smt");
	}
	
	public File[] read() throws IOException
	{
		InputStream inStream = null;
		FileOutputStream outStream;
		ZipEntry entry;
		int len;
		byte b[];
		
		entry = findSmf();
		if (entry == null)
			return null;
		inStream = zip.getInputStream(entry);
		len = (int) entry.getSize();
		b = new byte[len];
		outStream =  new FileOutputStream(smf);
		inStream.read(b, 0, len);
		outStream.write(b, 0, len);
		inStream.close();
		outStream.close();
		entry = findSmt();
		if (entry == null)
			return null;
		inStream = zip.getInputStream(entry);
		len = (int) entry.getSize();
		b = new byte[len];
		outStream = new FileOutputStream(smt);
		inStream.read(b, 0, len);
		outStream.write(b, 0, len);
		inStream.close();
		outStream.close();
		zip.close();
		return new File[] {smf, smt};
	}
	
	public void write(File name) throws IOException
	{

	}
}
