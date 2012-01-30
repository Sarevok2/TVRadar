package XMLTV::ValidateFile;

use strict;

BEGIN {
    use Exporter   ();
    our (@ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS);
    
    @ISA         = qw(Exporter);
    @EXPORT      = qw( );
    %EXPORT_TAGS = ( );     # eg: TAG => [ qw!name1 name2! ],
    @EXPORT_OK   = qw/LoadDtd ValidateFile/;
}
our @EXPORT_OK;

use XML::LibXML;
use File::Slurp qw/read_file/;
use XMLTV::Supplement qw/GetSupplement/;

our $REQUIRE_CHANNEL_ID=1;

my( $dtd, $parser );

=head1 NAME

XMLTV::ValidateFile

=head1 DESCRIPTION

Utility library that validates that a file is correct according to 
http://membled.com/twiki/bin/view/Main/XmltvFileFormat.


=head1 EXPORTED FUNCTIONS

All these functions are exported on demand.

=over 4

=cut

=item LoadDtd

Load the xmltv dtd. Takes a single parameter which is the name of
the xmltv dtd file.

LoadDtd must be called before ValidateFile can be called.

=cut

sub LoadDtd { 
    my( $dtd_file ) = @_;

    my $dtd_str = read_file($dtd_file) 
	or die "Failed to read $dtd_file";

    $dtd = XML::LibXML::Dtd->parse_string($dtd_str);
}

=item ValidateFile

Validate that a file is valid according to the XMLTV dtd and try to check
that it contains valid information. ValidateFile takes a filename as parameter
and optionally also a day and an offset and prints error messages to STDERR.

ValidateFile returns a list of errors that it found with the file. Each
error takes the form of a keyword:  

ValidateFile checks the following:

=over

=item notwell

The file is not well-formed XML.

=item notdtd

The file does not follow the XMLTV DTD.

=item unknownid

No channel-entry found for a channelid that is used in a programme-entry.

=item duplicatechannel

More than one channel-entry found for a channelid.

=item noprogrammes

No programme entries were found in the file.

=item channelnoprogramme

There are no programme entries for one of the channels listed with a 
channel-entry.

=item invalidid

An xmltvid does not look like a proper id, i.e. it does not  match 
/^[-a-zA-Z0-9]+(\.[-a-zA-Z0-9]+)+$/.

=item noid

A programme-entry without an id was found.

=item emptytitle

A programme entry with an empty or missing title was found.

=item emptydescription

A programme entry with an empty desc-element was found. The desc-element
shall be omitted if there is no description.

=item badstart

A programme entry with an invalid start-time was found.

=item badstop

A programme entry with an invalid stop-time was found.

=item badepisode

A programme entry with an invalid episode number was found.

=back

If no errors are found, an empty list is returned.

=cut 

my %errors;
my %timezoneerrors;
  
sub ValidateFile {
    my( $file ) = @_;

    if( not defined( $parser ) ) {
	$parser = XML::LibXML->new();
	$parser->line_numbers(1);
    }

    if( not defined( $dtd ) ) {
	my $dtd_str = GetSupplement( undef, 'xmltv.dtd');
	$dtd = XML::LibXML::Dtd->parse_string( $dtd_str );
    }

    %errors = ();

    my $doc;
    
    eval { $doc = $parser->parse_file( $file ); };
    
    if ( $@ ) {
	w( "The file is not well-formed xml:\n$@ ", 'notwell');
	return (keys %errors);
    }
    
    eval { $doc->validate( $dtd ) };  
    if ( $@ ) {
	w( "The file is not valid according to the xmltv dtd:\n $@",
	   'notvalid' );
	return (keys %errors);
    }

    my $w = sub { 
	my( $p, $msg, $id ) = @_;
	w( "Line " . $p->line_number() . " $msg", $id );
    };
    
    my %channels;
    
    my $ns = $doc->find( "//channel" );

    foreach my $ch ($ns->get_nodelist) {
	my $channelid = $ch->findvalue('@id');
	my $display_name = $ch->findvalue('display-name/text()');
	
	$w->( $ch, "Invalid channel-id '$channelid'", 'invalidid' )
	    if $channelid !~ /^[-a-zA-Z0-9]+(\.[-a-zA-Z0-9]+)+$/;
	
	$w->( $ch, "Duplicate channel-tag for '$channelid'", 'duplicateid' )
	    if defined( $channels{$channelid} );
	
	$channels{$channelid} = 0;
    }
    
    $ns = $doc->find( "//programme" );
    if ($ns->size() == 0) {
	w( "No programme entries found.", 'noprogrammes' );
	return (keys %errors);
    }

    foreach my $p ($ns->get_nodelist) {
	my $channelid = $p->findvalue('@channel');
	my $start = $p->findvalue('@start');
	my $stop = $p->findvalue('@stop');
	my $title = $p->findvalue('title/text()');
	my $desc;
	$desc = $p->findvalue('desc/text()')
	    if $p->findvalue( 'count(desc)' );

	my $xmltv_episode = $p->findvalue('episode-num[@system="xmltv_ns"]' );

	if ($REQUIRE_CHANNEL_ID and not exists( $channels{$channelid} )) {
	    $w->( $p, "Channel '$channelid' does not have a <channel>-entry.",
		  'unknownid' );
	    $channels{$channelid} = 0;
	}

	$channels{$channelid}++;
	
	$w->( $p, "Empty title", 'emptytitle' )    
	    if $title =~ /^\s*$/;

	$w->( $p, "Empty description", 'emptydescription' )    
	    if defined($desc) and $desc =~ /^\s*$/;
	
	$w->( $p, "Invalid start-time '$start'", 'badstart' )
	    if not verify_time( $start );
	
	$w->( $p, "Invalid stop-time '$stop'", 'badstop' )
	    if $stop ne "" and not verify_time( $stop );

	if( $xmltv_episode =~ /\S/ ) {
	    $w->($p, "Invalid episode-number '$xmltv_episode'", 'badepisode' )
		if $xmltv_episode !~ /^\s*\d* (\s* \/ \s*\d+)? \s* \. 
		                       \s*\d* (\s* \/ \s*\d+)? \s* \. 
		                       \s*\d* (\s* \/ \s*\d+)? \s* $/x; 
	}
    }

    foreach my $channel (keys %channels) {
	if ($channels{$channel} == 0) {
	    w( "No programme entries found for $channel", 
	       'channelnoprogramme' );
	}
    }
    
    return (keys %errors);
}

sub verify_time
{
    my( $timestamp ) = @_;

    my( $date, $time, $tz ) =
	($timestamp =~ /^(\d{8})(\d{4,6})(\s+([A-Z]+|[+-]\d{4})){0,1}$/ );

    return 0 unless defined $time;

    if( not defined( $tz ) )
    {
	if( not defined( $timezoneerrors{$tz} ) ) {
	    w( "No timezone specified", 'missingtimezone' );
	    $timezoneerrors{$tz}++;
	    return 0;
	}
    }

    if( $tz =~ /[a-zA-Z]/ ) {
	if( not defined( $timezoneerrors{$tz} ) ) {
	    w( "Invalid timezone '$tz'", 'invalidtimezone' );
	    $timezoneerrors{$tz}++;
	    return 0;
	}
    }
    
    return 1;
}

sub w {
    my( $msg, $id ) = @_;
    print "$msg\n";
    $errors{$id}++ if defined $id;
}
    

1;

=back 

=head1 BUGS

It is currently necessary to specify the path to the xmltv dtd-file.
This should not be necessary.
   
=head1 COPYRIGHT

Copyright (C) 2006 Mattias Holmlund.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=cut

### Setup indentation in Emacs
## Local Variables:
## perl-indent-level: 4
## perl-continued-statement-offset: 4
## perl-continued-brace-offset: 0
## perl-brace-offset: -4
## perl-brace-imaginary-offset: 0
## perl-label-offset: -2
## cperl-indent-level: 4
## cperl-brace-offset: 0
## cperl-continued-brace-offset: 0
## cperl-label-offset: -2
## cperl-extra-newline-before-brace: t
## cperl-merge-trailing-else: nil
## cperl-continued-statement-offset: 2
## indent-tabs-mode: t
## End:
