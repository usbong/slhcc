/*
 * Copyright 2018~2020 Usbong Social Systems, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @company: USBONG SOCIAL SYSTEMS, INC. (USBONG)
 * @author: SYSON, MICHAEL B.
 * @date created: 2018
 * @last updated: 20201030
 *
 */
 
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParsePosition;

//import java.lang.Integer;

/*
' Given:
' 1) Encoding for the Month Input Worksheet from the Master List
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' 2) Encoding of the list of paid HMO Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: paidHMOList201811.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-verified with notes of variation/deviation between the paid HMO list worksheet and the monthly input worksheet from the Master List 
' --> "Tab delimited" .txt file 
' --> The name of the output file is the same as that of the input file.
' --> The output files are stored inside an auto-created "output" folder in the directory where the add-on software/application is located.
'
' Notes:
' 1) To execute a set of input files, e.g. input201801.txt, input201802.txt, paidHMOList201811.txt, you can use the following command: 
'   java generateAnnualYearEndSummaryReportOfAllInputFilesFromMasterList *.txt
' 
' where: * means any set of characters
'
' 2) Make sure to include the paid HMO List whose file name contains "paidHMOList".
' --> Otherwise, the add-on software/application would not have a paid HMO List to verify with the Master List's monthly input worksheets.
'
*/ 

public class autoVerifyPaidHMOListwithMasterList {	
	private static boolean isInDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file
	private static String inputHmoListFilename = "paidHmoList201811"; //without extension; default input file
	
	private static String startDate = null;
	private static String endDate = null;

	//output columns are the same with the input columns due to the output file using the same structure as the input file
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_NOTES_COLUMN = 0;
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_NAME_COLUMN = 3;
	private static final int INPUT_FEE_COLUMN = 7;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10;

	//TO-DO: -update: set of instructions
	//note: There are variations in the structure/format of newly received paid HMO lists
/*	//edited by Mike, 20201029	
	private static final int INPUT_HMO_LIST_NOTES_COLUMN = 0;
	private static final int INPUT_HMO_LIST_DATE_COLUMN = 1;
	private static final int INPUT_HMO_LIST_CLASS_COLUMN = 2;
	private static final int INPUT_HMO_LIST_NAME_COLUMN = 3;
	private static final int INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN = 7;
	private static final int INPUT_HMO_LIST_NET_PF_COLUMN = 20;
*/
	private static final int INPUT_HMO_LIST_NOTES_COLUMN = 0;
	private static final int INPUT_HMO_LIST_DATE_COLUMN = 1;
	private static final int INPUT_HMO_LIST_CLASS_COLUMN = 2;
	private static final int INPUT_HMO_LIST_NAME_COLUMN = 3;
	private static final int INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN = 7;
	private static final int INPUT_HMO_LIST_NET_PF_COLUMN = 20;

	private static final int INPUT_CONSULTATION_OFFSET = 1;
		
	private static double[] columnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206
	//private static ArrayList<int> dateValuesArrayInt; //edited by Mike, 20181221
		
	private static boolean isConsultation;
	
	private static DecimalFormat df = new DecimalFormat("0.00"); //added by Mike, 20181105
	private static int rowCount; //added by Mike, 20181105
	private static int hmoRowCount; //added by Mike, 20181230
						
	public static void main ( String[] args ) throws Exception
	{			
		makeFilePath("output"); //"output" is the folder where I've instructed the add-on software/application to store the output file			
//		PrintWriter writer = new PrintWriter("output/AnnualYearEndSummaryReportOutput.txt", "UTF-8");			
						
		//added by Mike, 20181116
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[args.length]; //added by Mike, 20180412
		dateValuesArrayInt = new int[args.length]; //added by Mike, 20180412

		//PART/COMPONENT/MODULE/PHASE 1
		processInputFiles(args, true);				
		
		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------
		*/
//		writer.print("Annual Year End Summary Report\n");				
//		writer.close();
	}
	
	private static String convertDateToMonthYearInWords(int date) {
		StringBuffer sb = new StringBuffer(""+date);	
		String year = sb.substring(0,4); //index 4 is not included
		int month = Integer.parseInt(sb.substring(4,6)); //index 6 is not included
		
		switch(month) {
			case 1:
				return "January" + " " + year;
			case 2:
				return "February" + " " + year;
			case 3:
				return "March" + " " + year;
			case 4:
				return "April" + " " + year;
			case 5:
				return "May" + " " + year;
			case 6:
				return "June" + " " + year;
			case 7:
				return "July" + " " + year;
			case 8:
				return "August" + " " + year;
			case 9:
				return "September" + " " + year;
			case 10:
				return "October" + " " + year;
			case 11:
				return "November" + " " + year;
			case 12:
				return "December" + " " + year;
		}	

		return null;//error
	}
	
	private static String getMonthYear(String date) {
		StringBuffer sb = new StringBuffer(date);				
		return sb.substring(0,3).concat("-").concat(sb.substring(sb.length()-2,sb.length()));
	}

	//added by Mike, 20201030	
	//input: 11/04/2019
	//input: Nov-04-19
	private static String convertDateToMonthWordDayYear(String date) {
		StringBuffer sb = new StringBuffer(date);	
		
		String year = sb.substring(6,10); //index 10 is not included
		String day = sb.substring(3,5); //index 5 is not included
		int month = Integer.parseInt(sb.substring(0,2)); //index 2 is not included
		
		switch(month) {
			case 1:
				return "Jan" + "-" + day + "-" + year;
			case 2:
				return "Feb" + "-" + day + "-" + year;
			case 3:
				return "Mar" + "-" + day + "-" + year;
			case 4:
				return "Apr" + "-" + day + "-" + year;
			case 5:
				return "May" + "-" + day + "-" + year;
			case 6:
				return "Jun" + "-" + day + "-" + year;
			case 7:
				return "Jul" + "-" + day + "-" + year;
			case 8:
				return "Aug" + "-" + day + "-" + year;
			case 9:
				return "Sep" + "-" + day + "-" + year;
			case 10:
				return "Oct" + "-" + day + "-" + year;
			case 11:
				return "Nov" + "-" + day + "-" + year;
			case 12:
				return "Dec" + "-" + day + "-" + year;
		}	

		return null;//error
	}

	//added by Mike, 20181227; edited by Mike, 20201029
	//input: Nov-04-19
	//output: 30-Nov-19
	private static String formatDateToMatchWithHmoListDateFormat(String date) {
		StringBuffer sb = new StringBuffer(date);				

		//added by Mike, 20201030
		//identify if date format is MM/DD/YYYY
		//example: 11/04/2019
		//output: Nov-04-19
		//TO-DO: -update: this
		//verify if slash exists
		if (date.contains("/")) {
			date = convertDateToMonthWordDayYear(date);			
			sb = new StringBuffer(date);
		}

//		StringBuffer sb = new StringBuffer(date);				
		return getDay(date).concat("-").concat(sb.substring(0,3)).concat("-").concat(sb.substring(sb.length()-2,sb.length()));
	}

	//added by Mike, 20181227
	private static String getDay(String date) {
		StringBuffer sb = new StringBuffer(date);				
		//We do a +1 and -1, because we are not including here the dash/hyphen, i.e. "-".
		//We still need to replace any "-" that is left since the day value in both the Master List worksheet and the paid HMO list can either have 1 or 2 digits.
		return sb.substring(sb.indexOf("-")+1).substring(0, sb.indexOf("-")-1).replace("-",""); 
	}
	
	//added by Mike, 20181030
	private static void makeFilePath(String filePath) {
		File directory = new File(filePath);		
		if (!directory.exists() && !directory.mkdirs()) 
    	{
    		System.out.println("File Path to file could not be made.");
    	}    			
	}
	
	private static void processInputFiles(String[] args, boolean isPhaseOne) throws Exception {
		File hmoListFile = null;
		
		//added by Mike, 20201030
		String sFileExtension = ".txt";
		
		for (int i=0; i<args.length; i++) {							
			if (args[i].toLowerCase().contains("hmo")) {
				//added by Mike, 20201030
				if (args[i].contains(".txt")) {
					inputHmoListFilename = args[i].replaceAll(".txt","");
					hmoListFile = new File(inputHmoListFilename+".txt");
				}
				else if (args[i].contains(".csv")) {
					inputHmoListFilename = args[i].replaceAll(".csv","");
					hmoListFile = new File(inputHmoListFilename+".csv");					
					sFileExtension = ".csv";
				}
				else {
					System.out.println("Did not find input files in .csv or .txt file formats");
					return;
				}
			}
		}
		
		if (hmoListFile==null) {
			System.out.println("Error: unable to locate the paid HMO List file whose file name should have the keyword \"hmo\".");
			return;
		}
		
		PrintWriter writer = null;			
		PrintWriter hmoListWriter = null;

		for (int i=0; i<args.length; i++) {						
			if (args[i].toLowerCase().contains("hmo")) {
				continue;
			}
		
			//edited by Mike, 20201030
/*			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");
*/
			inputFilename = args[i].replaceAll(sFileExtension,"");			
			File f = new File(inputFilename+sFileExtension);
			
/*			writer = new PrintWriter("output/"+inputFilename+".txt", "UTF-8");			
*/

/*			//edited by Mike, 20201030
			hmoListWriter = new PrintWriter("output/"+inputHmoListFilename+".txt", "UTF-8");
*/
			hmoListWriter = new PrintWriter("output/"+inputHmoListFilename+sFileExtension, "UTF-8");
						
			System.out.println("inputFilename: " + inputFilename);
			
			if (inputFilename.toLowerCase().contains("consultation")) {
				isConsultation=true;
			}
			else {
				isConsultation=false;
			}
			
/*			Scanner sc = new Scanner(new FileInputStream(f));				
*/
			Scanner hmoListScanner = new Scanner(new FileInputStream(hmoListFile));				
/*		
			String s;		
			s=sc.nextLine(); //skip the first row, which is the input file's table headers
*/
			String hmoListString;		

			//removed by Mike, 20201030
			//hmoListString=hmoListScanner.nextLine(); //skip the first row, which is the input file's table headers

			rowCount=0;
			hmoRowCount=0;

			//added by Mike, 20201030
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			
			//count/compute the number-based values of inputColumns 
			while (hmoListScanner.hasNextLine()) {
				hmoListString=hmoListScanner.nextLine();

				//added by Mike, 20201030
				//identify if table header row
				if (hmoListString.contains("DATE")){
					//skip the first row, which is the input file's table headers
					hmoListString=hmoListScanner.nextLine();
				}

//				System.out.println("hmoListString: "+hmoListString);
				
				//if the row is blank
				if (hmoListString.trim().equals("")) {
					continue;
				}
				
				String[] inputHmoListColumns = hmoListString.split("\t");					

//				System.out.println("inputHmoListColumns[INPUT_HMO_LIST_DATE_COLUMN]: "+inputHmoListColumns[INPUT_HMO_LIST_DATE_COLUMN]);
								
				//if the value for the date column is blank
				if (inputHmoListColumns[INPUT_HMO_LIST_DATE_COLUMN].equals("")) {
					continue;
				}

				//added by Mike, 20181230; edited by Mike, 20201030
				//writer = new PrintWriter("output/"+inputFilename+".txt", "UTF-8");			
				writer = new PrintWriter("output/"+inputFilename+sFileExtension, "UTF-8");			
				
				hmoRowCount++;

				Scanner sc = new Scanner(new FileInputStream(f));				
			
				String s;		
				
				//removed by Mike, 20201030
//				s =sc.nextLine(); 
				
				while (sc.hasNextLine()) {
					s=sc.nextLine();

					//identify if table header row
					//skip the first row, which is the input file's table headers
					if (s.contains("DATE")){
						System.out.println(">>"+s.toString());

						s=sc.nextLine(); //skip the first row, which is the input file's table headers
					}				
					
					//System.out.println("hmoListString: "+hmoListString);
			
					//if the row is blank
					if (s.trim().equals("")) {
						continue;
					}
					
					String[] inputColumns = s.split("\t");					

//					System.out.println(">>>"+s);
					
					rowCount++;
					
					if (isInDebugMode) {
						System.out.println("rowCount: "+rowCount);
					}
					
					//added by Mike, 20181121
					//skip transactions that have "RehabSupplies" as its "CLASS" value
					//In Excel logbook/workbook 2018 onwards, such transactions are not included in the Consultation and PT Treatment Excel logbooks/workbooks.
					if (inputColumns[INPUT_CLASS_COLUMN].contains("RehabSupplies")) {
						continue;
					}
					
					//TO-DO: -verify: date format of input master list file
					System.out.println("inputColumns[INPUT_DATE_COLUMN]: "+ inputColumns[INPUT_DATE_COLUMN]);
					
					System.out.println("inputColumns[INPUT_DATE_COLUMN]: "+formatDateToMatchWithHmoListDateFormat(inputColumns[INPUT_DATE_COLUMN]));
					System.out.println("inputHmoListColumns[INPUT_HMO_LIST_DATE_COLUMN]: "+inputHmoListColumns[INPUT_HMO_LIST_DATE_COLUMN]);
					
					//added by Mike, 20201030
					//NumberFormat format = NumberFormat.getInstance(Locale.US);
					Number nInputHMOListBilledAmount = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN]));
					double dInputHMOListBilledAmount = nInputHMOListBilledAmount.doubleValue();

					Number nInputMasterListBilledAmount = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputColumns[INPUT_FEE_COLUMN]));
					double dInputMasterListBilledAmount = nInputMasterListBilledAmount.doubleValue();

					Number nInputHMOListNetPf = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputHmoListColumns[INPUT_HMO_LIST_NET_PF_COLUMN]));
					double dInputHMOListNetPf = nInputHMOListNetPf.doubleValue();

					Number nInputMasterListNetPf = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputColumns[INPUT_NET_PF_COLUMN]));
					double dInputMasterListNetPf = nInputMasterListNetPf.doubleValue();

					
					if (inputHmoListColumns[INPUT_HMO_LIST_DATE_COLUMN].equals(formatDateToMatchWithHmoListDateFormat(inputColumns[INPUT_DATE_COLUMN]))) {

						System.out.println(
						"inputColumns[INPUT_NAME_COLUMN].toLowerCase(): "+inputColumns[INPUT_NAME_COLUMN].toLowerCase()+"\t"+
						"inputHmoListColumns[INPUT_HMO_LIST_NAME_COLUMN].toLowerCase(): "+inputHmoListColumns[INPUT_HMO_LIST_NAME_COLUMN].toLowerCase());
					
						if (inputHmoListColumns[INPUT_HMO_LIST_NAME_COLUMN].toLowerCase().equals(inputColumns[INPUT_NAME_COLUMN].toLowerCase()))
						{							
							//edited by Mike, 20201030
//							if (!isNumeric(inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN])) {
							if (!isNumeric(""+dInputHMOListBilledAmount)) {
							if (isInDebugMode) {
									System.out.println("NOT NUMERIC");
									System.out.println("inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN]): "+dInputHMOListBilledAmount);
								}
								continue;
							}
							else {
								if (isInDebugMode) {
									System.out.println(">>>>>>>> IS NUMERIC");
								}
							}													
/*
							System.out.println("inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN].toLowerCase(): "+inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN].toLowerCase());						
							System.out.println("inputColumns[INPUT_FEE_COLUMN]: "+inputColumns[INPUT_FEE_COLUMN]);						
*/
							System.out.println("inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN].toLowerCase(): "+dInputHMOListBilledAmount);						
							System.out.println("inputColumns[INPUT_FEE_COLUMN]: "+dInputMasterListBilledAmount);

/*							
							//added by Mike, 20201030; removed by Mike, 20201030							
							//NumberFormat format = NumberFormat.getInstance(Locale.US);
							Number nInputHMOListBilledAmount = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN]));
							double dInputHMOListBilledAmount = nInputHMOListBilledAmount.doubleValue();

							Number nInputMasterListBilledAmount = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputColumns[INPUT_FEE_COLUMN]));
							double dInputMasterListBilledAmount = nInputMasterListBilledAmount.doubleValue();

							Number nInputHMOListNetPf = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputHmoListColumns[INPUT_HMO_LIST_NET_PF_COLUMN]));
							double dInputHMOListNetPf = nInputHMOListNetPf.doubleValue();

							Number nInputMasterListNetPf = format.parse(UsbongUtilsStringConvertToParseableNumberString(inputColumns[INPUT_FEE_COLUMN]));
							double dInputMasterListNetPf = nInputMasterListNetPf.doubleValue();
*/
	
/*							if (Double.parseDouble(inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN]) == Double.parseDouble(inputColumns[INPUT_FEE_COLUMN])) {
*/								
							if (dInputHMOListBilledAmount == dInputMasterListBilledAmount) {
/*
								System.out.println(">> inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN].toLowerCase(): "+inputHmoListColumns[INPUT_HMO_LIST_BILLED_AMOUNT_COLUMN].toLowerCase());						
*/
/*
								if (Double.parseDouble(inputHmoListColumns[INPUT_HMO_LIST_NET_PF_COLUMN]) == Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN])) {															
*/
								//TO-DO: -reverify: NET PF computation due to VAT, etc

								System.out.println(">> dInputHMOListNetPf : "+dInputHMOListNetPf);		
								System.out.println(">> dInputMasterListNetPf : "+dInputMasterListNetPf);		

								if (dInputHMOListNetPf == dInputMasterListNetPf) {
/*
									System.out.println(">> inputHmoListColumns[INPUT_HMO_LIST_NET_PF_COLUMN].toLowerCase(): "+inputHmoListColumns[INPUT_HMO_LIST_NET_PF_COLUMN].toLowerCase());		
*/									

									//TO-DO: -verify: these
									System.out.println(">> inputHmoListColumns[INPUT_HMO_LIST_CLASS_COLUMN].toLowerCase(): "+inputHmoListColumns[INPUT_HMO_LIST_CLASS_COLUMN].toLowerCase());						
									System.out.println(">> inputColumns[INPUT_CLASS_COLUMN].toLowerCase(): "+inputColumns[INPUT_CLASS_COLUMN].toLowerCase());	

									if (inputHmoListColumns[INPUT_HMO_LIST_CLASS_COLUMN].toLowerCase().trim().equals(inputColumns[INPUT_CLASS_COLUMN].toLowerCase().replace("hmo/","").trim())) {								
										System.out.println(">> inputHmoListColumns[INPUT_HMO_LIST_CLASS_COLUMN].toLowerCase(): "+inputHmoListColumns[INPUT_HMO_LIST_CLASS_COLUMN].toLowerCase());			

										//System.out.println(""+inputHmoListFilename);
										//StringBuffer hmoListStringBuffer = new StringBuffer(hmoListString);
										s = inputHmoListFilename + "\t" + s;
										
										System.out.println(">>>>> s: "+s);			

										//hmoListWriter = new PrintWriter("output/"+inputHmoListFilename
										writer.println(s);					
										break;
									}
								}
							}
						}
//						Patient Name, Fee, Net PF, HMO
						
						System.out.println(">>"+formatDateToMatchWithHmoListDateFormat(inputColumns[INPUT_DATE_COLUMN]));
					}
					
					writer.println(s);					
				}				
				writer.close();									
				
				hmoListWriter.println(hmoListString);					
			}			
		}
		
		hmoListWriter.close();					
	}	
	
	//Reference: https://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java;
	//last accessed: 20181229
	//answer by: CraigTP; edited by: MHosafy
	public static boolean isNumeric(String str)
	{
	  NumberFormat formatter = NumberFormat.getInstance();
	  ParsePosition pos = new ParsePosition(0);
	  formatter.parse(str, pos);
	  return str.length() == pos.getIndex();
	}
	
	//added by Mike, 20201030
	//fix: unparseable number, e.g. "" 68.58 ""
	//input: "" 68.58 ""
	//output (String type): 68.58
	private static String UsbongUtilsStringConvertToParseableNumberString(String input) {
		return input.replace("\"","").replace(" ","");
	}
}
