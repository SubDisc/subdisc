/**
 * TODO use fileNameExtension class.
 */
package nl.liacs.subdisc;

import java.io.*;

import javax.swing.*;

import nl.liacs.subdisc.cui.*;
import nl.liacs.subdisc.gui.*;

public class FileHandler
{
	private static final long serialVersionUID = 1L;

	public static enum Action
	{
		OPEN_FILE, OPEN_DATABASE, SAVE
	};

	// remember the directory of the last used file, defaults to users' 
	// (platform specific) home directory if the the path cannot be resolved
	private static String itsLastFileLocation = ".";

	private Table itsTable;
	private SearchParameters itsSearchParameters;
	private File itsFile;

	// Main FileHandler
	public FileHandler(Action theAction)
	{
		switch(theAction)
		{
			case OPEN_FILE :
			{
				showFileChooser(theAction);
				openFile();
				break;
			}
			case OPEN_DATABASE : openDatabase(); break;
			case SAVE : save(); break;
			default : break;
		}
	}

	// Add CUI domain to existing Table
	public FileHandler(Table theTable, EnrichmentType theType)
	{
		if (theTable == null)
		{
			Log.logCommandLine(
				"FileHandler Constructor: parameter can not be 'null'.");
		}
		else
		{
			itsTable = new FileLoaderGeneRank(theTable, theType).getTable();
			printLoadingInfo();
		}
	}

	// Populate Table from XML file
	public FileHandler(File theFile, Table theTable)
	{
		if (theFile == null || !theFile.exists())
		{
			ErrorLog.log(theFile, new FileNotFoundException(""));
			return;
		}
		else if (theTable == null)
		{
			Log.logCommandLine(
				"FileHandler Constructor: Table is 'null', trying normal loading.");
			openFile();
		}
		else
		{
			itsFile = theFile;
			itsTable = theTable;
			openFile();
		}
	}

	private void openFile()
	{
		if (itsFile == null || !itsFile.exists())
		{
			ErrorLog.log(itsFile, new FileNotFoundException());
			return;
		}

		FileType aFileType = FileType.getFileType(itsFile);
		Timer aTimer = new Timer();

		switch (aFileType)
		{
			case TXT :
			{
				if (itsTable == null )
				{
					itsTable = new DataLoaderTXT(itsFile).getTable();
					//itsTable = new FileLoaderTXT(itsFile).getTable();
				}
				else
					itsTable = new DataLoaderTXT(itsFile).getTable();
					//new FileLoaderTXT(itsFile, itsTable);
				break;
			}
			case ARFF :
			{
				if (itsTable == null )
					itsTable = new FileLoaderARFF(itsFile).getTable();
				else
					new FileLoaderARFF(itsFile, itsTable);
				break;
			}
			case XML :
			{
				FileLoaderXML aLoader = new FileLoaderXML(itsFile);
				itsTable = aLoader.getTable();
				itsSearchParameters = aLoader.getSearchParameters();
				break;
			}
			// unknown FileType, log error
			default :
			{
				Log.logCommandLine(
					String.format("FileHandler: unknown FileType for File '%s'.",
							itsFile.getName()));
				return;
			}
		}
		Log.logCommandLine("Loading time: " + aTimer.getElapsedTimeString());
		printLoadingInfo();
	}

	private void openDatabase()
	{

	}

	private File save()
	{
		showFileChooser(Action.SAVE);
		return itsFile;
	}

	private void showFileChooser(Action theAction)
	{
		// dummy frame to pass ICON to showXXXXDialog
		// this class is no longer a JFrame (avoids HeadlessExceptions)
		JFrame aFrame = new JFrame();
		aFrame.setIconImage(MiningWindow.ICON);
		JFileChooser aChooser = new JFileChooser(new File(itsLastFileLocation));
		aChooser.addChoosableFileFilter(new FileTypeFilter(FileType.TXT));
		aChooser.addChoosableFileFilter(new FileTypeFilter(FileType.ARFF));
		aChooser.addChoosableFileFilter(new FileTypeFilter(FileType.XML));
		aChooser.setFileFilter (new FileTypeFilter(FileType.ALL_DATA_FILES));

		int theOption = -1;

		if (theAction == Action.OPEN_FILE)
			theOption = aChooser.showOpenDialog(aFrame);
		else if (theAction == Action.SAVE)
			theOption = aChooser.showSaveDialog(aFrame);

		if (theOption == JFileChooser.APPROVE_OPTION)
		{
			itsFile = aChooser.getSelectedFile();
			itsLastFileLocation = itsFile.getParent();
		}
	}

	public void printLoadingInfo()
	{
		itsTable.update();

		Log.logCommandLine(
			String.format(
					"Table '%s' has %d columns and %d rows.",
					itsTable.getName(),
					itsTable.getNrColumns(),
					itsTable.getNrRows()));
	}

	/**
	 * If a <code>JFileChooser</code> dialog was shown and a <code>File</code>
	 * was selected, use this method to retrieve it.
	 * 
	 * @return a <code>File</code>, or <code>null</code> if no approved
	 * selection was made.
	 */
	public File getFile() { return itsFile; };

	/**
	 * If this FileHandler successfully loaded a {@link Table Table} from a
	 * <code>File</code> or a database, use this method to retrieve it.
	 * 
	 * @return the <code>Table</code> if present, <code>null</code> otherwise.
	 */
	public Table getTable() { return itsTable; };

	/**
	 * If this FileHandler successfully loaded the
	 * {@link SearchParameters SearchParameters} from a <code>File</code>, use
	 * this method to retrieve them.
	 * 
	 * @return the <code>SearchParameters</code> if present, <code>null</code>
	 * otherwise.
	 */
	public SearchParameters getSearchParameters()
	{
		return itsSearchParameters;
	};
}
