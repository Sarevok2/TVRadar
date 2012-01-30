REQUIREMENTS:

1. TV Radar requires at least Sun Java 1.6.  Java is available for free at http://java.sun.com.

2. If you are using Linux and are going to be using XMLTV (see choosing source section below) then you will need to install XMLTV seperately (XMLTV is included with the Windows distribution of TV Radar).  XMLTV is available at wiki.xmltv.org.  Alterately, on most Debian based distributions such as Ubuntu you can run the command 'apt-get install xmltv'.





RUNNING:

1. Extract the contents of this archive

2. Run the script "Run TV Radar.bat" (Windows) or "Run TV Radar.sh" (Linux).





CHOOSING A SOURCE OF TVLISTINGS:

In order to use TV Radar you will need to choose a source (grabber) of TV listings.  This can be done through the first time wizard or through the options.  



The first option is the built in grabber for www.schedulesdirect.org, which is a service that provides TV listings for North America.  Schedules Direct is a non-profit organization which provides this service for a $20/year fee.  7 day free trial accounts are available.



If you are outside North America you can use XMLTV, which is an external open source program written in Perl (see wiki.xmltv.org).  XMLTV has grabbers for many sources all over the world.  There is a list of these grabbers in the XMLTV configuration dialog in TV Radar.





SCHEDULE AUTOMATIC DOWNLOADS:

TV Radar can make use of your operating system's task scheduler to regularly download TV listings at a certain day/time.  You can set it to download listings at a time when you are asleep or away from your computer.  This means that you will never have to wait for it to download listings whenever you open the program.  The advantage of using the operating system's scheduler is there doesn't need to be a TV Radar process running in the background all the time.



The script Download.bat (Windows) or Download.sh(Linux) will make TV Radar download the latest TV listings (as per current settings in options) and then terminate.  Simply set your task scheduler to run this script at regular intervals every few days.



Windows Task Scheduler instructions:

1. Go to start>programs>accessories>system tools>task scheduler

2. Create a new task to run Download.bat with the schedule of your choosing.  Be sure to set the working directory (Start in) to the TV Radar directory.



Crontab instructions (Linux):

1. Enter the following command: crontab -e

2. It will ask you which editor you want to use.  Choose nano if you are unsure.

3. Add the following line:

<minute> <hour> * * <day of week> export DISPLAY=:0; cd <TV Radar directory>; ./Download.sh

Replacing everything in <> with your own values.  <day of week> is a number from 0-6, with 0 being sunday

4. Save the file.

5. Type crontab -l to see a list of cron jobs to make sure it's listed

6. For more info about crontab type the following: man crontab


FEEDBACK:
Please send any feedback or bug reports to louis34@gmail.com