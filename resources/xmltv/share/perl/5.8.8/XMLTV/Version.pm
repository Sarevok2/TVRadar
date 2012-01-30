=head1 NAME

XMLTV::Version

=head1 DESCRIPTION

Add a --version argument to your program, eg

  use XMLTV::Version '$Id: Version.pm,v 1.2 2006/11/30 19:13:37 mattiasholmlund Exp $';

If a --version parameter is supplied on the command-line, it will
be caught already by the "use" statement, a message will be printed
to STDOUT and the program will exit.

It is best to put the use XMLTV::Version statement before other module 
imports, so that even if they fail --version will still work.

=head1 SEE ALSO

L<XMLTV::Options>

=cut

package XMLTV::Version;

my $opt = '--version';
sub import( $$ ) {
    die "usage: use $_[0] <version-string>" if @_ != 2;
    my $seen = 0;
    foreach (@ARGV) {
	# This doesn't handle abbreviations in the GNU style.
	last if $_ eq '--';
	if ($_ eq $opt) {
	    $seen++ && warn "seen '$opt' twice\n";
	}
    }
    return if not $seen;

    eval {
	require XMLTV;
	print "XMLTV module version $XMLTV::VERSION\n";
    };
    print "could not load XMLTV module, xmltv is not properly installed\n"
      if $@;
    for ($_[1]) {
	if (m!\$Id: ([^,]+),v (\S+) ([0-9/: -]+)!) {
	    print "This is $1 version $2, $3\n";
	}
	else {
	    print "This program version $_\n";
	}
    }

    exit();
}

1;
