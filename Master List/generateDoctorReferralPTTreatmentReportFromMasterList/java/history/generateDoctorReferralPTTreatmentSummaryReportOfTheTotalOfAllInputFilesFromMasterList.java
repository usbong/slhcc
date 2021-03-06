/*
 * Copyright 2018~2019 Usbong Social Systems, Inc.
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
 */
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/*
' Given:
' 1) Encoding for the Month Input Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated Medical Doctor Referral PT Treatment Report
' --> "Tab delimited" .txt file 
' --> Regardless of the name of the input file or input files, the output file will be "SummaryReportOutput.txt".
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateDoctorReferralPTTreatmentReportOfTheTotalOfAllInputFilesFromMasterList input201801.txt
' 
' where: "input201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'  java generateDoctorReferralPTTreatmentReportOfTheTotalOfAllInputFilesFromMasterList input*
'
' where: "input*" means any file in the directory that starts with "input".
*/ 

public class generateDoctorReferralPTTreatmentSummaryReportOfTheTotalOfAllInputFilesFromMasterList {	
	private static boolean inDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file
	
	private static String startDate = null;
	private static String endDate = null;
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_NOTES_COLUMN = 0;
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10;

	private static HashMap<String, double[]> referringDoctorContainer;	
	private static double[] columnValuesArray;
	
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 8; 
	
	private static final int OUTPUT_HMO_COUNT_COLUMN = 0; //transaction count
	private static final int OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 1;
	private static final int OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 2;
	private static final int OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 3;

	private static final int OUTPUT_NON_HMO_COUNT_COLUMN = 4; //transaction count
	private static final int OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 5;
	private static final int OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 6;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 7;
	
	private static DecimalFormat df = new DecimalFormat("0.00"); //added by Mike, 20181105
	private static int rowCount; //added by Mike, 20181105
				
	private static int totalCountForAllReferringDoctors;
	private static double totalNetTreatmentFeeForAllReferringDoctors;
	private static double totalPaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalUnpaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalFivePercentShareOfNetPaidForAllReferringDoctors;
				
	public static void main ( String[] args ) throws Exception
	{			
		makeFilePath("output"); //"output" is the folder where I've instructed the add-on software/application to store the output file			
		PrintWriter writer = new PrintWriter("output/SummaryReportOutput.txt", "UTF-8");			
		referringDoctorContainer = new HashMap<String, double[]>();

		//added by Mike, 20181116
		startDate = null; //properly set the month and year in the output file of each input file

		//edited by Mike, 20181030
		for (int i=0; i<args.length; i++) {
			//added by Mike, 20181030
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
			s=sc.nextLine(); //skip the first row, which is the input file's table headers
	
			if (inDebugMode) {
				rowCount=0;
			}
						
			//count/compute the number-based values of inputColumns 
			while (sc.hasNextLine()) {
				s=sc.nextLine();
				
				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				
				String[] inputColumns = s.split("\t");					

				//edited by Mike, 20181121
				if (startDate==null) {
					startDate = getMonthYear(inputColumns[INPUT_DATE_COLUMN]);
					endDate = startDate;
				}
				else {
					//edited by Mike, 20181121
					//add this condition in case the input file does not have a date for each transaction; however, ideally, for input files 2018 onwards, each transaction should have a date
					if (!inputColumns[INPUT_DATE_COLUMN].trim().equals("")) {
						endDate = getMonthYear(inputColumns[INPUT_DATE_COLUMN]);
					}
				}

				if (inDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
				
				//added by Mike, 20181121
				//skip transactions that have "RehabSupplies" as its "CLASS" value
				//In Excel logbook/workbook 2018 onwards, such transactions are not included in the Consultation and PT Treatment Excel logbooks/workbooks.
				if (inputColumns[INPUT_CLASS_COLUMN].contains("RehabSupplies")) {
					continue;
				}

				if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
					
					if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
						columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
						columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					else {
						columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
						columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}

					referringDoctorContainer.put(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN], columnValuesArray);
				}
				else {
					if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_COUNT_COLUMN]++;					
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					else {
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
				}
			}						
		}
		
		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------
		*/
		//added by Mike, 20181118
		writer.print("HMO and NON-HMO TOTAL Summary Report\n");
		 
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.print("UNPAID NET TREATMENT FEE:\t"); //"UNPAID NET TREATMENT FEE:" column		
		writer.println("5% SHARE OF NET PAID:"); //"5% SHARE OF NET PAID:" column		

		SortedSet<String> sortedKeyset = new TreeSet<String>(referringDoctorContainer.keySet());

		for (String key : sortedKeyset) {
			int totalCount = (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + (int) referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			double totalNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			double totalPaidNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			double totalUnpaidNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] +referringDoctorContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			double totalFivePercentShareOfNetPaid = referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05 + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05;

			//added by Mike, 20181123
			totalCountForAllReferringDoctors += totalCount;
			totalNetTreatmentFeeForAllReferringDoctors += totalNetTreatmentFee;
			totalPaidNetTreatmentFeeForAllReferringDoctors += totalPaidNetTreatmentFee;
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += totalUnpaidNetTreatmentFee;
			totalFivePercentShareOfNetPaidForAllReferringDoctors += totalFivePercentShareOfNetPaid;
			
			writer.println( startDate + " to " + endDate +
							"\t" + key +
							"\t" + totalCount +
							"\t" + df.format(totalNetTreatmentFee) +
							"\t" + df.format(totalPaidNetTreatmentFee) +
							"\t" + df.format(totalUnpaidNetTreatmentFee) +
							"\t" + df.format(totalFivePercentShareOfNetPaid)
							); 				   							
		}

		writer.println( startDate + " to " + endDate +
						"\t" + "All Referring Doctors" +
						"\t" + totalCountForAllReferringDoctors +
						"\t" + df.format(totalNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalPaidNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalUnpaidNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalFivePercentShareOfNetPaidForAllReferringDoctors)
						); 				   							
		
		//----------------------------------------------------------------------------------------------------------------------------		
		writer.print("\nHMO Report\n");
		 
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.print("UNPAID NET TREATMENT FEE:\t"); //"UNPAID NET TREATMENT FEE:" column		
		writer.println("5% SHARE OF NET PAID:"); //"5% SHARE OF NET PAID:" column		

//		SortedSet<String> sortedKeyset = new TreeSet<String>(referringDoctorContainer.keySet());
		
		//added by Mike, 20181123
		totalCountForAllReferringDoctors = 0;
		totalNetTreatmentFeeForAllReferringDoctors = 0;
		totalPaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalUnpaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalFivePercentShareOfNetPaidForAllReferringDoctors = 0;
		
		for (String key : sortedKeyset) {			
			//added by Mike, 20181123
			totalCountForAllReferringDoctors += (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
			totalNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			totalPaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			totalFivePercentShareOfNetPaidForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			
			writer.println( startDate + " to " + endDate +
							"\t" + key +
							"\t" + (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05)
							); 				   							
		}
		
		writer.println( startDate + " to " + endDate +
				"\t" + "All Referring Doctors" +
				"\t" + totalCountForAllReferringDoctors +
				"\t" + df.format(totalNetTreatmentFeeForAllReferringDoctors) +
				"\t" + df.format(totalPaidNetTreatmentFeeForAllReferringDoctors) +
				"\t" + df.format(totalUnpaidNetTreatmentFeeForAllReferringDoctors) +
				"\t" + df.format(totalFivePercentShareOfNetPaidForAllReferringDoctors*0.05) //edited by Mike, 20191210
				); 				   							

		
		//----------------------------------------------------------------------------------------------------------------------------
		writer.print("\nNON-HMO Report\n");
		
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.print("UNPAID NET TREATMENT FEE:\t"); //"UNPAID NET TREATMENT FEE:" column
		writer.println("5% SHARE OF NET PAID:"); //"5% SHARE OF NET PAID:" column		
								
		//added by Mike, 20181123
		totalCountForAllReferringDoctors = 0;
		totalNetTreatmentFeeForAllReferringDoctors = 0;
		totalPaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalUnpaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalFivePercentShareOfNetPaidForAllReferringDoctors = 0;

		for (String key : sortedKeyset) {
			//added by Mike, 20181123
			totalCountForAllReferringDoctors += (int) referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			totalNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			totalPaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			totalFivePercentShareOfNetPaidForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];

			writer.println( startDate + " to " + endDate +
							"\t" + key +
							"\t" + (int) referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN] +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05)
							); 				   							
		}
		
		writer.println( startDate + " to " + endDate +
		"\t" + "All Referring Doctors" +
		"\t" + totalCountForAllReferringDoctors +
		"\t" + df.format(totalNetTreatmentFeeForAllReferringDoctors) +
		"\t" + df.format(totalPaidNetTreatmentFeeForAllReferringDoctors) +
		"\t" + df.format(totalUnpaidNetTreatmentFeeForAllReferringDoctors) +
		"\t" + df.format(totalFivePercentShareOfNetPaidForAllReferringDoctors*0.05) //edited by Mike, 20191210
		); 				   							

		//----------------------------------------------------------------------------------------------------------------------------
		writer.close();
	}
	
	private static String getMonthYear(String date) {
		StringBuffer sb = new StringBuffer(date);				
		return sb.substring(0,3).concat("-").concat(sb.substring(sb.length()-2,sb.length()));
	}
	
	//added by Mike, 20181030
	private static void makeFilePath(String filePath) {
		File directory = new File(filePath);		
		if (!directory.exists() && !directory.mkdirs()) 
    	{
    		System.out.println("File Path to file could not be made.");
    	}    			
	}
}