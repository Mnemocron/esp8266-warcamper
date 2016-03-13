package warcamper;

import java.awt.List;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/*
*  @author Simon Burkhardt
*  @date 2016/03/13
*/


public class main {
	
	public static String scannedOn = "2016/03";
	public static String scanLog 	= "wifi.txt";
	public static String output_subfolder = "OUTPUT/";

	public static void main(String[] args) {
		
		
		
		
		File logFile = new File(scanLog);
		if (!logFile.exists() || logFile.isDirectory())
		{ 
		    System.err.println("could not find input file");
		}
		else 
		{
			System.out.println("fixing the rtc values ...");
			fixRtc(scanLog, "wifi_fix.txt", scannedOn, 5,56,6, 15,58,2);
			System.out.println("done");
			System.out.println();
			
			
			System.out.println("creating output folder ...");
			makeOutputFolder(output_subfolder);
			System.out.println("done");
			System.out.println();
			System.out.println("creating single output logs ...");
			createCsvLogs("wifi_fix.txt", scannedOn);
			System.out.println("done");
			System.out.println();
			
			System.out.println("creating a list of the found ssids ...");
			createSsidList(output_subfolder);
			System.out.println("done");
			System.out.println();
			
			System.out.println("generating security stats ...");
			createSecurityStats(output_subfolder, "wifi_fix.txt", scannedOn);
			System.out.println("done");
			System.out.println();
			
			
		}

	}
	
	public static void fixRtc(String fileIn, String fileOut, String dateOfScan, int hh1, int mm1, int ss1, int hh2, int mm2, int ss2){
		
		int time1[] = {hh1%24, mm1%60, ss1%60};
		int time2[] = {hh2%24, mm2%60, ss2%60};
		int timeTotal = 0;
		int currentTime[] = {0, 0, 0};
		double timeSec = 0.0;
		
		File logFile = new File(fileIn);
		
		// check if input file exists and is not a folder
		if(!logFile.exists() || logFile.isDirectory())
		{ 
		    System.err.println("could not find input file");
		} 
		else 
		{
			int totalScans = getNumberOfScans(fileIn, "2016/03");
			int totalScansFixed = 0;
			try
			{
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileOut, true));	// copied version with updated time
				BufferedReader reader = new BufferedReader(new FileReader(fileIn));			// original version
				String line = reader.readLine();				// assuming the first line contains a date
				String dateLine = line.substring(0, 11);		// assuming the date is formated yyyy:mm:dd or equal length
				
				// total time in seconds = total finished time - total started time
				timeTotal =  ((time2[0]*3600) + (time2[1]*60) + (time2[2])) - ((time1[0]*3600) + (time1[1]*60) + (time1[2]));
				timeSec = (double)timeTotal / totalScans;
				System.out.println("DEBUG : " + timeSec);
				
				// set the first currentTime to the starting value
				currentTime[2] = (time1[2]);
				currentTime[1] = (time1[1]);
				currentTime[0] = (time1[0]);
				// System.out.println(dateLine + currentTime[0] + ":" + currentTime[1] + ":" + currentTime[2]);
				writer.write(dateLine + String.format("%02d", currentTime[0])+ ":" 
						+ String.format("%02d", currentTime[1]) + ":" 
						+ String.format("%02d", currentTime[2]) + "\n");
				totalScansFixed ++;
				
				for(int i = 1; i < totalScans; i++){
					/*
					 * Ok, I kind of lost track about all the math that is happenig on the following 3 lines
					 * It is mostly rounding and not overflowing specific maximum values of time
					 * But it seems to work, so don't touch !!
					 */
					/*
					currentTime[2] = (time1[2] + ( (int)  Math.ceil((double) (timeSec)))) % 60;
					currentTime[1] = (time1[1] + ( (int)  Math.ceil((double) (timeSec / 60)))) % 60;
					currentTime[0] = (time1[0] + ( (int)  ((double)((timeSec + (time1[1] * 60)) / 3600)))) % 24;
					*/
					currentTime[2] = (time1[2] + (int)(Math.ceil(timeSec))) % 60;
					currentTime[1] = (time1[1] + (int)Math.ceil(((time1[2] + (int)Math.ceil(timeSec)) / 60))) % 60;
					currentTime[0] = (time1[0] + (int)Math.ceil(((time1[2] + (int)Math.ceil(timeSec) + (time1[1]*60)) / 3600))) % 24;
					line = reader.readLine();
					
					// find next date line, until then, copy every other line to the new file
					while(line != null)
					{
							if(line.length() > 0 && line.substring(0, dateOfScan.length()).equals(dateOfScan))
								break;
							else
								writer.write(line + "\n");
							line = reader.readLine();
					}
					
					// System.out.println(currentTime[0] + ":" + currentTime[1] + ":" + currentTime[2]);
					/*
					 *  Format !! add a zero before every number below 10
					 *  this would cause the String IndexOutOfBound Error in createCsvLogs()
					 *  because the string ends up too short
					 */
					writer.write(dateLine + String.format("%02d", currentTime[0])+ ":" 
							+ String.format("%02d", currentTime[1]) + ":" 
							+ String.format("%02d", currentTime[2]) + "\n");
					totalScansFixed ++;
					// don't even try to add brackets around the devision !
					timeSec += (double)timeTotal / totalScans;
				}
				// write out the last ssids of the file
				line = reader.readLine();	// skip this line with date and time
				while(line != null)
				{
					writer.write(line + "\n");
					line = reader.readLine();
				}

				reader.close();
				writer.close();
				
				System.out.println("total of " + totalScans + " scans");
				System.out.println("fixed    " + totalScansFixed + " scans");
				System.out.println("total time in s    \t" + timeTotal);
				System.out.println("scan interval in s \t" + ((double)(timeTotal/totalScans)));
				System.out.println("last time added :   " + String.format("%02d", currentTime[0])+ ":" 
						+ String.format("%02d", currentTime[1]) + ":" 
						+ String.format("%02d", currentTime[2]));
				System.out.println("entered last time : " + String.format("%02d",hh2) + ":"
						+ String.format("%02d",mm2) + ":"
						+ String.format("%02d",ss2));
				// System.out.println("in: " + time1[0] + ":" + time1[1] + ":" + time1[2]);
				/*
				 *  @todo add comparison of input values vs. output values.
				 *  @todo calculate error
				 */
			}
			catch(Exception es)
			{
				es.printStackTrace();
			}
			
		}
	}
	
	public static int getNumberOfScans(String file, String dateOfScan){
		/*
		 * this function just counts the occurrences of the date line
		 */
		int total = 0;
		try	{
			BufferedReader  reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			
			while(line != null)
			{
				if(line.length() > 0)
				{
					if(line.substring(0, dateOfScan.length()).compareTo(dateOfScan) == 0)
					{
						// System.out.println("found date");
						total ++;
					}
				}
				line = reader.readLine();
			}
			reader.close();
		}
		catch (Exception es)
	    {
	    	es.printStackTrace();
	    }
		return total;
	}
	
	public static void makeOutputFolder(String folder){
		// nothing fancy here, really...
		File dir = new File(folder);
		if(!dir.exists() || !dir.isDirectory())
		{
			try
			{
		        dir.mkdir();
		    } 
		    catch(SecurityException se)
			{
		        se.printStackTrace();
		    }        
		}
		
	}

	public static void createCsvLogs(String file, String dateOfScan){
		/*
		 * reading the logfile
		 * generating a filename from the ssid
		 * at every occurrence the ssid shows up, append the time and rssi
		 * in csv format to the specific file
		 */
		String line = "";
		String currentTime = "";
		String[] currentWifi = {};
		String newFilename = "";
		int currentRssi = 0;
		
		try	{
			// continuously read from the logfile
			BufferedReader  reader = new BufferedReader(new FileReader(file));
			line = reader.readLine();
			
			while(line != null)				// till end of file
			{
				if(line.length() > 0)		// skip empty lines
				{
					// find the date lines
					if(line.substring(0, dateOfScan.length()).compareTo(dateOfScan) == 0)
					{
						// System.out.println("found date string");
						// get the time out of the date line
						if(line.length() > 18)
							currentTime = line.substring(11, 19);
						else
						{
							currentTime = line.substring(11, line.length());
							System.err.println("String IndexOutOfRange Error\ntime format is propably wrong :\n" + currentTime);
						}
						// System.out.println("time : " + currentTime);
					}
					else
					{
						// System.out.println("found ssid");
						// get ssid, key type & rssi
						currentWifi = line.split(";");		// split like .csv
						currentRssi = Integer.parseInt(currentWifi[2]);
						/*
						System.out.println(currentWifi[0]);
						System.out.println(currentWifi[1]);
						System.out.println(currentRssi);
						*/
						
						// create a filename from the input data
						newFilename = output_subfolder + currentWifi[0].replaceAll("[<>?/\":*|]", "") + ".csv";
						File outjob = new File(newFilename);
						if(outjob.exists() && !outjob.isDirectory())
						{ 
						    // System.out.println("writing to file : " + outjob.getAbsolutePath());
						    try 
						    {
						    	// check if currentTime was already updated
							    String sLastLine = "", sCurrentLine = "";
							    BufferedReader reader2 = new BufferedReader(new FileReader(newFilename));
							    while ((sCurrentLine = reader2.readLine()) != null)
							    	sLastLine = sCurrentLine;
							    reader2.close();
							    sLastLine = sLastLine.substring(0, 10);
							    if(sLastLine.substring(0,8).compareTo(currentTime) == 0){
							    	// System.err.println("this file was already updated at this time");
							    } else {
								    BufferedWriter writer = new BufferedWriter(new FileWriter(newFilename, true));
								    writer.write(currentTime + ";" + currentRssi);
								    writer.newLine();
								    writer.close();
							    }
						    }
						    catch (Exception es)
						    {
						    	es.printStackTrace();
						    }
						}
						else		// file not found, create a new one
						{
							try {
						    	BufferedWriter writer = new BufferedWriter(new FileWriter(newFilename, true));
							    writer.write(currentTime + ";" + currentRssi);	// csv formattet	hh:mm:ss;rssi
							    writer.newLine();
							    writer.close();
						    } catch (Exception e) {
						    	e.printStackTrace();
						    }
						}
					}
				}
				line = reader.readLine();
			}
			reader.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

	public static void createSsidList(String dir){
		/*
		 * makes a list of all the previously generated .csv files
		 */
		try
		{
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();

			BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "SSID.txt", true));
		    
			for (int i = 0; i < listOfFiles.length; i++)
			{
				if (listOfFiles[i].isFile())
				{
					String ssid = listOfFiles[i].getName();
					ssid = ssid.substring(0, ssid.lastIndexOf("."));
					// System.out.println("File " + ssid);
					writer.write(ssid);
					writer.newLine();
				}
			}
			writer.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	public static void createSecurityStats(String dir, String file, String dateOfScan){
		/*
		 * takes the ssid list and goes through every ssid in the original log file
		 * finds the encryption type and makes some stats about the security
		 */
		String lineEnc = "";
		int linecount = 0;
		
		try
		{
			int encNone = 0;
			int encWep = 0;
			int encPsk = 0;
			int encPsk2 = 0;
			int encAuto = 0;
			// String lineEnc = "";
			BufferedReader reader = new BufferedReader(new FileReader(dir + "SSID.txt"));
			String line = reader.readLine();
			
			System.out.println("--- security ---");
			linecount = 0;
			while(line != null)
			{
				linecount ++;
				// reopen this file for each ssid
				BufferedReader reader2 = new BufferedReader(new FileReader(file));
				
				// System.out.println(line);
				
				lineEnc = reader2.readLine();
				if(lineEnc.substring(0, 8).compareTo(dateOfScan) == 0)
					lineEnc = reader.readLine();
				
				String[] netInfo = lineEnc.split(";");
				
				while(netInfo[0].compareTo(line) != 0)
				{
					lineEnc = reader2.readLine();
					if(lineEnc != null) netInfo = lineEnc.split(";");
					else break;
				}
				if(lineEnc != null)
				{
					// System.out.println(netInfo[1]);
					if(netInfo[1].compareTo("none") == 0)		encNone ++;
					if(netInfo[1].compareTo("WEP") == 0)		encWep ++;
					if(netInfo[1].compareTo("AUTO") == 0)		encAuto ++;
					if(netInfo[1].compareTo("WPA_PSK") == 0)	encPsk ++;
					if(netInfo[1].compareTo("WPA_PSK2") == 0)	encPsk2 ++;
				}
				
				line = reader.readLine();
				reader2.close();
			}
			reader.close();
			
			double totNone = (double)(100 * encNone)/linecount;
			double totWep  = (double)(100 * encWep )/linecount;
			double totPsk  = (double)(100 * encPsk )/linecount;
			double totPsk2 = (double)(100 * encPsk2)/linecount;
			double totAuto = (double)(100 * encAuto)/linecount;
			double totalSecured = 100 - totNone;
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(output_subfolder + "SECURITY.txt", true));
			
			writer.write("networks total \t" + linecount + "\n\n");
			writer.write("secured with :\n");
			writer.write("none \t\t\t" + encNone + "\n");
			writer.write("WEP \t\t\t" + encWep + "\n");
			writer.write("WPA PSK \t\t" + encPsk + "\n");
			writer.write("WPA PSK2 \t\t" + encPsk2 + "\n");
			writer.write("auto \t\t\t" + encAuto + "\n");
			writer.newLine();
			writer.write("statistics :\n");
			writer.write("total secured :\t" + totalSecured + "\t%" + "\n");
			writer.write("unsecured \t\t" + totNone + " \t%" + "\n");
			writer.write("WEP \t\t\t" + totWep + " \t%" + "\n");
			writer.write("WPA PSK \t\t" + totPsk + " \t%" + "\n");
			writer.write("WPA PSK2 \t\t" + totPsk2 + " \t%" + "\n");
			writer.write("Auto \t\t\t" + totAuto + " \t%" + "\n");
			
			writer.close();
			System.out.println("total \t\t\t" + linecount);
			System.out.println("none \t\t\t" + encNone);
			System.out.println("WEP \t\t\t" + encWep);
			System.out.println("WPA PSK \t\t" + encPsk);
			System.out.println("WPA PSK2 \t\t" + encPsk2);
			System.out.println("auto \t\t\t" + encAuto);
			
			System.out.println("\ntotal secured : \t" + totalSecured + "%\n");
			System.out.println("none \t\t\t" + totNone+ "%");
			System.out.println("WEP \t\t\t" + totWep + "%");
			System.out.println("WPA PSK \t\t" + totPsk + "%");
			System.out.println("WPA PSK2 \t\t" + totPsk2 + "%");
			System.out.println("Auto \t\t\t" + totAuto + "%");
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println(lineEnc);
		}
	}
	
}
