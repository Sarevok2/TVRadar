XMLTV 0.5.56, Windows binary release

Gather television listings, process them and organize your viewing.
XMLTV is a file format for storing TV listings, defined in xmltv.dtd.
Then there are several tools to produce and process these listings.

Please see doc\QuickStart for documentation on what each program does,
and xmltv.dtd for documentation on the file format.

This is a release of the software as a single Windows binary
xmltv.exe, generated from the Perl source code linked from
<http://sourceforge.net/project/showfiles.php?group_id=39046>.

* Known problems

Due to prerequisite problems, EXE support is not currently available
for tv_grab_jp and tv_grab_is. If you need that, you'll need to install
Perl and the necessary modules and use the full distribution.

Some Win98 users get errors in Perl58.dll when running tv_grab_na_dd. See
below for more info.

tv_grab_se_swedb sometimes fails to work on Windows if there are spaces
in the path to your home-directory. This can be avoided by setting
the environment variable HOME to a path without spaces (e.g. c:\home).

Progress is being made for a 1.0 release.  We've really been stable for
a while I and consider it well overdue. 

* Changes in this release (0.5.56)

tv_grab_uk_rt:  improve UTF8 support, improve actor support
tv_grab_huro:   Add Slovakian episode parsing
tv_grab_it_dvb: new grabber for Italian DVB-S stream
tv_grab_za:     South African grabber fixed

And of some bugfixes and polish. 

* Installation

There is no installer, just unpack the zipfile into a
directory such as C:\xmltv.  If you are reading this you've probably
already done that.

All the different programs are combined into a single executable.  For
example, instead of running 'tv_grab_na --days 2 >na.xml' you would run

c:\xmltv\xmltv.exe tv_grab_na_dd --days 2 --output a.xml

Apart from the extra 'xmltv.exe' at the front of each command line,
the usage should be the same as the Unix version.  Some programs make
use of a "share" directory.  That directory is assumed be named
"share" at the same location as the exe.  If you just keep everything
where you unzipped it, everything should be fine.  If you must move
xmltv.exe, you may need to specify a --share option with some
programs.

xmltv.exe will try and guess a timezone.  This usually works fine. If
it doesn't, you can set a TZ variable just like on unix.

* General Windows Notes

Spaces in filenames may cause problems with some programs.  Directories
with spaces (i.e. C:\program files\xmltv) are not recommended.
C:\xmltv is better.

Some of the programs allow you pass a date format on the command line.
This uses % followed by a letter to specify a component of a date, for
example %Y gives a four digit year.  This can cause problems on windows
since % is used as a shell escape character.

To get around this, use %% to pass a % to the application. (ex. %%Y%%M )

If you *DO* want to insert a shell variable, you can do so by surrounding
it with percents. (ex %HOME% )

* crash in with PERL58.DLL

When using tv_grab_na_dd, some users are experiencing a crash in the
Perl58.DLL.

I had a similar problem when I had an older version of Perl installed
as well as XMLTV. It seems to cause DLL confusion and eventually a program
crash.

I fixed my system when I removed the old Perl.  Re-installing Perl
didn't cause the problem to return.

Before that, I used the work-around below (I could fetch 3 days)

tv_grab_na_dd --output=a.xml --days 3
tv_grab_na_dd --output=b.xml --days 3 --offset 3
tv_grab_na_dd --output=c.xml --days 1 --offset 6
tv_cat a.xml b.xml c.xml >guide.xml

I really thought this problem would be a fluke and/or easily solved
when I solved it for myself.  That doesn't look to be the case.  I'm
looking for a solution.

* Proxy servers

Proxy server support is provide by the LWP modules.
You can define a proxy server via the HTTP_PROXY enviornment variable.
    http_proxy=http://somehost.somedomain:port

For more information, see the the following:
http://search.cpan.org/~gaas/libwww-perl-5.803/lib/LWP/UserAgent.pm#$ua->env_proxy

* Author and copying

This is free software distributed under the GPL, see COPYING.  But if
you would like to use the code under some other conditions, please do
ask.  The Windows executable distribution was created by Robert Eden;
for details of the many contributors to the project please download
the source code.

There is a web page at <http://membled.com/work/apps/xmltv/> and a
Sourceforge project 'XMLTV', where you can download the source code.
Sourceforge also hosts the following mailing lists
    xmltv-announce - low volume announcements. Please join this at least.
    xmltv-users    - how to use XMLTV
    xmltv-devel    - detailed discussions among developers

-- Robert Eden, rmeden@yahoo.com, 2009-08-09
$Id: README.win32,v 1.86 2009/08/10 05:22:03 rmeden Exp $
