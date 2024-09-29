# PikaAnnouncer: Race Announcer Display

***

PikaAnnouncer is intended to be used in partnership with PikaTimer to provide the race announcer with a list of names, demographic data, etc. It can also be used standalone by importing the participant data via a CSV file. 

## Current Features
* Auto-Sync participant data from a local PikaTimer instance
* Font Size control
* Manual bib # entry
* Automatic bib number entry from PikaTimer
* Finish time displayed when connected to a PikaTimer instance

## Planned 
* "Screen Mode" for unattended display to a TV or similar monitor

## Manual CSV participant file format
PikaAnnouncer can be used in a "manual" mode by importing a CSV file with participant data. 

The system will look for the following named fields in the CSV file:
* bib: Bib Number
* first: First Name
* middle: Middle Name
* last: Last Name
* age:  Age of participant
* sex 
* city
* state
* country
* note: A note field for additional information to display




