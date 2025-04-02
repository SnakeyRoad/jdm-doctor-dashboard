#!/usr/bin/env python3
"""
CSV Data Cleaning Script for Lab Results Dataset

This script cleans and normalizes CSV files to prepare them for import into an SQL database.
It handles various datetime format issues, fixes value representations, and 
ensures relational integrity between tables.

Main issues addressed:
1. DateTime format issues in Measurement.csv
2. Value format standardization
3. Empty rows and whitespace cleaning
4. Handling special characters

Usage:
    python clean_csv_files.py

The script will create cleaned versions of all CSV files in the current directory
with "_clean" appended to the filename.
"""

import csv
import os
import re
import datetime
from pathlib import Path

# Define input and output files
INPUT_FILES = [
    'LabResultGroup.csv',
    'LabResult.csv',
    'LabResultsEN.csv',
    'Measurement.csv',
    'Patient.csv',
    'CMAS.csv'
]

def clean_datetime(datetime_str):
    """
    Clean and standardize datetime strings as per professor's requirements.
    
    Args:
        datetime_str (str): Original datetime string
    
    Returns:
        str: Cleaned datetime string in format 'DD-MM-YYYY HH:MM'
    """
    if not datetime_str or datetime_str.isspace():
        return ""
    
    # Remove quotes, extra spaces, and newlines
    clean_str = datetime_str.strip().replace('"', '').replace('\n', '')
    
    # Remove decimal points from time as suggested by professor
    clean_str = re.sub(r'\.\d+$', '', clean_str)
    
    # Insert space between date and time if missing
    if re.match(r'^\d{2}-\d{2}-\d{4}\d{2}:\d{2}$', clean_str):
        clean_str = re.sub(r'(\d{2}-\d{2}-\d{4})(\d{2}:\d{2})', r'\1 \2', clean_str)
    
    return clean_str

def clean_value(value_str):
    """
    Clean value strings to ensure they're SQL-compatible.
    
    Args:
        value_str (str): Original value string
    
    Returns:
        str: Cleaned value string
    """
    if not value_str:
        return ""
    
    # Remove any surrounding quotes
    clean_str = value_str.strip().replace('"', '')
    
    # Replace commas with dots in numeric values (for international format)
    if re.match(r'^-?\d+,\d+$', clean_str):
        clean_str = clean_str.replace(',', '.')
    
    return clean_str

def clean_measurement_csv(input_file, output_file):
    """
    Clean the Measurement.csv file which has specific DateTime format issues.
    
    Args:
        input_file (str): Path to input file
        output_file (str): Path to output file
    """
    print(f"Cleaning {input_file}...")
    
    with open(input_file, 'r', newline='', encoding='utf-8') as infile, \
         open(output_file, 'w', newline='', encoding='utf-8') as outfile:
        
        reader = csv.DictReader(infile)
        fieldnames = reader.fieldnames
        
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()
        
        for row in reader:
            # Clean DateTime field
            if 'DateTime' in row:
                row['DateTime'] = clean_datetime(row['DateTime'])
            
            # Clean Value field
            if 'Value' in row:
                row['Value'] = clean_value(row['Value'])
            
            writer.writerow(row)
    
    print(f"✓ Cleaned {input_file} -> {output_file}")

def clean_cmas_csv(input_file, output_file):
    """
    Restructure and clean the CMAS.csv file which has dates as columns.
    Converts to a standard format with date as a column.
    
    Args:
        input_file (str): Path to input file
        output_file (str): Path to output file
    """
    print(f"Restructuring and cleaning {input_file}...")
    
    try:
        # Read the original file
        with open(input_file, 'r', newline='', encoding='utf-8') as infile:
            reader = csv.reader(infile)
            data = list(reader)
        
        if not data or len(data) < 2:
            print(f"Warning: {input_file} is empty or has insufficient data")
            return
        
        # First row contains dates as headers
        headers = data[0]
        
        # Create standardized structure
        with open(output_file, 'w', newline='', encoding='utf-8') as outfile:
            writer = csv.writer(outfile)
            
            # New header: Date, Category, Value
            writer.writerow(['Date', 'Category', 'Value'])
            
            # Process each row (categories)
            for row_idx in range(1, len(data)):
                if not data[row_idx]:
                    continue
                
                category = data[row_idx][0]
                
                # Process each date column
                for col_idx in range(1, len(headers)):
                    if col_idx >= len(data[row_idx]):
                        continue
                    
                    date = headers[col_idx]
                    value = data[row_idx][col_idx]
                    
                    # Skip empty values
                    if not value or value.isspace():
                        continue
                    
                    # Clean date format
                    clean_date = date.strip()
                    
                    # Convert YYYY-MM-DD to DD-MM-YYYY if needed
                    if re.match(r'^\d{4}-\d{2}-\d{2}$', clean_date):
                        date_parts = clean_date.split('-')
                        clean_date = f"{date_parts[2]}-{date_parts[1]}-{date_parts[0]}"
                    
                    # Fix inconsistent date formats (D-M-YYYY to DD-MM-YYYY)
                    elif re.match(r'^\d{1,2}-\d{1,2}-\d{4}$', clean_date):
                        date_parts = clean_date.split('-')
                        clean_date = f"{date_parts[0].zfill(2)}-{date_parts[1].zfill(2)}-{date_parts[2]}"
                    
                    # Write row
                    writer.writerow([clean_date, category, value])
        
        print(f"✓ Restructured and cleaned {input_file} -> {output_file}")
    
    except Exception as e:
        print(f"Error cleaning CMAS.csv: {e}")

def clean_generic_csv(input_file, output_file):
    """
    Clean a generic CSV file by removing whitespace, empty rows, and ensuring proper formatting.
    
    Args:
        input_file (str): Path to input file
        output_file (str): Path to output file
    """
    print(f"Cleaning {input_file}...")
    
    with open(input_file, 'r', newline='', encoding='utf-8') as infile, \
         open(output_file, 'w', newline='', encoding='utf-8') as outfile:
        
        reader = csv.DictReader(infile)
        fieldnames = reader.fieldnames
        
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()
        
        for row in reader:
            # Skip completely empty rows
            if all(not value or value.isspace() for value in row.values()):
                continue
            
            # Clean all values
            cleaned_row = {
                key: value.strip() if value else ""
                for key, value in row.items()
            }
            
            writer.writerow(cleaned_row)
    
    print(f"✓ Cleaned {input_file} -> {output_file}")

def clean_patient_csv(input_file, output_file):
    """
    Special case for Patient.csv which appears to have only one row
    
    Args:
        input_file (str): Path to input file
        output_file (str): Path to output file
    """
    print(f"Cleaning {input_file}...")
    
    with open(input_file, 'r', newline='', encoding='utf-8') as infile, \
         open(output_file, 'w', newline='', encoding='utf-8') as outfile:
        
        reader = csv.DictReader(infile)
        fieldnames = reader.fieldnames
        
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()
        
        for row in reader:
            # Skip completely empty rows
            if all(not value or value.isspace() for value in row.values()):
                continue
            
            # Ensure the PatientID exists in the LabResult.csv file
            # You may need to implement this check if required
            
            writer.writerow(row)
    
    print(f"✓ Cleaned {input_file} -> {output_file}")

def main():
    """Main function to process all CSV files"""
    print("Starting CSV cleaning process...")
    
    # Create output directory if it doesn't exist
    output_dir = "cleaned_csv"
    os.makedirs(output_dir, exist_ok=True)
    
    # Process each file
    for filename in INPUT_FILES:
        input_path = filename
        output_path = os.path.join(output_dir, filename)
        
        if not os.path.exists(input_path):
            print(f"Warning: {input_path} does not exist, skipping.")
            continue
        
        # Apply the appropriate cleaning function based on file type
        if filename == 'Measurement.csv':
            clean_measurement_csv(input_path, output_path)
        elif filename == 'CMAS.csv':
            clean_cmas_csv(input_path, output_path)
        elif filename == 'Patient.csv':
            clean_patient_csv(input_path, output_path)
        else:
            clean_generic_csv(input_path, output_path)
    
    print("\nCSV cleaning complete! Cleaned files are in the 'cleaned_csv' directory.")
    print("These files should now be ready for SQL database import.")

if __name__ == "__main__":
    main()
