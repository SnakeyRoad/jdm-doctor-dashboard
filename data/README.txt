JDM Doctor Dashboard Data Files
==============================

Required CSV Files
-----------------
Place the following CSV files in this directory:
- CMAS.csv
- Patient.csv
- LabResultGroup.csv
- LabResult.csv
- LabResultsEN.csv
- Measurement.csv

These files contain the patient data that will be loaded into the SQLite database
when the application first runs.

Data Cleaning Process
--------------------
The original CSV files were pre-cleaned using the clean_csv_files.py script 
located in the root project directory. This script handles:

1. DateTime format standardization - Ensures consistent DD-MM-YYYY HH:MM format
2. Value format normalization - Handles international number formats (comma vs. decimal point)
3. Empty rows and whitespace removal - Cleans up inconsistencies in the data
4. Special character handling - Prevents encoding issues in the database
5. CMAS data restructuring - Converts from wide to long format for better analysis

The script creates a 'cleaned_csv' directory with the prepared files that are
ready for import into the SQLite database.

Using the Cleaning Script
------------------------
If you have raw CSV data files that need to be processed:

1. Place the original CSV files in the project root directory
2. Run the cleaning script:
   $ python clean_csv_files.py
3. Copy the cleaned files from the 'cleaned_csv' directory to this data directory:
   $ cp ../cleaned_csv/* ./

Data Structure
-------------
- Patient.csv: Contains patient identification information
- CMAS.csv: Childhood Myositis Assessment Scale measurements over time
- LabResultGroup.csv: Groups for categorizing lab results
- LabResult.csv: Definitions of various lab result types
- LabResultsEN.csv: English translations for lab result names
- Measurement.csv: Actual measurement values for lab results

Note: The application expects properly formatted CSV files. If you're using 
the original uncleaned data files, please run them through the cleaning script 
first to ensure proper data loading.
